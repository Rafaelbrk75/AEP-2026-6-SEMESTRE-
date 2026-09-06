package br.com.unicesumar.aep.imunizamais.exception;

/**
 * Lancada quando uma regra de aplicacao de dose e violada.
 */
public class RegraNegocioException extends DominioException {

    private final String regra;

    public RegraNegocioException(String regra, String mensagem) {
        super(mensagem);
        this.regra = regra;
    }

    public String getRegra() {
        return regra;
    }
}
