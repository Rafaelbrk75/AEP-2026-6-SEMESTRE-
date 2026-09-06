package br.com.unicesumar.aep.imunizamais.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Subdocumento que compoe a lista "historicoDoses" do documento de paciente.
 * Referencia as colecoes "vacinas" e "campanhas" por identificador.
 */
public class DoseAplicada {

    private String vacinaId;
    private String nomeVacina;
    private int numeroDose;
    private LocalDate dataAplicacao;
    private String lote;
    private String postoSaudeId;
    private String nomePostoSaude;
    private String campanhaId;

    protected DoseAplicada() {
    }

    public DoseAplicada(String vacinaId, String nomeVacina, int numeroDose, LocalDate dataAplicacao,
                        String lote, String postoSaudeId, String nomePostoSaude, String campanhaId) {
        this.vacinaId = Objects.requireNonNull(vacinaId, "vacinaId e obrigatorio");
        this.nomeVacina = nomeVacina;
        this.numeroDose = numeroDose;
        this.dataAplicacao = Objects.requireNonNull(dataAplicacao, "dataAplicacao e obrigatoria");
        this.lote = lote;
        this.postoSaudeId = postoSaudeId;
        this.nomePostoSaude = nomePostoSaude;
        this.campanhaId = campanhaId;
    }

    public String getVacinaId() {
        return vacinaId;
    }

    public String getNomeVacina() {
        return nomeVacina;
    }

    public int getNumeroDose() {
        return numeroDose;
    }

    public LocalDate getDataAplicacao() {
        return dataAplicacao;
    }

    public String getLote() {
        return lote;
    }

    public String getPostoSaudeId() {
        return postoSaudeId;
    }

    public String getNomePostoSaude() {
        return nomePostoSaude;
    }

    public String getCampanhaId() {
        return campanhaId;
    }

    public boolean referenteA(String vacinaId) {
        return this.vacinaId.equals(vacinaId);
    }

    public boolean vinculadaACampanha() {
        return campanhaId != null && !campanhaId.isBlank();
    }
}
