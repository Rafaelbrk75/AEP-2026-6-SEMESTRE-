package br.com.unicesumar.aep.imunizamais.domain;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento da colecao "pacientes".
 * Contem objetos aninhados (contato, endereco) e uma lista de subdocumentos (historicoDoses),
 * atendendo ao requisito de objetos complexos previsto na AEP.
 */
@Document(collection = "pacientes")
public class Paciente {

    @Id
    private String id;

    @Indexed(unique = true)
    private String cpf;

    private String nome;
    private LocalDate dataNascimento;
    private Contato contato;
    private Endereco endereco;
    private List<DoseAplicada> historicoDoses = new ArrayList<>();

    protected Paciente() {
    }

    public Paciente(String id, String cpf, String nome, LocalDate dataNascimento,
                    Contato contato, Endereco endereco) {
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.contato = contato;
        this.endereco = endereco;
        this.historicoDoses = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCpf() {
        return cpf;
    }

    public String getNome() {
        return nome;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public Contato getContato() {
        return contato;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public List<DoseAplicada> getHistoricoDoses() {
        return Collections.unmodifiableList(historicoDoses);
    }

    /** Idade do paciente, em meses completos, na data de referencia. */
    public int idadeEmMesesEm(LocalDate referencia) {
        Period periodo = Period.between(dataNascimento, referencia);
        return periodo.getYears() * 12 + periodo.getMonths();
    }

    public int idadeEmAnosEm(LocalDate referencia) {
        return Period.between(dataNascimento, referencia).getYears();
    }

    /** Doses ja aplicadas de uma vacina especifica, ordenadas pelo numero da dose. */
    public List<DoseAplicada> dosesDaVacina(String vacinaId) {
        return historicoDoses.stream()
                .filter(dose -> dose.referenteA(vacinaId))
                .sorted(Comparator.comparingInt(DoseAplicada::getNumeroDose))
                .toList();
    }

    /** Ultima dose aplicada de uma vacina, se existir. */
    public Optional<DoseAplicada> ultimaDoseDe(String vacinaId) {
        return dosesDaVacina(vacinaId).stream()
                .max(Comparator.comparing(DoseAplicada::getDataAplicacao));
    }

    /** Numero da proxima dose do esquema desta vacina. */
    public int proximoNumeroDose(String vacinaId) {
        return dosesDaVacina(vacinaId).size() + 1;
    }

    public boolean jaTomouDose(String vacinaId, int numeroDose) {
        return dosesDaVacina(vacinaId).stream().anyMatch(dose -> dose.getNumeroDose() == numeroDose);
    }

    /** Adiciona a dose ao historico do paciente. */
    public void registrarDose(DoseAplicada dose) {
        if (dose == null) {
            throw new IllegalArgumentException("Dose nao pode ser nula");
        }
        this.historicoDoses.add(dose);
    }

    public int totalDosesAplicadas() {
        return historicoDoses.size();
    }
}
