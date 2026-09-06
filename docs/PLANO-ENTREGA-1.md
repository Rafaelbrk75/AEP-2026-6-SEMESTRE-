# Plano da 1ª Entrega — AEP 2026.2 (6S)

## 1. Levantamento: o que a 1ª entrega exige

Valor total: **1,0 ponto**, distribuído em 7 critérios.

| # | Critério | Evidência que o professor vai procurar | Pontos | Status |
|---|---|---|---|---|
| 1 | Problema e alinhamento ao ODS | Problema claramente definido, público/contexto identificável e relação objetiva com o ODS escolhido | 0,1 | ✅ `README.md` §1 e §2 |
| 2 | Primeira versão funcional da PoC | A solução executa o fluxo principal e demonstra funcionalidade real | 0,1 | ✅ API REST completa + seed de dados |
| 3 | Banco de dados NoSQL | Uso efetivo de NoSQL e atendimento aos requisitos do semestre | 0,1 | ✅ MongoDB, 3 coleções, aninhados (`docs/MODELO-DADOS.md`) |
| 4 | POO e organização do código | Classes/objetos coerentes, responsabilidades compreensíveis, estrutura organizada | 0,1 | ✅ Camadas + regras polimórficas (`docs/ARQUITETURA.md`) |
| 5 | GitHub e versionamento | Repositório acessível, código versionado, histórico de commits, estrutura mínima | 0,1 | ⏳ **falta publicar** (§3 abaixo) |
| 6 | Testes automatizados | Testes relevantes, executáveis e coerentes com as funcionalidades | 0,1 | ✅ 6 classes de teste, JUnit 5 + Mockito + MockMvc |
| 7 | Cobertura ≥ 70% | Relatório **ou comando reproduzível** evidenciando ≥ 70%. **Abaixo de 70% este item vale 0.** | 0,1 | ✅ JaCoCo com `check` travando o build em 0,70 |
| 8 | Vídeo de demonstração (2–3 min) | Problema, ODS, arquitetura inicial e execução da PoC, dentro do tempo | 0,3 | ⏳ **falta gravar** (§4 abaixo) |

> **Atenção ao peso.** O vídeo sozinho vale 0,3 — mais que qualquer outro item. Não deixe para o final.

### Requisitos técnicos obrigatórios (seção 7 do enunciado)

| Área | Exigência | Como foi atendido |
|---|---|---|
| Banco de dados | NoSQL efetivo | MongoDB + Spring Data MongoDB |
| Programação | Linguagem OO com uso efetivo do paradigma | Java 21: encapsulamento, herança de exceções, polimorfismo nas regras, composição |
| Versionamento | GitHub acessível | Repositório + tag `entrega-1` |
| Testes | Testes automatizados executáveis | `mvn test` |
| Cobertura | ≥ 70% com evidência reproduzível | `mvn clean verify` falha abaixo de 70% |
| Documentação | Suficiente para compreender, instalar, executar e testar | `README.md` + `docs/` |
| Funcionalidade | PoC executável | `mvn spring-boot:run` + `docker compose up -d` |

### Decisão sobre a seção 8 (Evolução por Semestre)

O enunciado descreve dois níveis de modelagem. Como a turma é de **6º semestre**, a PoC
foi construída já no nível mais avançado — **múltiplas coleções, relacionamento entre
coleções e uma coleção com objetos complexos (documentos aninhados e lista de
subdocumentos)**. Isso atende simultaneamente aos dois níveis descritos, então não há
risco caso o professor esperasse o modelo mais simples.

> Vale confirmar com o professor mesmo assim — é uma pergunta de 30 segundos que elimina a única ambiguidade do enunciado.

---

## 2. O que já está pronto

```
imunizamais/
├── README.md                      # problema, ODS, tecnologias, execução, cobertura
├── pom.xml                        # Spring Boot 3.3.5 + JaCoCo travado em 70%
├── docker-compose.yml             # MongoDB + Mongo Express
├── .gitignore
├── .github/workflows/ci.yml       # build + testes + cobertura a cada push
├── docs/
│   ├── PLANO-ENTREGA-1.md         # este arquivo
│   ├── ARQUITETURA.md             # decisões de projeto e estratégia de testes
│   ├── MODELO-DADOS.md            # coleções, aninhamento, índices, mongosh
│   └── API.md                     # contrato de todos os endpoints
└── src/
    ├── main/  (26 classes)
    └── test/  (6 classes de teste + fixtures)
```

**Domínio:** `Paciente`, `Vacina`, `Campanha`, `DoseAplicada`, `Endereco`, `Contato`,
`PublicoAlvo`, `SituacaoPaciente`, enum `SituacaoVacinal`.

**Regras (polimórficas):** `DataAplicacaoRegra`, `IdadeMinimaRegra`,
`EsquemaCompletoRegra`, `IntervaloEntreDosesRegra`, `CampanhaVigenteRegra`.

