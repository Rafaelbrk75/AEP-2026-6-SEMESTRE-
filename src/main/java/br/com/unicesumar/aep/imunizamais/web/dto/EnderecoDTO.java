package br.com.unicesumar.aep.imunizamais.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnderecoDTO(
        @NotBlank(message = "logradouro e obrigatorio") String logradouro,
        String numero,
        String bairro,
        @NotBlank(message = "cidade e obrigatoria") String cidade,
        @NotBlank(message = "uf e obrigatoria") @Size(min = 2, max = 2, message = "uf deve ter 2 letras") String uf,
        String cep) {
}
