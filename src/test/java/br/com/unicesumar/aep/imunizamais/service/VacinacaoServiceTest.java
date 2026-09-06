package br.com.unicesumar.aep.imunizamais.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.unicesumar.aep.imunizamais.TestFixtures;
import br.com.unicesumar.aep.imunizamais.domain.Campanha;
import br.com.unicesumar.aep.imunizamais.domain.DoseAplicada;
import br.com.unicesumar.aep.imunizamais.domain.Paciente;
import br.com.unicesumar.aep.imunizamais.domain.PostoSaude;
import br.com.unicesumar.aep.imunizamais.domain.SituacaoPaciente;
import br.com.unicesumar.aep.imunizamais.domain.SituacaoVacinal;
import br.com.unicesumar.aep.imunizamais.domain.regra.CampanhaVigenteRegra;
import br.com.unicesumar.aep.imunizamais.domain.regra.DataAplicacaoRegra;
import br.com.unicesumar.aep.imunizamais.domain.regra.EsquemaCompletoRegra;
import br.com.unicesumar.aep.imunizamais.domain.regra.IdadeMinimaRegra;
import br.com.unicesumar.aep.imunizamais.domain.regra.IntervaloEntreDosesRegra;
import br.com.unicesumar.aep.imunizamais.domain.regra.RegraAplicacaoDose;
import br.com.unicesumar.aep.imunizamais.exception.RecursoNaoEncontradoException;
import br.com.unicesumar.aep.imunizamais.exception.RegraNegocioException;
import br.com.unicesumar.aep.imunizamais.repository.CampanhaRepository;
import br.com.unicesumar.aep.imunizamais.repository.PacienteRepository;
import br.com.unicesumar.aep.imunizamais.repository.PostoSaudeRepository;
import br.com.unicesumar.aep.imunizamais.repository.VacinaRepository;
import br.com.unicesumar.aep.imunizamais.web.dto.AlertaVacinacaoResponse;
import br.com.unicesumar.aep.imunizamais.web.dto.AplicacaoDoseRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("VacinacaoService")
class VacinacaoServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;
    @Mock
    private VacinaRepository vacinaRepository;
    @Mock
    private CampanhaRepository campanhaRepository;
    @Mock
    private PostoSaudeRepository postoSaudeRepository;

    private VacinacaoService servico;

    private final List<RegraAplicacaoDose> regras = List.of(
            new DataAplicacaoRegra(TestFixtures.relogioFixo()),
            new IdadeMinimaRegra(),
            new EsquemaCompletoRegra(),
            new IntervaloEntreDosesRegra(),
            new CampanhaVigenteRegra());

    @BeforeEach
    void setUp() {
        servico = new VacinacaoService(pacienteRepository, vacinaRepository, campanhaRepository,
                postoSaudeRepository, regras, TestFixtures.relogioFixo());
        when(pacienteRepository.save(any(Paciente.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
        when(campanhaRepository.save(any(Campanha.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
        when(postoSaudeRepository.findById("posto-1")).thenReturn(Optional.of(TestFixtures.postoSaude()));
    }

    private AplicacaoDoseRequest requisicao(String vacinaId, String campanhaId) {
        return new AplicacaoDoseRequest(vacinaId, campanhaId, TestFixtures.HOJE, "LOTE-A", "posto-1");
    }

    @Test
    @DisplayName("registra a primeira dose fora de campanha")
    void registraPrimeiraDose() {
        Paciente paciente = TestFixtures.adulta();
        when(pacienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(paciente));
        when(vacinaRepository.findById("vac-hepb")).thenReturn(Optional.of(TestFixtures.hepatiteB()));

        Paciente atualizado = servico.registrarAplicacao("12345678901", requisicao("vac-hepb", null));

        assertEquals(1, atualizado.totalDosesAplicadas());
        DoseAplicada dose = atualizado.getHistoricoDoses().get(0);
        assertEquals(1, dose.getNumeroDose());
        assertEquals("Hepatite B", dose.getNomeVacina());
        assertEquals("LOTE-A", dose.getLote());
        assertEquals("posto-1", dose.getPostoSaudeId());
        assertEquals("UBS Central", dose.getNomePostoSaude());
        assertNull(dose.getCampanhaId());
        verify(pacienteRepository).save(paciente);
        verify(campanhaRepository, never()).save(any(Campanha.class));
    }

    @Test
    @DisplayName("registra dose vinculada a campanha e incrementa a cobertura")
    void registraDoseEmCampanha() {
        Paciente paciente = TestFixtures.adulta();
        Campanha campanha = TestFixtures.campanhaVigente("vac-flu");
        when(pacienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(paciente));
        when(vacinaRepository.findById("vac-flu")).thenReturn(Optional.of(TestFixtures.influenza()));
        when(campanhaRepository.findById("camp-1")).thenReturn(Optional.of(campanha));

        Paciente atualizado = servico.registrarAplicacao("12345678901", requisicao("vac-flu", "camp-1"));

        assertEquals("camp-1", atualizado.getHistoricoDoses().get(0).getCampanhaId());
        assertEquals(1, campanha.getDosesAplicadas());
        verify(campanhaRepository).save(campanha);
    }

    @Test
    @DisplayName("calcula o numero da dose a partir do historico")
    void numeroSequencialDaDose() {
        Paciente paciente = TestFixtures.adulta();
        paciente.registrarDose(TestFixtures.dose("vac-hepb", 1, TestFixtures.HOJE.minusDays(60)));
        when(pacienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(paciente));
        when(vacinaRepository.findById("vac-hepb")).thenReturn(Optional.of(TestFixtures.hepatiteB()));

        Paciente atualizado = servico.registrarAplicacao("12345678901", requisicao("vac-hepb", null));

        assertEquals(2, atualizado.dosesDaVacina("vac-hepb").get(1).getNumeroDose());
    }

    @Test
    @DisplayName("falha quando o paciente nao existe")
    void pacienteInexistente() {
        when(pacienteRepository.findByCpf("00000000000")).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.registrarAplicacao("00000000000", requisicao("vac-hepb", null)));
    }

    @Test
    @DisplayName("falha quando a vacina nao existe")
    void vacinaInexistente() {
        when(pacienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(TestFixtures.adulta()));
        when(vacinaRepository.findById("inexistente")).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.registrarAplicacao("12345678901", requisicao("inexistente", null)));
    }

    @Test
    @DisplayName("falha quando a campanha informada nao existe")
    void campanhaInexistente() {
        when(pacienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(TestFixtures.adulta()));
        when(vacinaRepository.findById("vac-flu")).thenReturn(Optional.of(TestFixtures.influenza()));
        when(campanhaRepository.findById("nao-existe")).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.registrarAplicacao("12345678901", requisicao("vac-flu", "nao-existe")));
    }

    @Test
    @DisplayName("propaga a violacao de regra e nao persiste nada")
    void regraViolada() {
        Paciente bebe = TestFixtures.bebeCom3Meses();
        when(pacienteRepository.findByCpf("98765432100")).thenReturn(Optional.of(bebe));
        when(vacinaRepository.findById("vac-tv")).thenReturn(Optional.of(TestFixtures.tripliceViral()));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> servico.registrarAplicacao("98765432100", requisicao("vac-tv", null)));

        assertEquals("IDADE_MINIMA", ex.getRegra());
        assertEquals(0, bebe.totalDosesAplicadas());
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    @DisplayName("classifica situacao pendente, em dia, atrasada e completa")
    void situacaoVacinal() {
        Paciente paciente = TestFixtures.adulta();
        // Hepatite B: 1 de 3 doses, aplicada ha 10 dias (intervalo de 30) -> EM_DIA
        paciente.registrarDose(TestFixtures.dose("vac-hepb", 1, TestFixtures.HOJE.minusDays(10)));
        // Influenza: dose unica aplicada -> COMPLETO
        paciente.registrarDose(TestFixtures.dose("vac-flu", 1, TestFixtures.HOJE.minusDays(200)));
        // Triplice viral: 1 de 2 doses, aplicada ha 200 dias (intervalo de 90) -> ATRASADA
        paciente.registrarDose(TestFixtures.dose("vac-tv", 1, TestFixtures.HOJE.minusDays(200)));

        when(pacienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(paciente));
        when(vacinaRepository.findAll()).thenReturn(List.of(
                TestFixtures.hepatiteB(), TestFixtures.influenza(), TestFixtures.tripliceViral()));

        List<SituacaoPaciente> situacoes = servico.consultarSituacao("12345678901");

        assertEquals(3, situacoes.size());
        SituacaoPaciente hepB = situacoes.get(0);
        assertEquals(SituacaoVacinal.EM_DIA, hepB.getSituacao());
        assertEquals(2, hepB.getProximaDose());
        assertEquals(TestFixtures.HOJE.minusDays(10).plusDays(30), hepB.getDataPrevistaProximaDose());

        assertEquals(SituacaoVacinal.COMPLETO, situacoes.get(1).getSituacao());
        assertNull(situacoes.get(1).getProximaDose());

        SituacaoPaciente tv = situacoes.get(2);
        assertEquals(SituacaoVacinal.ATRASADA, tv.getSituacao());
        assertTrue(tv.exigeAcao());
    }

    @Test
    @DisplayName("classifica pendente e nao elegivel para quem ainda nao tomou nenhuma dose")
    void situacaoSemDoses() {
        Paciente bebe = TestFixtures.bebeCom3Meses();
        when(pacienteRepository.findByCpf("98765432100")).thenReturn(Optional.of(bebe));
        when(vacinaRepository.findAll()).thenReturn(List.of(
                TestFixtures.hepatiteB(), TestFixtures.tripliceViral()));

        List<SituacaoPaciente> situacoes = servico.consultarSituacao("98765432100");

        SituacaoPaciente hepB = situacoes.get(0);
        assertEquals(SituacaoVacinal.PENDENTE, hepB.getSituacao());
        assertEquals(TestFixtures.HOJE, hepB.getDataPrevistaProximaDose());
        assertTrue(hepB.exigeAcao());

        SituacaoPaciente tv = situacoes.get(1);
        assertEquals(SituacaoVacinal.NAO_ELEGIVEL, tv.getSituacao());
        assertEquals(bebe.getDataNascimento().plusMonths(12), tv.getDataPrevistaProximaDose());
    }

    @Test
    @DisplayName("consulta de situacao falha para paciente inexistente")
    void situacaoPacienteInexistente() {
        when(pacienteRepository.findByCpf("00000000000")).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.consultarSituacao("00000000000"));
    }

    @Test
    @DisplayName("construtor de producao usa o relogio do sistema")
    void construtorDeProducao() {
        assertNotNull(new VacinacaoService(pacienteRepository, vacinaRepository,
                campanhaRepository, postoSaudeRepository, regras));
    }

    @Test
    @DisplayName("falha quando o posto de saude informado nao existe")
    void postoInexistente() {
        when(pacienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(TestFixtures.adulta()));
        when(vacinaRepository.findById("vac-hepb")).thenReturn(Optional.of(TestFixtures.hepatiteB()));
        when(postoSaudeRepository.findById("posto-x")).thenReturn(Optional.empty());

        AplicacaoDoseRequest request = new AplicacaoDoseRequest("vac-hepb", null,
                TestFixtures.HOJE, "LOTE-A", "posto-x");

        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.registrarAplicacao("12345678901", request));
    }

    @Test
    @DisplayName("falha quando o posto de saude esta inativo")
    void postoInativo() {
        PostoSaude inativo = TestFixtures.postoSaude();
        inativo.desativar();
        when(pacienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(TestFixtures.adulta()));
        when(vacinaRepository.findById("vac-hepb")).thenReturn(Optional.of(TestFixtures.hepatiteB()));
        when(postoSaudeRepository.findById("posto-1")).thenReturn(Optional.of(inativo));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> servico.registrarAplicacao("12345678901", requisicao("vac-hepb", null)));
        assertEquals("POSTO_INATIVO", ex.getRegra());
    }

    @Test
    @DisplayName("lista alertas de pacientes pendentes, atrasados e com dose proxima")
    void listarAlertas() {
        Paciente paciente = TestFixtures.adulta();
        // Hepatite B: 1 de 3 doses, aplicada ha 200 dias (intervalo de 30) -> ATRASADA
        paciente.registrarDose(TestFixtures.dose("vac-hepb", 1, TestFixtures.HOJE.minusDays(200)));
        when(pacienteRepository.findAll()).thenReturn(List.of(paciente));
        when(vacinaRepository.findAll()).thenReturn(List.of(TestFixtures.hepatiteB()));

        List<AlertaVacinacaoResponse> alertas = servico.listarAlertas(null);

        assertEquals(1, alertas.size());
        assertEquals(SituacaoVacinal.ATRASADA, alertas.get(0).situacao());
        assertEquals("12345678901", alertas.get(0).cpf());
        assertFalse(alertas.get(0).proximaDoseEmBreve());
    }

    @Test
    @DisplayName("lista alertas filtrando por situacao e sinaliza proxima dose em breve")
    void listarAlertasComFiltroEProximaDoseEmBreve() {
        Paciente paciente = TestFixtures.adulta();
        // Hepatite B: 1 de 3 doses, aplicada ha 25 dias (intervalo de 30) -> EM_DIA,
        // mas a proxima dose vence em 5 dias, dentro da janela de alerta preventivo.
        paciente.registrarDose(TestFixtures.dose("vac-hepb", 1, TestFixtures.HOJE.minusDays(25)));
        when(pacienteRepository.findAll()).thenReturn(List.of(paciente));
        when(vacinaRepository.findAll()).thenReturn(List.of(TestFixtures.hepatiteB()));

        List<AlertaVacinacaoResponse> todos = servico.listarAlertas(null);
        assertEquals(1, todos.size());
        assertEquals(SituacaoVacinal.EM_DIA, todos.get(0).situacao());
        assertTrue(todos.get(0).proximaDoseEmBreve());

        assertTrue(servico.listarAlertas(SituacaoVacinal.ATRASADA).isEmpty());
    }
}
