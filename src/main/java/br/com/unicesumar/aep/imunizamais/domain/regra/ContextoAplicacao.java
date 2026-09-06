package br.com.unicesumar.aep.imunizamais.domain.regra;

import br.com.unicesumar.aep.imunizamais.domain.Campanha;
import br.com.unicesumar.aep.imunizamais.domain.Paciente;
import br.com.unicesumar.aep.imunizamais.domain.Vacina;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Reune tudo o que as regras precisam para decidir sobre a aplicacao de uma dose.
 * A campanha e opcional (dose de rotina, fora de campanha).
 */
public class ContextoAplicacao {

    private final Paciente paciente;
    private final Vacina vacina;
    private final Campanha campanha;
    private final LocalDate dataAplicacao;
    private final int numeroDose;

    public ContextoAplicacao(Paciente paciente, Vacina vacina, Campanha campanha,
                             LocalDate dataAplicacao, int numeroDose) {
        this.paciente = paciente;
        this.vacina = vacina;
        this.campanha = campanha;
        this.dataAplicacao = dataAplicacao;
        this.numeroDose = numeroDose;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public Vacina getVacina() {
        return vacina;
    }

    public Optional<Campanha> getCampanha() {
        return Optional.ofNullable(campanha);
    }

    public LocalDate getDataAplicacao() {
        return dataAplicacao;
    }

    public int getNumeroDose() {
        return numeroDose;
    }

    public int idadeDoPacienteEmMeses() {
        return paciente.idadeEmMesesEm(dataAplicacao);
    }
}
