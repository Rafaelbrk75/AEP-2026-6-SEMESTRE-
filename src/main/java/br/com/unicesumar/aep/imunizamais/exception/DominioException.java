package br.com.unicesumar.aep.imunizamais.exception;

/**
 * Raiz da hierarquia de excecoes de negocio da aplicacao.
 */
public abstract class DominioException extends RuntimeException {

    protected DominioException(String mensagem) {
        super(mensagem);
    }
}
