package br.com.unicesumar.aep.imunizamais.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContatoDTO(
        @NotBlank(message = "telefone e obrigatorio") String telefone,
        @Email(message = "email invalido") String email) {
}
