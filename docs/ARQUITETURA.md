# Arquitetura Técnica — ImunizaMais

## 1. Visão geral

A PoC é uma aplicação Spring Boot em camadas, com o domínio isolado da infraestrutura.
O fluxo de uma requisição é sempre o mesmo:

```
HTTP  ->  Controller  ->  Service  ->  [Regras de negócio]  ->  Repository  ->  MongoDB
                 ^                                                  |
                 +----------- GlobalExceptionHandler <--------------+
```

| Pacote | Responsabilidade |
|---|---|
| `web` | Controllers REST, DTOs de entrada/saída e tradução de exceções em respostas HTTP |
| `service` | Casos de uso; orquestra repositórios e regras, sem lógica de domínio duplicada |
| `domain` | Entidades, objetos de valor, enums e as regras de negócio |
| `domain.regra` | Implementações de `RegraAplicacaoDose` (polimorfismo) |
| `repository` | Interfaces Spring Data MongoDB |
| `exception` | Hierarquia de exceções de domínio |
| `config` | Carga inicial de dados para demonstração |

## 2. Decisões de projeto

### 2.1 Regras de negócio como estratégias, não como `if`

O ponto mais sensível da aplicação — decidir se uma dose pode ser aplicada — envolve
cinco validações independentes. Em vez de concentrá-las numa cadeia de condicionais
dentro do serviço, cada uma é uma classe:

```java
public interface RegraAplicacaoDose {
    String codigo();
    void validar(ContextoAplicacao contexto);
}
```

O Spring injeta **todas** as implementações anotadas com `@Component` como uma
`List<RegraAplicacaoDose>`, e o serviço apenas itera:

```java
for (RegraAplicacaoDose regra : regras) {
    regra.validar(contexto);
}
```

Consequências práticas:

- Adicionar uma nova regra não altera nenhuma linha do `VacinacaoService` — basta criar
  a classe (aberto para extensão, fechado para modificação).
- Cada regra é testável isoladamente, sem mock de repositório.
- O código de erro (`IDADE_MINIMA`, `INTERVALO_ENTRE_DOSES`, …) chega ao cliente HTTP,
  então o consumidor da API sabe exatamente o que corrigir.

A existência e o estado do posto de saúde (`POSTO_INATIVO`) são validados à parte,
diretamente no `VacinacaoService` — no mesmo espírito das checagens de "recurso não
encontrado" que já existiam para vacina e campanha. Não virou uma implementação de
`RegraAplicacaoDose` porque não depende do `ContextoAplicacao` usado pelas outras
regras (não participa da decisão clínica de aplicar a dose, só confirma onde ela
aconteceu); mantê-la fora da lista evita alargar a interface do contexto por conta
de uma checagem de existência.

### 2.2 Comportamento nas entidades, não em "helpers"

As entidades carregam as decisões que dizem respeito ao próprio estado:

| Classe | Comportamento |
|---|---|
| `Paciente` | `idadeEmMesesEm`, `dosesDaVacina`, `ultimaDoseDe`, `proximoNumeroDose`, `registrarDose` |
| `Campanha` | `vigenteEm`, `aptaPara`, `registrarDose`, `percentualCobertura`, `metaAtingida` |
| `Vacina` | `exigeReforco`, `doseValida` |
| `PublicoAlvo` | `contempla` |

Os campos são privados; as coleções são expostas via `Collections.unmodifiableList`,
de modo que o histórico de doses só muda por `registrarDose`. Isso é encapsulamento
real, não getter/setter cerimonial.

### 2.3 Referência por id, não `@DBRef`

`campanhas.vacinaId` e `historicoDoses[].vacinaId` guardam apenas o identificador.
`@DBRef` foi descartado porque provoca leitura adicional a cada acesso e acopla o
modelo à API do driver. A desnormalização do `nomeVacina` (e, do mesmo jeito,
`nomePostoSaude`) dentro da dose é deliberada: a carteira de vacinação é um registro
histórico, e o nome vigente à época da aplicação deve ser preservado mesmo se o
catálogo mudar ou o posto for renomeado/desativado depois.

### 2.4 Relógio injetável

`VacinacaoService` e `DataAplicacaoRegra` recebem um `java.time.Clock`. Em produção é
`Clock.systemDefaultZone()`; nos testes, um `Clock.fixed` em `2026-09-05`. Sem isso, o
teste de "dose atrasada" passaria hoje e falharia daqui a três meses.

### 2.5 Erros com significado

```
RuntimeException
└── DominioException (abstrata)
    ├── RecursoNaoEncontradoException  -> HTTP 404
    └── RegraNegocioException          -> HTTP 422 (+ código da regra)
```

Falha de validação de payload (Bean Validation) vira **HTTP 400** com a lista de
campos inválidos. O `GlobalExceptionHandler` é o único ponto que conhece códigos HTTP.

## 3. Estratégia de testes

| Nível | Alvo | Técnica |
|---|---|---|
| Unidade — domínio | Entidades, objetos de valor, enum | JUnit 5 puro |
| Unidade — regras | As 5 implementações de `RegraAplicacaoDose` | JUnit 5 + `Clock` fixo |
| Unidade — serviços | `VacinacaoService`, `PacienteService`, `VacinaService`, `CampanhaService`, `PostoSaudeService` | JUnit 5 + Mockito (repositórios mockados) |
| Integração da camada web | Os 4 controllers + `GlobalExceptionHandler` | MockMvc **standalone** com serviços mockados |

**Nenhum teste exige MongoDB em execução.** O MockMvc standalone não sobe o contexto
do Spring: monta o controller diretamente com os conversores de mensagem e o validador,
o que torna a suíte rápida e reproduzível em qualquer máquina e no CI.

A cobertura é medida pelo JaCoCo. `ImunizaMaisApplication`, `config` e `web/dto` estão
excluídos: são, respectivamente, bootstrap, carga de demonstração e records sem lógica —
incluí-los inflaria o percentual sem representar teste real.

## 4. Como estender

| Necessidade | O que fazer |
|---|---|
| Nova regra de aplicação | Criar classe `@Component` que implemente `RegraAplicacaoDose` |
| Nova consulta | Declarar o método na interface do repositório (query derivada) |
| Novo endpoint | Controller + DTO record; erros já são tratados globalmente |
| Nova situação vacinal | Adicionar item em `SituacaoVacinal` e o ramo em `VacinacaoService.avaliar` |
