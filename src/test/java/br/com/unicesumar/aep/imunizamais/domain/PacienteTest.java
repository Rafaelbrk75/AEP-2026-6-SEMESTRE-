package br.com.unicesumar.aep.imunizamais.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.unicesumar.aep.imunizamais.TestFixtures;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Paciente")
class PacienteTest {

    @Test
    @DisplayName("calcula idade em meses e anos completos")
    void idade() {
        Paciente paciente = TestFixtures.adulta();
        assertEquals(36, paciente.idadeEmAnosEm(LocalDate.of(2026, 9, 5)));
        assertEquals(36 * 12 + 3, paciente.idadeEmMesesEm(LocalDate.of(2026, 9, 5)));
        assertEquals(3, TestFixtures.bebeCom3Meses().idadeEmMesesEm(TestFixtures.HOJE));
    }

    @Test
    @DisplayName("registra doses no historico aninhado")
    void registrarDose() {
        Paciente paciente = TestFixtures.adulta();
        assertEquals(0, paciente.totalDosesAplicadas());

        paciente.registrarDose(TestFixtures.dose("vac-hepb", 1, LocalDate.of(2026, 1, 10)));
        paciente.registrarDose(TestFixtures.dose("vac-hepb", 2, LocalDate.of(2026, 2, 15)));
        paciente.registrarDose(TestFixtures.dose("vac-flu", 1, LocalDate.of(2026, 3, 1)));

        assertEquals(3, paciente.totalDosesAplicadas());
        assertEquals(2, paciente.dosesDaVacina("vac-hepb").size());
        assertEquals(1, paciente.dosesDaVacina("vac-flu").size());
        assertTrue(paciente.dosesDaVacina("inexistente").isEmpty());
    }

    @Test
    @DisplayName("nao aceita dose nula")
    void doseNula() {
        Paciente paciente = TestFixtures.adulta();
        assertThrows(IllegalArgumentException.class, () -> paciente.registrarDose(null));
    }

    @Test
    @DisplayName("historico exposto e imutavel")
    void historicoImutavel() {
        Paciente paciente = TestFixtures.adulta();
        List<DoseAplicada> historico = paciente.getHistoricoDoses();
        assertThrows(UnsupportedOperationException.class,
                () -> historico.add(TestFixtures.dose("vac-flu", 1, TestFixtures.HOJE)));
    }

    @Test
    @DisplayName("identifica a ultima dose e o proximo numero do esquema")
    void ultimaDoseEProximoNumero() {
        Paciente paciente = TestFixtures.adulta();
        assertEquals(Optional.empty(), paciente.ultimaDoseDe("vac-hepb"));
        assertEquals(1, paciente.proximoNumeroDose("vac-hepb"));

        paciente.registrarDose(TestFixtures.dose("vac-hepb", 1, LocalDate.of(2026, 1, 10)));
        paciente.registrarDose(TestFixtures.dose("vac-hepb", 2, LocalDate.of(2026, 2, 15)));

        Optional<DoseAplicada> ultima = paciente.ultimaDoseDe("vac-hepb");
        assertTrue(ultima.isPresent());
        assertEquals(LocalDate.of(2026, 2, 15), ultima.get().getDataAplicacao());
        assertEquals(3, paciente.proximoNumeroDose("vac-hepb"));
        assertTrue(paciente.jaTomouDose("vac-hepb", 2));
        assertFalse(paciente.jaTomouDose("vac-hepb", 3));
    }

    @Test
    @DisplayName("expoe os objetos aninhados de contato e endereco")
    void objetosAninhados() {
        Paciente paciente = TestFixtures.adulta();
        paciente.setId("novo-id");

        assertEquals("novo-id", paciente.getId());
        assertEquals("12345678901", paciente.getCpf());
        assertEquals("Maria Souza", paciente.getNome());
        assertEquals(LocalDate.of(1990, 5, 12), paciente.getDataNascimento());
        assertTrue(paciente.getContato().possuiEmail());
        assertEquals("44999990001", paciente.getContato().getTelefone());
        assertEquals("Maringa", paciente.getEndereco().getCidade());
        assertEquals("PR", paciente.getEndereco().getUf());
        assertEquals("87050-900", paciente.getEndereco().getCep());
        assertEquals("Jardim Aclimacao", paciente.getEndereco().getBairro());
        assertEquals("1610", paciente.getEndereco().getNumero());
        assertTrue(paciente.getEndereco().resumo().contains("Av. Guedner"));
    }

    @Test
    @DisplayName("contato sem email e reconhecido")
    void contatoSemEmail() {
        assertFalse(new Contato("4499", null).possuiEmail());
        assertFalse(new Contato("4499", "   ").possuiEmail());
    }

    @Test
    @DisplayName("dose guarda os dados de rastreio da aplicacao")
    void dadosDaDose() {
        DoseAplicada dose = new DoseAplicada("vac-flu", "Influenza", 1,
                LocalDate.of(2026, 4, 2), "L-99", "posto-7", "UBS Zona 7", "camp-1");

        assertEquals("vac-flu", dose.getVacinaId());
        assertEquals("Influenza", dose.getNomeVacina());
        assertEquals(1, dose.getNumeroDose());
        assertEquals("L-99", dose.getLote());
        assertEquals("posto-7", dose.getPostoSaudeId());
        assertEquals("UBS Zona 7", dose.getNomePostoSaude());
        assertEquals("camp-1", dose.getCampanhaId());
        assertTrue(dose.referenteA("vac-flu"));
        assertFalse(dose.referenteA("vac-hepb"));
        assertTrue(dose.vinculadaACampanha());
        assertFalse(TestFixtures.dose("vac-flu", 1, TestFixtures.HOJE).vinculadaACampanha());
    }

    @Test
    @DisplayName("dose exige vacina e data")
    void doseExigeCamposObrigatorios() {
        assertThrows(NullPointerException.class,
                () -> new DoseAplicada(null, "X", 1, TestFixtures.HOJE, "L", "posto-1", "UBS", null));
        assertThrows(NullPointerException.class,
                () -> new DoseAplicada("vac", "X", 1, null, "L", "posto-1", "UBS", null));
    }
}
