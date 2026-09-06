package br.com.unicesumar.aep.imunizamais.domain;

import java.time.LocalDate;

/**
 * Objeto de valor calculado (nao persistido) que descreve a situacao do paciente
 * em relacao ao esquema de uma vacina.
 */
public class SituacaoPaciente {

    private final String vacinaId;
    private final String nomeVacina;
    private final int dosesAplicadas;
    private final int dosesRecomendadas;
    private final SituacaoVacinal situacao;
    private final Integer proximaDose;
    private final LocalDate dataPrevistaProximaDose;

    public SituacaoPaciente(String vacinaId, String nomeVacina, int dosesAplicadas, int dosesRecomendadas,
                            SituacaoVacinal situacao, Integer proximaDose, LocalDate dataPrevistaProximaDose) {
        this.vacinaId = vacinaId;
        this.nomeVacina = nomeVacina;
        this.dosesAplicadas = dosesAplicadas;
        this.dosesRecomendadas = dosesRecomendadas;
        this.situacao = situacao;
        this.proximaDose = proximaDose;
        this.dataPrevistaProximaDose = dataPrevistaProximaDose;
    }

    public String getVacinaId() {
        return vacinaId;
    }

    public String getNomeVacina() {
        return nomeVacina;
    }

    public int getDosesAplicadas() {
        return dosesAplicadas;
    }

    public int getDosesRecomendadas() {
        return dosesRecomendadas;
    }

    public SituacaoVacinal getSituacao() {
        return situacao;
    }

    public Integer getProximaDose() {
        return proximaDose;
    }

    public LocalDate getDataPrevistaProximaDose() {
        return dataPrevistaProximaDose;
    }

    public boolean exigeAcao() {
        return situacao.exigeAcao();
    }
}
