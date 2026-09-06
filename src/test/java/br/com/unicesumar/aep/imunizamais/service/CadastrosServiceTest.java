package br.com.unicesumar.aep.imunizamais.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.unicesumar.aep.imunizamais.TestFixtures;
import br.com.unicesumar.aep.imunizamais.domain.Campanha;
import br.com.unicesumar.aep.imunizamais.domain.Paciente;
import br.com.unicesumar.aep.imunizamais.domain.PostoSaude;
import br.com.unicesumar.aep.imunizamais.domain.Vacina;
import br.com.unicesumar.aep.imunizamais.exception.RecursoNaoEncontradoException;
import br.com.unicesumar.aep.imunizamais.exception.RegraNegocioException;
import br.com.unicesumar.aep.imunizamais.repository.CampanhaRepository;
import br.com.unicesumar.aep.imunizamais.repository.PacienteRepository;
import br.com.unicesumar.aep.imunizamais.repository.PostoSaudeRepository;
import br.com.unicesumar.aep.imunizamais.repository.VacinaRepository;
import br.com.unicesumar.aep.imunizamais.web.dto.CoberturaCampanhaResponse;
import br.com.unicesumar.aep.imunizamais.web.dto.ContatoDTO;
import br.com.unicesumar.aep.imunizamais.web.dto.EnderecoDTO;
import br.com.unicesumar.aep.imunizamais.web.dto.NovaCampanhaRequest;
import br.com.unicesumar.aep.imunizamais.web.dto.NovaVacinaRequest;
import br.com.unicesumar.aep.imunizamais.web.dto.NovoPacienteRequest;
import br.com.unicesumar.aep.imunizamais.web.dto.NovoPostoSaudeRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Servicos de cadastro")
class CadastrosServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;
    @Mock
    private VacinaRepository vacinaRepository;
    @Mock
    private CampanhaRepository campanhaRepository;
    @Mock
    private PostoSaudeRepository postoSaudeRepository;

    @Nested
    @DisplayName("PacienteService")
    class Pacientes {

        private PacienteService servico;

        @BeforeEach
        void setUp() {
            servico = new PacienteService(pacienteRepository);
        }

        private NovoPacienteRequest requisicao() {
            return new NovoPacienteRequest("12345678901", "Maria Souza", LocalDate.of(1990, 5, 12),
                    new ContatoDTO("44999990001", "maria@exemplo.com"),
                    new EnderecoDTO("Av. Guedner", "1610", "Jardim Aclimacao", "Maringa", "PR", "87050-900"));
        }

        @Test
        @DisplayName("cadastra paciente novo montando os objetos aninhados")
        void cadastra() {
            when(pacienteRepository.existsByCpf("12345678901")).thenReturn(false);
            when(pacienteRepository.save(any(Paciente.class)))
                    .thenAnswer(i -> i.getArgument(0));

            Paciente salvo = servico.cadastrar(requisicao());

            assertEquals("Maria Souza", salvo.getNome());
            assertEquals("Maringa", salvo.getEndereco().getCidade());
            assertEquals("maria@exemplo.com", salvo.getContato().getEmail());
            assertTrue(salvo.getHistoricoDoses().isEmpty());
        }

        @Test
        @DisplayName("recusa CPF ja cadastrado")
        void cpfDuplicado() {
            when(pacienteRepository.existsByCpf("12345678901")).thenReturn(true);
            RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                    () -> servico.cadastrar(requisicao()));
            assertEquals("PACIENTE_DUPLICADO", ex.getRegra());
            verify(pacienteRepository, never()).save(any(Paciente.class));
        }

        @Test
        @DisplayName("busca por CPF e lanca quando nao encontra")
        void buscaPorCpf() {
            Paciente paciente = TestFixtures.adulta();
            when(pacienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(paciente));
            assertEquals(paciente, servico.buscarPorCpf("12345678901"));

            when(pacienteRepository.findByCpf("00000000000")).thenReturn(Optional.empty());
            RecursoNaoEncontradoException ex = assertThrows(RecursoNaoEncontradoException.class,
                    () -> servico.buscarPorCpf("00000000000"));
            assertTrue(ex.getMessage().contains("Paciente"));
        }

        @Test
        @DisplayName("lista todos e filtra por cidade")
        void listagens() {
            when(pacienteRepository.findAll()).thenReturn(List.of(TestFixtures.adulta()));
            when(pacienteRepository.findByEnderecoCidadeIgnoreCase("maringa"))
                    .thenReturn(List.of(TestFixtures.adulta()));

            assertEquals(1, servico.listar().size());
            assertEquals(1, servico.listarPorCidade("maringa").size());
        }
    }

    @Nested
    @DisplayName("VacinaService")
    class Vacinas {

        private VacinaService servico;

        @BeforeEach
        void setUp() {
            servico = new VacinaService(vacinaRepository);
        }

        private NovaVacinaRequest requisicao() {
            return new NovaVacinaRequest("Hepatite B", "Fiocruz", 3, 30, 0, List.of("Hepatite B"));
        }

        @Test
        @DisplayName("cadastra vacina nova")
        void cadastra() {
            when(vacinaRepository.existsByNomeIgnoreCase("Hepatite B")).thenReturn(false);
            when(vacinaRepository.save(any(Vacina.class))).thenAnswer(i -> i.getArgument(0));

            Vacina salva = servico.cadastrar(requisicao());

            assertEquals("Hepatite B", salva.getNome());
            assertEquals(3, salva.getDosesRecomendadas());
            assertTrue(salva.exigeReforco());
        }

        @Test
        @DisplayName("recusa vacina com nome repetido")
        void nomeDuplicado() {
            when(vacinaRepository.existsByNomeIgnoreCase("Hepatite B")).thenReturn(true);
            RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                    () -> servico.cadastrar(requisicao()));
            assertEquals("VACINA_DUPLICADA", ex.getRegra());
        }

        @Test
        @DisplayName("lista e busca por id")
        void listaEBusca() {
            when(vacinaRepository.findAll()).thenReturn(List.of(TestFixtures.hepatiteB()));
            assertEquals(1, servico.listar().size());

            when(vacinaRepository.findById("vac-hepb")).thenReturn(Optional.of(TestFixtures.hepatiteB()));
            assertEquals("Hepatite B", servico.buscarPorId("vac-hepb").getNome());

            when(vacinaRepository.findById("x")).thenReturn(Optional.empty());
            assertThrows(RecursoNaoEncontradoException.class, () -> servico.buscarPorId("x"));
        }
    }

    @Nested
    @DisplayName("CampanhaService")
    class Campanhas {

        private CampanhaService servico;

        @BeforeEach
        void setUp() {
            servico = new CampanhaService(campanhaRepository, vacinaRepository);
        }

        private NovaCampanhaRequest requisicao(LocalDate inicio, LocalDate fim, int idadeMin, int idadeMax) {
            return new NovaCampanhaRequest("Campanha de Influenza 2026", "vac-flu",
                    idadeMin, idadeMax, "Criancas e adultos", inicio, fim, 500);
        }

        @Test
        @DisplayName("cadastra campanha valida")
        void cadastra() {
            when(vacinaRepository.existsById("vac-flu")).thenReturn(true);
            when(campanhaRepository.save(any(Campanha.class))).thenAnswer(i -> i.getArgument(0));

            Campanha salva = servico.cadastrar(
                    requisicao(TestFixtures.HOJE, TestFixtures.HOJE.plusDays(30), 6, 1200));

            assertEquals("vac-flu", salva.getVacinaId());
            assertEquals(500, salva.getMetaDoses());
            assertTrue(salva.isAtiva());
            assertEquals(0, salva.getDosesAplicadas());
        }

        @Test
        @DisplayName("recusa campanha de vacina inexistente")
        void vacinaInexistente() {
            when(vacinaRepository.existsById("vac-flu")).thenReturn(false);
            assertThrows(RecursoNaoEncontradoException.class, () -> servico.cadastrar(
                    requisicao(TestFixtures.HOJE, TestFixtures.HOJE.plusDays(30), 6, 1200)));
        }

        @Test
        @DisplayName("recusa periodo invertido")
        void periodoInvalido() {
            when(vacinaRepository.existsById("vac-flu")).thenReturn(true);
            RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> servico.cadastrar(
                    requisicao(TestFixtures.HOJE, TestFixtures.HOJE.minusDays(1), 6, 1200)));
            assertEquals("PERIODO_INVALIDO", ex.getRegra());
        }

        @Test
        @DisplayName("recusa publico-alvo invertido")
        void publicoAlvoInvalido() {
            when(vacinaRepository.existsById("vac-flu")).thenReturn(true);
            RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> servico.cadastrar(
                    requisicao(TestFixtures.HOJE, TestFixtures.HOJE.plusDays(30), 60, 12)));
            assertEquals("PUBLICO_ALVO_INVALIDO", ex.getRegra());
        }

        @Test
        @DisplayName("calcula o indicador de cobertura")
        void cobertura() {
            Campanha campanha = TestFixtures.campanhaVigente("vac-flu");
            campanha.registrarDose();
            when(campanhaRepository.findById("camp-1")).thenReturn(Optional.of(campanha));

            CoberturaCampanhaResponse resposta = servico.cobertura("camp-1");

            assertEquals("camp-1", resposta.campanhaId());
            assertEquals(500, resposta.metaDoses());
            assertEquals(1, resposta.dosesAplicadas());
            assertEquals(0.2d, resposta.percentualCobertura());
            assertFalse(resposta.metaAtingida());
            assertTrue(resposta.ativa());
        }

        @Test
        @DisplayName("encerra campanha e lista ativas")
        void encerraELista() {
            Campanha campanha = TestFixtures.campanhaVigente("vac-flu");
            when(campanhaRepository.findById("camp-1")).thenReturn(Optional.of(campanha));
            when(campanhaRepository.save(any(Campanha.class))).thenAnswer(i -> i.getArgument(0));

            assertFalse(servico.encerrar("camp-1").isAtiva());

            when(campanhaRepository.findAll()).thenReturn(List.of(campanha));
            when(campanhaRepository.findByAtivaTrue()).thenReturn(List.of());
            assertEquals(1, servico.listar().size());
            assertEquals(0, servico.listarAtivas().size());
        }

        @Test
        @DisplayName("busca campanha inexistente lanca excecao")
        void buscaInexistente() {
            when(campanhaRepository.findById("x")).thenReturn(Optional.empty());
            assertThrows(RecursoNaoEncontradoException.class, () -> servico.buscarPorId("x"));
        }
    }

    @Nested
    @DisplayName("PostoSaudeService")
    class PostosSaude {

        private PostoSaudeService servico;

        @BeforeEach
        void setUp() {
            servico = new PostoSaudeService(postoSaudeRepository);
        }

        private NovoPostoSaudeRequest requisicao() {
            return new NovoPostoSaudeRequest("UBS Central", "44898887777", 150,
                    new EnderecoDTO("Av. Brasil", "500", "Centro", "Maringa", "PR", "87013-000"));
        }

        @Test
        @DisplayName("cadastra posto de saude novo")
        void cadastra() {
            when(postoSaudeRepository.existsByNomeIgnoreCase("UBS Central")).thenReturn(false);
            when(postoSaudeRepository.save(any(PostoSaude.class))).thenAnswer(i -> i.getArgument(0));

            PostoSaude salvo = servico.cadastrar(requisicao());

            assertEquals("UBS Central", salvo.getNome());
            assertEquals("Maringa", salvo.getEndereco().getCidade());
            assertTrue(salvo.isAtivo());
        }

        @Test
        @DisplayName("recusa nome repetido")
        void nomeDuplicado() {
            when(postoSaudeRepository.existsByNomeIgnoreCase("UBS Central")).thenReturn(true);
            RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                    () -> servico.cadastrar(requisicao()));
            assertEquals("POSTO_DUPLICADO", ex.getRegra());
        }

        @Test
        @DisplayName("lista, busca e desativa")
        void listaBuscaEDesativa() {
            PostoSaude posto = TestFixtures.postoSaude();
            when(postoSaudeRepository.findAll()).thenReturn(List.of(posto));
            when(postoSaudeRepository.findByAtivoTrue()).thenReturn(List.of(posto));
            assertEquals(1, servico.listar().size());
            assertEquals(1, servico.listarAtivos().size());

            when(postoSaudeRepository.findById("posto-1")).thenReturn(Optional.of(posto));
            assertEquals("UBS Central", servico.buscarPorId("posto-1").getNome());

            when(postoSaudeRepository.save(any(PostoSaude.class))).thenAnswer(i -> i.getArgument(0));
            assertFalse(servico.desativar("posto-1").isAtivo());

            when(postoSaudeRepository.findById("x")).thenReturn(Optional.empty());
            assertThrows(RecursoNaoEncontradoException.class, () -> servico.buscarPorId("x"));
        }
    }
}
