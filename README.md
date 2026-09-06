# ImunizaMais — Gestão de Vacinação Comunitária

> **AEP 2026.2 — Engenharia de Software — 6º Semestre — UniCesumar**
> Prova de Conceito (PoC) alinhada ao **ODS 3 — Saúde e Bem-Estar** da ONU.

---

## 1. Problema

Postos de saúde de pequenos municípios e equipes de campanha ainda controlam a
carteira de vacinação em papel ou em planilhas isoladas. Isso produz três falhas
concretas e mensuráveis:

1. **Doses fora do esquema.** Sem o histórico à mão, o profissional aplica a dose
   antes do intervalo mínimo, antes da idade recomendada ou além do número de doses
   previsto — o que reduz a eficácia da imunização e desperdiça insumo.
2. **Abandono do esquema multidose.** Vacinas como Hepatite B (3 doses) e Tríplice
   Viral (2 doses) exigem retorno. Sem cálculo automático da próxima dose, ninguém
   sabe quem está atrasado, e a busca ativa não acontece.
3. **Campanhas sem indicador em tempo real.** A cobertura só é conhecida no
   fechamento, quando já não dá para corrigir a rota.

**Público/contexto:** equipes de Atenção Primária (UBS) e coordenações de campanha
de vacinação em municípios de pequeno e médio porte.

## 2. Relação com o ODS 3

O **ODS 3 (Saúde e Bem-Estar)** tem como meta 3.8 alcançar a cobertura universal de
saúde e o acesso a vacinas essenciais seguras e eficazes para todos. A PoC contribui
de forma direta e objetiva:

| Meta do ODS 3 | Como o ImunizaMais atua |
|---|---|
| 3.2 — Reduzir a mortalidade infantil evitável | Calcula a situação vacinal de cada criança e sinaliza doses `PENDENTE` e `ATRASADA`, viabilizando busca ativa |
| 3.8 — Cobertura universal e acesso a vacinas essenciais | Indicador de cobertura por campanha (`dosesAplicadas / metaDoses`) disponível em tempo real |
| 3.b — Apoiar a imunização | Impede, por regra automatizada, a aplicação de doses fora do esquema técnico recomendado |

## 3. O que a PoC faz

- Cadastra **vacinas** com o esquema técnico (nº de doses, intervalo, idade mínima).
- Cadastra **pacientes** com contato e endereço aninhados e um **histórico de doses**
  como lista de subdocumentos.
- Cadastra **campanhas** com público-alvo aninhado, período e meta de doses.
- Cadastra **postos de saúde** (nome, telefone, capacidade diária, endereço) — é onde
  cada dose registrada é efetivamente aplicada.
- **Registra a aplicação de uma dose**, atravessando 6 regras de negócio antes de
  persistir; atualiza o histórico do paciente e a cobertura da campanha na mesma operação.
- **Calcula a situação vacinal** do paciente para todo o catálogo de vacinas
  (`COMPLETO`, `EM_DIA`, `PENDENTE`, `ATRASADA`, `NAO_ELEGIVEL`), com a data prevista
  da próxima dose.
- **Lista alertas de vacinação** (`/api/pacientes/alertas`): varre a base e aponta
  pacientes com dose pendente, atrasada, ou com a próxima dose vencendo nos próximos
  7 dias — busca ativa pronta para um posto de saúde consultar antes que a
  vacinação atrase.
- Expõe o **indicador de cobertura** de cada campanha.

### Regras de negócio implementadas

Cada regra é uma implementação da interface `RegraAplicacaoDose`, injetada como lista
pelo Spring e aplicada por polimorfismo — sem cadeia de `if` no serviço. A validação
do posto de saúde (existência e se está ativo) é feita diretamente no serviço, no
mesmo espírito das demais checagens de existência.

| Código | Regra |
|---|---|
| `DATA_APLICACAO` | Data não pode ser futura nem anterior ao nascimento |
| `IDADE_MINIMA` | Paciente precisa ter a idade mínima exigida pela vacina |
| `ESQUEMA_COMPLETO` | Não se aplica dose além do nº recomendado |
| `INTERVALO_ENTRE_DOSES` | Intervalo mínimo em dias entre a última dose e a próxima |
| `CAMPANHA_VIGENTE` | Campanha ativa, no período e com o paciente no público-alvo |
| `POSTO_INATIVO` | Posto de saúde informado existe, mas está desativado |

