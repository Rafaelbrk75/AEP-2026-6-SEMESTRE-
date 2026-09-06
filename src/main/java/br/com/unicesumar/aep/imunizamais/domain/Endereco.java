package br.com.unicesumar.aep.imunizamais.domain;

/**
 * Objeto de valor aninhado dentro do documento de paciente.
 */
public class Endereco {

    private String logradouro;
    private String numero;
    private String bairro;
    private String cidade;
    private String uf;
    private String cep;

    protected Endereco() {
    }

    public Endereco(String logradouro, String numero, String bairro, String cidade, String uf, String cep) {
        this.logradouro = logradouro;
        this.numero = numero;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf;
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getUf() {
        return uf;
    }

    public String getCep() {
        return cep;
    }

    public String resumo() {
        return logradouro + ", " + numero + " - " + bairro + " - " + cidade + "/" + uf;
    }
}