**Endpoints:** cadastro de vacinas/pacientes/campanhas, registro de dose com validação,
consulta de situação vacinal e indicador de cobertura de campanha.

---

## 3. Passo a passo: publicar no GitHub

O repositório já está inicializado localmente, com um commit e a tag `entrega-1`.

```bash
cd imunizamais

# 1. Confirme que o build passa (precisa de internet para baixar as dependências)
mvn clean verify

# 2. Crie o repositório no GitHub (Público, sem README/.gitignore — já existem aqui)

# 3. Aponte o remoto e envie
git remote add origin https://github.com/<usuario>/imunizamais.git
git push -u origin main
git push origin entrega-1
```

**Antes do push, faça commits por partes** para que o histórico mostre evolução — o
critério 5 avalia justamente isso. Sugestão de sequência:

```bash
git reset --soft HEAD~1      # desfaz o commit único, mantendo os arquivos

git add pom.xml .gitignore docker-compose.yml
git commit -m "chore: estrutura do projeto Spring Boot com MongoDB e JaCoCo"

git add src/main/java/**/domain src/main/java/**/exception
git commit -m "feat: modelo de dominio e regras de aplicacao de dose"

git add src/main/java/**/repository src/main/java/**/service
git commit -m "feat: repositorios MongoDB e casos de uso"

git add src/main/java/**/web src/main/resources src/main/java/**/config
git commit -m "feat: API REST, tratamento de erros e carga inicial"

git add src/test
git commit -m "test: cobertura de dominio, regras, servicos e camada REST"

git add README.md docs .github
git commit -m "docs: README, documentacao tecnica e pipeline de CI"

git tag -f -a entrega-1 -m "Primeira entrega da AEP 2026.2"
```

**Cada integrante deve aparecer no histórico.** Se a equipe tem 3 pessoas, distribua os
commits acima entre as máquinas (ou use `git commit --author`), porque o professor pode
verificar a participação individual.

---

## 4. Roteiro do vídeo (2 a 3 minutos — vale 0,3)

O enunciado exige exatamente 4 tópicos. Cronometre.

| Tempo | Conteúdo | O que mostrar na tela |
|---|---|---|
| 0:00–0:30 | **O problema.** Controle de vacinação em papel/planilha: dose fora do esquema, abandono de esquema multidose, cobertura só conhecida no fechamento. | Slide simples ou o §1 do README |
| 0:30–0:50 | **O ODS atendido.** ODS 3 (Saúde e Bem-Estar), metas 3.2, 3.8 e 3.b — cite a relação objetiva, não genérica. | Tabela do §2 do README |
| 0:50–1:30 | **O projeto inicial.** Arquitetura em camadas, Java 21 + Spring Boot, MongoDB com 3 coleções relacionadas e histórico de doses aninhado. Mostre o diagrama. | `docs/MODELO-DADOS.md` (diagrama) e a árvore de pastas |
| 1:30–2:40 | **A primeira versão funcional.** Suba a aplicação e execute o fluxo: (a) listar vacinas; (b) consultar situação vacinal → `PENDENTE`; (c) registrar a 1ª dose → `201`; (d) tentar a 2ª dose antes do prazo → `422 INTERVALO_ENTRE_DOSES`; (e) consultar a cobertura da campanha. Feche rodando `mvn clean verify` e mostrando o relatório JaCoCo acima de 70%. | Terminal + Postman/curl + `target/site/jacoco/index.html` |
| 2:40–3:00 | Fechamento: o que vem na 2ª entrega. | §10 do README |

**Dicas que costumam render pontos:**

- Deixe o MongoDB já rodando e a aplicação já compilada antes de gravar — tempo perdido em build é tempo perdido de conteúdo.
- O erro `422` com o código da regra é o momento mais forte da demonstração: mostra que existe **regra de negócio real**, não só CRUD.
- Termine com o percentual de cobertura na tela. É a evidência do critério 7.
- Suba no YouTube como **"não listado"** e coloque o link na ficha.

---

## 5. Checklist final antes de enviar

- [ ] `mvn clean verify` passa e o JaCoCo mostra ≥ 70%
- [ ] Repositório público no GitHub, com histórico de commits distribuído pela equipe
- [ ] Tag `entrega-1` criada e enviada (`git push origin entrega-1`)
- [ ] README abre corretamente no GitHub (tabelas e blocos de código renderizando)
- [ ] Vídeo de 2 a 3 min publicado no YouTube (não listado) e link testado numa aba anônima
- [ ] Ficha de identificação (seção 11 do enunciado) preenchida: curso, série, RA e nome dos integrantes, título da PoC, link do vídeo, link do GitHub
- [ ] Título da PoC: **ImunizaMais — Gestão de Vacinação Comunitária (ODS 3)**
