package br.com.unicesumar.aep.imunizamais.domain;

/**
 * Objeto de valor aninhado com os canais de contato do paciente.
 */
public class Contato {

    private String telefone;
    private String email;

    protected Contato() {
    }

    public Contato(String telefone, String email) {
        this.telefone = telefone;
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public boolean possuiEmail() {
        return email != null && !email.isBlank();
    }
}
