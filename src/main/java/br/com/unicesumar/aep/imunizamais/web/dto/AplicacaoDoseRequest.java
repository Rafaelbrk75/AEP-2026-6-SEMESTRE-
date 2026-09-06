package br.com.unicesumar.aep.imunizamais.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AplicacaoDoseRequest(
        @NotBlank(message = "vacinaId e obrigatorio") String vacinaId,
        String campanhaId,
        @NotNull(message = "dataAplicacao e obrigatoria") LocalDate dataAplicacao,
        @NotBlank(message = "lote e obrigatorio") String lote,
        @NotBlank(message = "postoSaudeId e obrigatorio") String postoSaudeId) {
}
