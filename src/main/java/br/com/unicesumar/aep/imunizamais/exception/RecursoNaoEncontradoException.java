package br.com.unicesumar.aep.imunizamais.exception;

/**
 * Lancada quando um documento referenciado nao existe em nenhuma colecao.
 */
public class RecursoNaoEncontradoException extends DominioException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public static RecursoNaoEncontradoException de(String recurso, String identificador) {
        return new RecursoNaoEncontradoException(recurso + " nao encontrado(a): " + identificador);
    }
}
