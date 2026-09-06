package br.com.unicesumar.aep.imunizamais.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record NovoPacienteRequest(
        @NotBlank(message = "cpf e obrigatorio")
        @Pattern(regexp = "\\d{11}", message = "cpf deve conter 11 digitos") String cpf,

        @NotBlank(message = "nome e obrigatorio") String nome,

        @NotNull(message = "dataNascimento e obrigatoria")
        @Past(message = "dataNascimento deve estar no passado") LocalDate dataNascimento,

        @Valid @NotNull(message = "contato e obrigatorio") ContatoDTO contato,

        @Valid @NotNull(message = "endereco e obrigatorio") EnderecoDTO endereco) {
}
