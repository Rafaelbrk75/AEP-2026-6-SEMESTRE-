package br.com.unicesumar.aep.imunizamais.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record NovaCampanhaRequest(
        @NotBlank(message = "nome e obrigatorio") String nome,
        @NotBlank(message = "vacinaId e obrigatorio") String vacinaId,
        @Min(value = 0, message = "idadeMinimaMeses nao pode ser negativa") int idadeMinimaMeses,
        @Min(value = 0, message = "idadeMaximaMeses nao pode ser negativa") int idadeMaximaMeses,
        String descricaoPublicoAlvo,
        @NotNull(message = "dataInicio e obrigatoria") LocalDate dataInicio,
        @NotNull(message = "dataFim e obrigatoria") LocalDate dataFim,
        @Min(value = 1, message = "metaDoses deve ser no minimo 1") int metaDoses) {
}
