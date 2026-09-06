package br.com.unicesumar.aep.imunizamais.domain;

import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento da colecao "campanhas". Referencia a colecao "vacinas" pelo campo vacinaId
 * e mantem o publico-alvo como subdocumento aninhado.
 */
@Document(collection = "campanhas")
public class Campanha {

    @Id
    private String id;

    private String nome;
    private String vacinaId;
    private PublicoAlvo publicoAlvo;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private int metaDoses;
    private int dosesAplicadas;
    private boolean ativa = true;

    protected Campanha() {
    }

    public Campanha(String id, String nome, String vacinaId, PublicoAlvo publicoAlvo,
                    LocalDate dataInicio, LocalDate dataFim, int metaDoses) {
        this.id = id;
        this.nome = nome;
        this.vacinaId = vacinaId;
        this.publicoAlvo = publicoAlvo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.metaDoses = metaDoses;
        this.dosesAplicadas = 0;
        this.ativa = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public String getVacinaId() {
        return vacinaId;
    }

    public PublicoAlvo getPublicoAlvo() {
        return publicoAlvo;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public int getMetaDoses() {
        return metaDoses;
    }

    public int getDosesAplicadas() {
        return dosesAplicadas;
    }

    public boolean isAtiva() {
        return ativa;
    }

    /** Verifica se a data informada esta dentro do periodo da campanha. */
    public boolean vigenteEm(LocalDate data) {
        return !data.isBefore(dataInicio) && !data.isAfter(dataFim);
    }

    /** Verifica se a campanha esta apta a receber doses na data informada. */
    public boolean aptaPara(LocalDate data, int idadeEmMeses) {
        return ativa && vigenteEm(data) && publicoAlvo != null && publicoAlvo.contempla(idadeEmMeses);
    }

    /** Incrementa o contador de doses aplicadas da campanha. */
    public void registrarDose() {
        this.dosesAplicadas++;
    }

    public void encerrar() {
        this.ativa = false;
    }

    /** Percentual de cobertura em relacao a meta, limitado a 100%. */
    public double percentualCobertura() {
        if (metaDoses <= 0) {
            return 0d;
        }
        double percentual = (dosesAplicadas * 100d) / metaDoses;
        return Math.min(percentual, 100d);
    }

    public boolean metaAtingida() {
        return metaDoses > 0 && dosesAplicadas >= metaDoses;
    }
}
