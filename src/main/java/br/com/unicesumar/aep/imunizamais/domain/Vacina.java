package br.com.unicesumar.aep.imunizamais.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento da colecao "vacinas": catalogo do esquema vacinal.
 */
@Document(collection = "vacinas")
public class Vacina {

    @Id
    private String id;

    @Indexed(unique = true)
    private String nome;

    private String fabricante;
    private int dosesRecomendadas;
    private int intervaloDiasEntreDoses;
    private int idadeMinimaMeses;
    private List<String> doencasPrevenidas = new ArrayList<>();

    protected Vacina() {
    }

    public Vacina(String id, String nome, String fabricante, int dosesRecomendadas,
                  int intervaloDiasEntreDoses, int idadeMinimaMeses, List<String> doencasPrevenidas) {
        this.id = id;
        this.nome = nome;
        this.fabricante = fabricante;
        this.dosesRecomendadas = dosesRecomendadas;
        this.intervaloDiasEntreDoses = intervaloDiasEntreDoses;
        this.idadeMinimaMeses = idadeMinimaMeses;
        this.doencasPrevenidas = doencasPrevenidas == null ? new ArrayList<>() : new ArrayList<>(doencasPrevenidas);
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

    public String getFabricante() {
        return fabricante;
    }

    public int getDosesRecomendadas() {
        return dosesRecomendadas;
    }

    public int getIntervaloDiasEntreDoses() {
        return intervaloDiasEntreDoses;
    }

    public int getIdadeMinimaMeses() {
        return idadeMinimaMeses;
    }

    public List<String> getDoencasPrevenidas() {
        return Collections.unmodifiableList(doencasPrevenidas);
    }

    /** Indica se o esquema exige mais de uma dose. */
    public boolean exigeReforco() {
        return dosesRecomendadas > 1;
    }

    /** Indica se o numero informado esta dentro do esquema recomendado. */
    public boolean doseValida(int numeroDose) {
        return numeroDose >= 1 && numeroDose <= dosesRecomendadas;
    }
}