## 4. Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 (orientação a objetos) |
| Framework | Spring Boot 3.3.5 (Web, Validation) |
| Banco de dados | **MongoDB** (NoSQL, orientado a documentos) via Spring Data MongoDB |
| Testes | JUnit 5, Mockito, Spring MockMvc (standalone) |
| Cobertura | JaCoCo 0.8.12 com trava de build em 70% |
| Build | Maven 3.9+ |
| CI | GitHub Actions |

## 5. Modelo de dados NoSQL

Quatro coleções relacionadas por identificador, com objetos complexos aninhados —
atendendo ao requisito de evolução do 2º semestre da AEP (múltiplas coleções,
relacionamento entre coleções e ao menos uma coleção com documentos aninhados/listas
de subdocumentos).

```
vacinas ──┬──< campanhas.vacinaId
          └──< pacientes.historicoDoses[].vacinaId

campanhas ───< pacientes.historicoDoses[].campanhaId

postos_saude ───< pacientes.historicoDoses[].postoSaudeId
```

Documento de `pacientes` (objetos aninhados + lista de subdocumentos):

```json
{
  "_id": "66f0c1e2a1b2c3d4e5f60001",
  "cpf": "12345678901",
  "nome": "Maria Souza",
  "dataNascimento": "1990-05-12",
  "contato": { "telefone": "44999990001", "email": "maria@exemplo.com" },
  "endereco": {
    "logradouro": "Av. Guedner", "numero": "1610", "bairro": "Jardim Aclimacao",
    "cidade": "Maringa", "uf": "PR", "cep": "87050-900"
  },
  "historicoDoses": [
    {
      "vacinaId": "66f0c1e2a1b2c3d4e5f60010",
      "nomeVacina": "Hepatite B",
      "numeroDose": 1,
      "dataAplicacao": "2026-07-01",
      "lote": "LOTE-A",
      "postoSaudeId": "66f0c1e2a1b2c3d4e5f60030",
      "nomePostoSaude": "UBS Central",
      "campanhaId": null
    }
  ]
}
```

Detalhamento completo em [`docs/MODELO-DADOS.md`](docs/MODELO-DADOS.md).

## 6. Como executar

### Pré-requisitos

- JDK 21
- Maven 3.9+
- MongoDB 6+ (local, Docker ou Atlas)

### Passo 1 — subir o MongoDB

Com Docker (recomendado):

```bash
docker compose up -d
```

Ou aponte para uma instância existente:

```bash
export MONGODB_URI="mongodb://localhost:27017/imunizamais"
```

### Passo 2 — rodar a aplicação

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Na primeira execução, uma carga inicial
(`DataSeeder`) cria 3 vacinas, 2 postos de saúde, 2 campanhas e 2 pacientes (um deles
já com uma dose atrasada, pronta para aparecer em `/api/pacientes/alertas`). Para
desativar: `export APP_SEED_ENABLED=false`.

### Passo 3 — exercitar o fluxo principal

```bash
# 1. Listar as vacinas criadas pelo seed (guarde o id da Hepatite B)
curl -s http://localhost:8080/api/vacinas | jq

# 2. Listar os postos de saude criados pelo seed (guarde o id da UBS Central)
curl -s http://localhost:8080/api/postos-saude | jq

# 3. Consultar a situação vacinal da paciente Maria Souza
curl -s http://localhost:8080/api/pacientes/12345678901/situacao | jq

# 4. Registrar a 1ª dose de Hepatite B (troque <VACINA_ID> e <POSTO_ID>)
curl -s -X POST http://localhost:8080/api/pacientes/12345678901/doses \
  -H 'Content-Type: application/json' \
  -d '{"vacinaId":"<VACINA_ID>","dataAplicacao":"2026-07-01","lote":"LOTE-A","postoSaudeId":"<POSTO_ID>"}' | jq

# 5. Tentar aplicar a 2ª dose antes do intervalo mínimo -> 422 com o código da regra
curl -s -X POST http://localhost:8080/api/pacientes/12345678901/doses \
  -H 'Content-Type: application/json' \
  -d '{"vacinaId":"<VACINA_ID>","dataAplicacao":"2026-07-05","lote":"LOTE-B","postoSaudeId":"<POSTO_ID>"}' | jq

# 6. Cobertura de uma campanha (troque <CAMPANHA_ID>)
curl -s http://localhost:8080/api/campanhas | jq
curl -s http://localhost:8080/api/campanhas/<CAMPANHA_ID>/cobertura | jq

# 7. Ver quem esta com vacinacao pendente, atrasada ou proxima do vencimento
curl -s http://localhost:8080/api/pacientes/alertas | jq
```

