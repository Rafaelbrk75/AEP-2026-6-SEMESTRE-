package br.com.unicesumar.aep.imunizamais.domain;

/**
 * Objeto de valor aninhado no documento de campanha.
 */
public class PublicoAlvo {

    private int idadeMinimaMeses;
    private int idadeMaximaMeses;
    private String descricao;

    protected PublicoAlvo() {
    }

    public PublicoAlvo(int idadeMinimaMeses, int idadeMaximaMeses, String descricao) {
        this.idadeMinimaMeses = idadeMinimaMeses;
        this.idadeMaximaMeses = idadeMaximaMeses;
        this.descricao = descricao;
    }

    public int getIdadeMinimaMeses() {
        return idadeMinimaMeses;
    }

    public int getIdadeMaximaMeses() {
        return idadeMaximaMeses;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean contempla(int idadeEmMeses) {
        return idadeEmMeses >= idadeMinimaMeses && idadeEmMeses <= idadeMaximaMeses;
    }
}
