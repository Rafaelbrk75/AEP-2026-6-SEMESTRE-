# Modelo de Dados NoSQL — ImunizaMais

Banco: **MongoDB** (orientado a documentos). Database: `imunizamais`.

## 1. Aderência ao requisito da AEP

O item 8 do enunciado exige, no estágio mais avançado, **múltiplas coleções**,
**relacionamento entre coleções** e **pelo menos uma coleção contendo objetos
complexos** (documentos aninhados ou listas de subdocumentos). O modelo abaixo atende
aos três pontos:

| Requisito | Onde é atendido |
|---|---|
| Múltiplas coleções | `vacinas`, `pacientes`, `campanhas`, `postos_saude` |
| Relacionamento entre coleções | `campanhas.vacinaId` → `vacinas._id`; `pacientes.historicoDoses[].vacinaId` → `vacinas._id`; `pacientes.historicoDoses[].campanhaId` → `campanhas._id`; `pacientes.historicoDoses[].postoSaudeId` → `postos_saude._id` |
| Objetos complexos | `pacientes` contém `contato` e `endereco` (documentos aninhados) e `historicoDoses` (lista de subdocumentos); `campanhas` contém `publicoAlvo` (documento aninhado); `postos_saude` contém `endereco` (documento aninhado) |

## 2. Diagrama de relacionamento

```
┌──────────────────────┐     ┌──────────────────────┐
│ vacinas              │     │ postos_saude         │
│ _id                  │     │ _id                  │
│ nome (unico)         │     │ nome (unico)         │
│ dosesRecomendadas    │     │ telefone             │
│ intervaloDias...     │     │ capacidadeDiaria...  │
│ idadeMinimaMeses     │     │ endereco {...}       │  (aninhado)
│ doencasPrevenidas[]  │     │ ativo                │
└──────────────────────┘     └──────────────────────┘

┌──────────────────────┐
│ campanhas            │
│ _id                  │
│ vacinaId             │
│ publicoAlvo {...}    │  (aninhado)
│ dataInicio/dataFim   │
│ metaDoses            │
│ dosesAplicadas       │
│ ativa                │
└──────────────────────┘

┌──────────────────────────────────────────────────────┐
│ pacientes                                             │
│ _id                                                   │
│ cpf (unico)                                           │
│ nome, dataNascimento                                  │
│ contato { telefone, email }              (aninhado)   │
│ endereco { logradouro, ..., cidade, uf } (aninhado)   │
│ historicoDoses [                 (lista de subdocs)   │
│   { vacinaId, nomeVacina, numeroDose, dataAplicacao,  │
│     lote, postoSaudeId, nomePostoSaude, campanhaId }  │
│ ]                                                     │
└──────────────────────────────────────────────────────┘
```

Relacionamentos (por identificador, sem *foreign key* nativa do MongoDB):

```
vacinas ──┬──< campanhas.vacinaId
          └──< pacientes.historicoDoses[].vacinaId

campanhas ───< pacientes.historicoDoses[].campanhaId

postos_saude ───< pacientes.historicoDoses[].postoSaudeId
```

## 3. Coleção `vacinas`

Catálogo do esquema técnico. É a coleção de referência das outras duas.

```json
{
  "_id": "66f0c1e2a1b2c3d4e5f60010",
  "nome": "Hepatite B",
  "fabricante": "Fiocruz",
  "dosesRecomendadas": 3,
  "intervaloDiasEntreDoses": 30,
  "idadeMinimaMeses": 0,
  "doencasPrevenidas": ["Hepatite B"]
}
```

| Campo | Tipo | Observação |
|---|---|---|
| `nome` | string | Índice único (`@Indexed(unique = true)`) |
| `dosesRecomendadas` | int | Limite superior do esquema; usado pela regra `ESQUEMA_COMPLETO` |
| `intervaloDiasEntreDoses` | int | Usado pela regra `INTERVALO_ENTRE_DOSES` |
| `idadeMinimaMeses` | int | Usado pela regra `IDADE_MINIMA` |
| `doencasPrevenidas` | array de string | Array simples de escalares |

## 4. Coleção `postos_saude`

Unidades onde as doses sao efetivamente aplicadas. Referenciada por id em
`pacientes.historicoDoses[].postoSaudeId`.

```json
{
  "_id": "66f0c1e2a1b2c3d4e5f60030",
  "nome": "UBS Central",
  "telefone": "44898887777",
  "capacidadeDiariaDoses": 150,
  "endereco": {
    "logradouro": "Av. Brasil", "numero": "500", "bairro": "Centro",
    "cidade": "Maringa", "uf": "PR", "cep": "87013-000"
  },
  "ativo": true
}
```

| Campo | Tipo | Observação |
|---|---|---|
| `nome` | string | Índice único (`@Indexed(unique = true)`) |
| `capacidadeDiariaDoses` | int | Informativo; não é validado automaticamente na aplicação de dose |
| `endereco` | documento aninhado | Reaproveita o mesmo objeto de valor `Endereco` usado em `pacientes` |
| `ativo` | boolean | Um posto inativo faz `POST /api/pacientes/{cpf}/doses` responder `422 POSTO_INATIVO` |

