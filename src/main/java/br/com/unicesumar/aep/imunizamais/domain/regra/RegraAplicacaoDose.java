package br.com.unicesumar.aep.imunizamais.domain.regra;

/**
 * Contrato das regras de negocio aplicadas antes de registrar uma dose.
 * Cada implementacao valida um aspecto isolado, permitindo compor o conjunto
 * de validacoes por polimorfismo, sem encadeamento de condicionais no servico.
 */
public interface RegraAplicacaoDose {

    /** Codigo curto que identifica a regra nas mensagens de erro. */
    String codigo();

    /** Lanca RegraNegocioException quando o contexto viola a regra. */
    void validar(ContextoAplicacao contexto);
}
