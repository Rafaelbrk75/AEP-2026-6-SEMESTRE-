package br.com.unicesumar.aep.imunizamais;

import br.com.unicesumar.aep.imunizamais.domain.Campanha;
import br.com.unicesumar.aep.imunizamais.domain.Contato;
import br.com.unicesumar.aep.imunizamais.domain.DoseAplicada;
import br.com.unicesumar.aep.imunizamais.domain.Endereco;
import br.com.unicesumar.aep.imunizamais.domain.Paciente;
import br.com.unicesumar.aep.imunizamais.domain.PostoSaude;
import br.com.unicesumar.aep.imunizamais.domain.PublicoAlvo;
import br.com.unicesumar.aep.imunizamais.domain.Vacina;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Fabricas de objetos usadas pelos testes. Datas sao fixas para tornar os testes deterministicos.
 */
public final class TestFixtures {

    public static final LocalDate HOJE = LocalDate.of(2026, 9, 5);

    private TestFixtures() {
    }

    public static Clock relogioFixo() {
        return Clock.fixed(HOJE.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.of("UTC"));
    }

    public static Vacina hepatiteB() {
        return new Vacina("vac-hepb", "Hepatite B", "Fiocruz", 3, 30, 0, List.of("Hepatite B"));
    }

    public static Vacina tripliceViral() {
        return new Vacina("vac-tv", "Triplice Viral", "Fiocruz", 2, 90, 12,
                List.of("Sarampo", "Caxumba", "Rubeola"));
    }

    public static Vacina influenza() {
        return new Vacina("vac-flu", "Influenza", "Butantan", 1, 0, 6, List.of("Gripe sazonal"));
    }

    public static Paciente adulta() {
        return new Paciente("pac-1", "12345678901", "Maria Souza", LocalDate.of(1990, 5, 12),
                new Contato("44999990001", "maria@exemplo.com"),
                new Endereco("Av. Guedner", "1610", "Jardim Aclimacao", "Maringa", "PR", "87050-900"));
    }

    public static Paciente bebeCom3Meses() {
        return new Paciente("pac-2", "98765432100", "Joao Pedro Lima", HOJE.minusMonths(3),
                new Contato("44999990002", "responsavel@exemplo.com"),
                new Endereco("Rua Pioneiro", "245", "Zona 7", "Maringa", "PR", "87020-100"));
    }

    public static Campanha campanhaVigente(String vacinaId) {
        return new Campanha("camp-1", "Campanha de Influenza 2026", vacinaId,
                new PublicoAlvo(6, 1200, "Criancas a partir de 6 meses e adultos"),
                HOJE.minusDays(30), HOJE.plusDays(60), 500);
    }

    public static PostoSaude postoSaude() {
        return new PostoSaude("posto-1", "UBS Central", "44898887777", 150,
                new Endereco("Av. Brasil", "500", "Centro", "Maringa", "PR", "87013-000"));
    }

    public static DoseAplicada dose(String vacinaId, int numero, LocalDate data) {
        return new DoseAplicada(vacinaId, "Vacina " + vacinaId, numero, data, "LOTE-" + numero,
                "posto-1", "UBS Central", null);
    }
}