## 5. Coleção `pacientes`

Contém os objetos complexos do modelo.

```json
{
  "_id": "66f0c1e2a1b2c3d4e5f60001",
  "cpf": "12345678901",
  "nome": "Maria Souza",
  "dataNascimento": "1990-05-12",
  "contato": {
    "telefone": "44999990001",
    "email": "maria@exemplo.com"
  },
  "endereco": {
    "logradouro": "Av. Guedner",
    "numero": "1610",
    "bairro": "Jardim Aclimacao",
    "cidade": "Maringa",
    "uf": "PR",
    "cep": "87050-900"
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
    },
    {
      "vacinaId": "66f0c1e2a1b2c3d4e5f60010",
      "nomeVacina": "Hepatite B",
      "numeroDose": 2,
      "dataAplicacao": "2026-08-05",
      "lote": "LOTE-C",
      "postoSaudeId": "66f0c1e2a1b2c3d4e5f60030",
      "nomePostoSaude": "UBS Central",
      "campanhaId": "66f0c1e2a1b2c3d4e5f60020"
    }
  ]
}
```

### Por que o histórico fica embutido no paciente

A carteira de vacinação é sempre lida **junto** com o paciente e nunca isoladamente:
consultar a situação vacinal, validar o intervalo entre doses e registrar uma nova
aplicação são operações que já têm o paciente em mãos. Embutir a lista elimina o
*join* e faz de cada aplicação uma escrita atômica em um único documento.

O volume é seguro: um esquema vacinal completo ao longo da vida tem dezenas de doses,
muito longe do limite de 16 MB por documento no MongoDB.

`nomeVacina` e `nomePostoSaude` são **desnormalizados** de propósito. A dose é um
registro histórico: se o catálogo de vacinas mudar de nome, ou o posto de saúde for
renomeado (ou mesmo desativado), a carteira deve continuar refletindo o que foi
aplicado na época — sem precisar de *join* para exibir o histórico.

## 6. Coleção `campanhas`

```json
{
  "_id": "66f0c1e2a1b2c3d4e5f60020",
  "nome": "Campanha de Influenza 2026",
  "vacinaId": "66f0c1e2a1b2c3d4e5f60011",
  "publicoAlvo": {
    "idadeMinimaMeses": 6,
    "idadeMaximaMeses": 1200,
    "descricao": "Criancas a partir de 6 meses e adultos"
  },
  "dataInicio": "2026-08-06",
  "dataFim": "2026-11-04",
  "metaDoses": 500,
  "dosesAplicadas": 137,
  "ativa": true
}
```

`dosesAplicadas` é um contador incrementado na mesma transação lógica em que a dose é
gravada no paciente. É desnormalização deliberada: o indicador de cobertura precisa
ser lido a todo momento e não pode depender de varrer todos os pacientes.

## 7. Índices

| Coleção | Campo | Tipo | Motivo |
|---|---|---|---|
| `pacientes` | `cpf` | único | Chave de negócio; toda a API busca por CPF |
| `vacinas` | `nome` | único | Impede catálogo duplicado |
| `postos_saude` | `nome` | único | Impede cadastro duplicado do mesmo posto |

Criados automaticamente (`spring.data.mongodb.auto-index-creation: true`).

## 8. Operações principais

| Operação | Coleções envolvidas |
|---|---|
| Cadastrar paciente | insert em `pacientes` |
| Registrar dose | read em `postos_saude` (existencia + `ativo`) + update em `pacientes` (push no array) + update em `campanhas` (contador) |
| Consultar situação vacinal | read em `pacientes` + read em `vacinas` |
| Listar alertas de vacinação (`/api/pacientes/alertas`) | read em `pacientes` (todos) + read em `vacinas` (todas) |
| Cobertura da campanha | read em `campanhas` |
| Pacientes por cidade | query em `pacientes` por campo aninhado (`endereco.cidade`) |

## 9. Comandos úteis no `mongosh`

```javascript
use imunizamais

db.vacinas.find().pretty()
db.postos_saude.find().pretty()
db.pacientes.find({ cpf: "12345678901" }).pretty()

// Consulta por campo aninhado
db.pacientes.find({ "endereco.cidade": "Maringa" })

// Consulta dentro da lista de subdocumentos
db.pacientes.find({ "historicoDoses.nomeVacina": "Hepatite B" })
db.pacientes.find({ "historicoDoses.postoSaudeId": "66f0c1e2a1b2c3d4e5f60030" })

// Cobertura das campanhas ativas
db.campanhas.find({ ativa: true }, { nome: 1, metaDoses: 1, dosesAplicadas: 1 })

// Postos de saude ativos
db.postos_saude.find({ ativo: true }, { nome: 1, telefone: 1 })
```
