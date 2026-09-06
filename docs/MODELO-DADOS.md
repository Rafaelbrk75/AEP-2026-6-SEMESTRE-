# Modelo de Dados NoSQL — ImunizaMais

Banco: **MongoDB** (orientado a documentos). Database: `imunizamais`.

## 1. Aderência ao requisito da AEP

O item 8 do enunciado exige, no estágio mais avançado, **múltiplas coleções**,
**relacionamento entre coleções** e **pelo menos uma coleção contendo objetos
complexos** (documentos aninhados ou listas de subdocumentos). O modelo abaixo atende
aos três pontos:

| Requisito | Onde é atendido |
|---|---|
| Múltiplas coleções | `vacinas`, `pacientes`, `campanhas` |
| Relacionamento entre coleções | `campanhas.vacinaId` → `vacinas._id`; `pacientes.historicoDoses[].vacinaId` → `vacinas._id`; `pacientes.historicoDoses[].campanhaId` → `campanhas._id` |
| Objetos complexos | `pacientes` contém `contato` e `endereco` (documentos aninhados) e `historicoDoses` (lista de subdocumentos); `campanhas` contém `publicoAlvo` (documento aninhado) |

## 2. Diagrama de relacionamento

```
┌────────────────────┐
│      vacinas       │
│ _id                │◄───────────────┐
│ nome (único)       │                │
│ dosesRecomendadas  │◄──────┐        │
│ intervaloDias...   │       │        │
│ idadeMinimaMeses   │       │        │
│ doencasPrevenidas[]│       │        │
└────────────────────┘       │        │
                             │        │
┌────────────────────┐       │        │
│     campanhas      │       │        │
│ _id                │◄──┐   │        │
│ vacinaId ──────────┼───┼───┘        │
│ publicoAlvo {…}    │   │            │  (aninhado)
│ dataInicio/dataFim │   │            │
│ metaDoses          │   │            │
│ dosesAplicadas     │   │            │
│ ativa              │   │            │
└────────────────────┘   │            │
                         │            │
┌────────────────────────┴────────────┴────┐
│                 pacientes                │
│ _id                                      │
│ cpf (único)                              │
│ nome, dataNascimento                     │
│ contato { telefone, email }              │  (aninhado)
│ endereco { logradouro, …, cidade, uf }   │  (aninhado)
│ historicoDoses [                         │  (lista de subdocumentos)
│   { vacinaId, nomeVacina, numeroDose,    │
│     dataAplicacao, lote, unidadeSaude,   │
│     campanhaId }                         │
│ ]                                        │
└──────────────────────────────────────────┘
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

## 4. Coleção `pacientes`

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
      "unidadeSaude": "UBS Central",
      "campanhaId": null
    },
    {
      "vacinaId": "66f0c1e2a1b2c3d4e5f60010",
      "nomeVacina": "Hepatite B",
      "numeroDose": 2,
      "dataAplicacao": "2026-08-05",
      "lote": "LOTE-C",
      "unidadeSaude": "UBS Central",
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

`nomeVacina` é **desnormalizado** de propósito. A dose é um registro histórico: se o
catálogo mudar o nome da vacina, a carteira deve continuar refletindo o que foi
aplicado na época.

## 5. Coleção `campanhas`

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

## 6. Índices

| Coleção | Campo | Tipo | Motivo |
|---|---|---|---|
| `pacientes` | `cpf` | único | Chave de negócio; toda a API busca por CPF |
| `vacinas` | `nome` | único | Impede catálogo duplicado |

Criados automaticamente (`spring.data.mongodb.auto-index-creation: true`).

## 7. Operações principais

| Operação | Coleções envolvidas |
|---|---|
| Cadastrar paciente | insert em `pacientes` |
| Registrar dose | update em `pacientes` (push no array) + update em `campanhas` (contador) |
| Consultar situação vacinal | read em `pacientes` + read em `vacinas` |
| Cobertura da campanha | read em `campanhas` |
| Pacientes por cidade | query em `pacientes` por campo aninhado (`endereco.cidade`) |

## 8. Comandos úteis no `mongosh`

```javascript
use imunizamais

db.vacinas.find().pretty()
db.pacientes.find({ cpf: "12345678901" }).pretty()

// Consulta por campo aninhado
db.pacientes.find({ "endereco.cidade": "Maringa" })

// Consulta dentro da lista de subdocumentos
db.pacientes.find({ "historicoDoses.nomeVacina": "Hepatite B" })

// Cobertura das campanhas ativas
db.campanhas.find({ ativa: true }, { nome: 1, metaDoses: 1, dosesAplicadas: 1 })
```