Contrato completo dos endpoints em [`docs/API.md`](docs/API.md).

## 7. Testes e cobertura

Os testes **não exigem MongoDB em execução**: a persistência é mockada com Mockito e
a camada REST é testada com MockMvc em modo standalone.

```bash
# Roda os testes, gera o relatório e falha o build se a cobertura ficar abaixo de 70%
mvn clean verify
```

Relatório HTML gerado em:

```
target/site/jacoco/index.html
```

Resumo no terminal (linhas cobertas / total), sem abrir o navegador:

```bash
mvn clean verify && \
python3 -c "import xml.etree.ElementTree as ET; \
c={x.get('type'):x for x in ET.parse('target/site/jacoco/jacoco.xml').getroot().findall('counter')}; \
[print(f\"{k}: {int(v.get('covered'))}/{int(v.get('covered'))+int(v.get('missed'))} = {100*int(v.get('covered'))/(int(v.get('covered'))+int(v.get('missed'))):.1f}%\") for k,v in c.items()]"
```

A trava está declarada no `pom.xml` (execução `check-cobertura-minima`): se
`INSTRUCTION` ou `LINE` ficarem abaixo de **0,70**, o `mvn verify` **falha**. Ou seja,
um build verde já é a evidência reproduzível da cobertura mínima exigida pela AEP.

## 8. Estrutura do repositório

```
.
├── README.md
├── pom.xml
├── docker-compose.yml
├── .github/workflows/ci.yml       # build + testes + cobertura a cada push
├── docs/
│   ├── ARQUITETURA.md
│   ├── MODELO-DADOS.md
│   └── API.md
└── src/
    ├── main/java/br/com/unicesumar/aep/imunizamais/
    │   ├── ImunizaMaisApplication.java
    │   ├── config/       # carga inicial de dados
    │   ├── domain/       # entidades, objetos de valor e enums
    │   │   └── regra/    # regras de negócio polimórficas
    │   ├── exception/    # hierarquia de exceções de domínio
    │   ├── repository/   # Spring Data MongoDB
    │   ├── service/      # casos de uso
    │   └── web/          # controllers REST, DTOs e handler de erros
    └── test/java/...     # JUnit 5 + Mockito + MockMvc
```

## 9. Entrega da AEP

| Item exigido | Onde está |
|---|---|
| Repositório com a 1ª versão funcional | este repositório, tag `entrega-1` |
| README com problema, ODS, tecnologias e execução | este arquivo, seções 1–6 |
| Documentação técnica | `docs/` |
| Testes automatizados executáveis | `mvn test` |
| Evidência reproduzível de cobertura ≥ 70% | `mvn clean verify` + `target/site/jacoco/index.html` |
| Vídeo de demonstração (2–3 min) | link na ficha de identificação |

## 10. Próximos passos (2ª entrega)

- Consultas agregadas de cobertura por bairro/faixa etária (aggregation pipeline).
- Índices compostos e análise de plano de execução no MongoDB.
- Busca ativa por região: o endpoint `GET /api/pacientes/alertas` já lista pacientes
  com dose pendente/atrasada/próxima do vencimento; falta filtrar por
  bairro/cidade e por posto de saúde responsável.
- Disparo real de notificação (e-mail/SMS) a partir dos alertas — hoje o endpoint
  só expõe a lista, sem enviar nada.
- Vincular campanhas a postos de saúde específicos (hoje o posto só é registrado
  por dose aplicada, não por campanha).
- Autenticação e perfis de acesso (profissional × coordenação).

## 11. Equipe

| RA | Nome |
|---|---|
| _preencher_ | _preencher_ |
| _preencher_ | _preencher_ |
| _preencher_ | _preencher_ |
#   A E P - 2 0 2 6 - 6 - S E M E S T R E 
 
 