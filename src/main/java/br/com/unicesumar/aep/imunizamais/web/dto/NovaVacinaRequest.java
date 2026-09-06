package br.com.unicesumar.aep.imunizamais.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record NovaVacinaRequest(
        @NotBlank(message = "nome e obrigatorio") String nome,
        @NotBlank(message = "fabricante e obrigatorio") String fabricante,
        @Min(value = 1, message = "dosesRecomendadas deve ser no minimo 1") int dosesRecomendadas,
        @Min(value = 0, message = "intervaloDiasEntreDoses nao pode ser negativo") int intervaloDiasEntreDoses,
        @Min(value = 0, message = "idadeMinimaMeses nao pode ser negativa") int idadeMinimaMeses,
        List<String> doencasPrevenidas) {
}
