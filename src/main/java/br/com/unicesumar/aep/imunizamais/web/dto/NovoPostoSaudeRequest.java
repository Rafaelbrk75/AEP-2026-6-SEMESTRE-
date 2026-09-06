package br.com.unicesumar.aep.imunizamais.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NovoPostoSaudeRequest(
        @NotBlank(message = "nome e obrigatorio") String nome,
        @NotBlank(message = "telefone e obrigatorio") String telefone,
        @Min(value = 1, message = "capacidadeDiariaDoses deve ser no minimo 1") int capacidadeDiariaDoses,
        @Valid @NotNull(message = "endereco e obrigatorio") EnderecoDTO endereco) {
}
