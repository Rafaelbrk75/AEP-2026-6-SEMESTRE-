package br.com.unicesumar.aep.imunizamais.web;

import br.com.unicesumar.aep.imunizamais.exception.RecursoNaoEncontradoException;
import br.com.unicesumar.aep.imunizamais.exception.RegraNegocioException;
import br.com.unicesumar.aep.imunizamais.web.dto.ErroResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converte as excecoes de dominio em respostas HTTP consistentes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        ErroResponse corpo = ErroResponse.de(HttpStatus.NOT_FOUND.value(),
                "RECURSO_NAO_ENCONTRADO", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(RegraNegocioException ex) {
        ErroResponse corpo = ErroResponse.de(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getRegra(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(corpo);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(this::descrever)
                .toList();
        ErroResponse corpo = ErroResponse.de(HttpStatus.BAD_REQUEST.value(),
                "PAYLOAD_INVALIDO", "Os dados enviados nao passaram na validacao", detalhes);
        return ResponseEntity.badRequest().body(corpo);
    }

    private String descrever(FieldError erro) {
        return erro.getField() + ": " + erro.getDefaultMessage();
    }
}
