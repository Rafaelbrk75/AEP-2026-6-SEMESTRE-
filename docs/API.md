# API REST — ImunizaMais

Base: `http://localhost:8080`
Content-Type: `application/json`

## Códigos de status

| Status | Quando |
|---|---|
| `200 OK` | Consulta bem-sucedida |
| `201 Created` | Recurso criado / dose registrada |
| `400 Bad Request` | Payload inválido (Bean Validation) — campo `detalhes` lista os erros |
| `404 Not Found` | Paciente, vacina ou campanha inexistente |
| `422 Unprocessable Entity` | Regra de negócio violada — campo `erro` traz o código da regra |

Formato de erro:

```json
{
  "timestamp": "2026-09-05T14:32:10.512",
  "status": 422,
  "erro": "INTERVALO_ENTRE_DOSES",
  "mensagem": "Intervalo minimo de 30 dia(s) nao respeitado: ultima dose em 2026-07-01 (4 dia(s) decorridos)",
  "detalhes": []
}
```

---

## Vacinas

### `POST /api/vacinas`

```json
{
  "nome": "Hepatite B",
  "fabricante": "Fiocruz",
  "dosesRecomendadas": 3,
  "intervaloDiasEntreDoses": 30,
  "idadeMinimaMeses": 0,
  "doencasPrevenidas": ["Hepatite B"]
}
```

`201` com o documento criado. `422 VACINA_DUPLICADA` se o nome já existir.

### `GET /api/vacinas`

Lista o catálogo.

### `GET /api/vacinas/{id}`

`404` se não existir.

---

## Pacientes

### `POST /api/pacientes`

```json
{
  "cpf": "12345678901",
  "nome": "Maria Souza",
  "dataNascimento": "1990-05-12",
  "contato": { "telefone": "44999990001", "email": "maria@exemplo.com" },
  "endereco": {
    "logradouro": "Av. Guedner",
    "numero": "1610",
    "bairro": "Jardim Aclimacao",
    "cidade": "Maringa",
    "uf": "PR",
    "cep": "87050-900"
  }
}
```

Validações: `cpf` com 11 dígitos, `nome` não vazio, `dataNascimento` no passado,
`uf` com 2 letras, `email` em formato válido.
`422 PACIENTE_DUPLICADO` se o CPF já existir.

### `GET /api/pacientes` · `GET /api/pacientes?cidade=Maringa`

Lista todos ou filtra pelo campo aninhado `endereco.cidade`.

### `GET /api/pacientes/{cpf}`

Retorna o paciente com o histórico de doses completo.

---

## Aplicação de dose

### `POST /api/pacientes/{cpf}/doses`

```json
{
  "vacinaId": "66f0c1e2a1b2c3d4e5f60010",
  "campanhaId": null,
  "dataAplicacao": "2026-07-01",
  "lote": "LOTE-A",
  "unidadeSaude": "UBS Central"
}
```

`campanhaId` é opcional (dose de rotina fora de campanha). O **número da dose não é
enviado**: é derivado do histórico do paciente, o que impede furo de sequência.

Retorna `201` com o paciente atualizado.

Regras aplicadas antes de persistir — cada uma responde `422` com o próprio código:

| Código | Condição de falha |
|---|---|
| `DATA_APLICACAO` | Data futura ou anterior ao nascimento |
| `IDADE_MINIMA` | Paciente abaixo da idade mínima da vacina |
| `ESQUEMA_COMPLETO` | Todas as doses recomendadas já foram aplicadas |
| `INTERVALO_ENTRE_DOSES` | Intervalo mínimo desde a última dose não cumprido |
| `CAMPANHA_VIGENTE` | Campanha de outra vacina, encerrada, fora do período ou paciente fora do público-alvo |

Efeito colateral: se `campanhaId` for informado, o contador `dosesAplicadas` da
campanha é incrementado.

### `GET /api/pacientes/{cpf}/situacao`

```json
[
  {
    "vacinaId": "66f0c1e2a1b2c3d4e5f60010",
    "nomeVacina": "Hepatite B",
    "dosesAplicadas": 1,
    "dosesRecomendadas": 3,
    "situacao": "EM_DIA",
    "proximaDose": 2,
    "dataPrevistaProximaDose": "2026-07-31"
  },
  {
    "vacinaId": "66f0c1e2a1b2c3d4e5f60012",
    "nomeVacina": "Triplice Viral",
    "dosesAplicadas": 0,
    "dosesRecomendadas": 2,
    "situacao": "PENDENTE",
    "proximaDose": 1,
    "dataPrevistaProximaDose": "2026-09-05"
  }
]
```

| Situação | Significado |
|---|---|
| `COMPLETO` | Todas as doses aplicadas |
| `EM_DIA` | Esquema em andamento, dentro do prazo |
| `ATRASADA` | A data prevista da próxima dose já passou |
| `PENDENTE` | Elegível, nenhuma dose aplicada |
| `NAO_ELEGIVEL` | Ainda não atingiu a idade mínima (`dataPrevistaProximaDose` = data em que ficará elegível) |

---

## Campanhas

### `POST /api/campanhas`

```json
{
  "nome": "Campanha de Influenza 2026",
  "vacinaId": "66f0c1e2a1b2c3d4e5f60011",
  "idadeMinimaMeses": 6,
  "idadeMaximaMeses": 1200,
  "descricaoPublicoAlvo": "Criancas a partir de 6 meses e adultos",
  "dataInicio": "2026-08-06",
  "dataFim": "2026-11-04",
  "metaDoses": 500
}
```

`404` se a vacina não existir. `422 PERIODO_INVALIDO` se `dataFim < dataInicio`.
`422 PUBLICO_ALVO_INVALIDO` se `idadeMaximaMeses < idadeMinimaMeses`.

### `GET /api/campanhas` · `GET /api/campanhas?apenasAtivas=true`

### `GET /api/campanhas/{id}`

### `GET /api/campanhas/{id}/cobertura`

```json
{
  "campanhaId": "66f0c1e2a1b2c3d4e5f60020",
  "nome": "Campanha de Influenza 2026",
  "metaDoses": 500,
  "dosesAplicadas": 137,
  "percentualCobertura": 27.4,
  "metaAtingida": false,
  "ativa": true
}
```

### `PATCH /api/campanhas/{id}/encerramento`

Marca a campanha como inativa. Doses passam a ser recusadas com `422 CAMPANHA_VIGENTE`.
