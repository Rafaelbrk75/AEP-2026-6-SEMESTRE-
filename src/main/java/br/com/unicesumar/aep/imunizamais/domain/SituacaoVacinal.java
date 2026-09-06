package br.com.unicesumar.aep.imunizamais.domain;

/**
 * Classificacao da situacao do paciente em relacao ao esquema de uma vacina.
 */
public enum SituacaoVacinal {

    NAO_ELEGIVEL("Paciente ainda nao atingiu a idade minima para esta vacina"),
    PENDENTE("Nenhuma dose aplicada e o paciente ja e elegivel"),
    EM_DIA("Esquema em andamento e dentro do prazo previsto"),
    ATRASADA("Proxima dose ja venceu e nao foi aplicada"),
    COMPLETO("Todas as doses recomendadas foram aplicadas");

    private final String descricao;

    SituacaoVacinal(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean exigeAcao() {
        return this == PENDENTE || this == ATRASADA;
    }
}
