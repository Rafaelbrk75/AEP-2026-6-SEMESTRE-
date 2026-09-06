package br.com.unicesumar.aep.imunizamais.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.unicesumar.aep.imunizamais.TestFixtures;
import br.com.unicesumar.aep.imunizamais.domain.Campanha;
import br.com.unicesumar.aep.imunizamais.domain.Paciente;
import br.com.unicesumar.aep.imunizamais.domain.SituacaoPaciente;
import br.com.unicesumar.aep.imunizamais.domain.SituacaoVacinal;
import br.com.unicesumar.aep.imunizamais.exception.RecursoNaoEncontradoException;
import br.com.unicesumar.aep.imunizamais.exception.RegraNegocioException;
import br.com.unicesumar.aep.imunizamais.service.CampanhaService;
import br.com.unicesumar.aep.imunizamais.service.PacienteService;
import br.com.unicesumar.aep.imunizamais.service.VacinaService;
import br.com.unicesumar.aep.imunizamais.service.VacinacaoService;
import br.com.unicesumar.aep.imunizamais.web.dto.AplicacaoDoseRequest;
import br.com.unicesumar.aep.imunizamais.web.dto.CoberturaCampanhaResponse;
import br.com.unicesumar.aep.imunizamais.web.dto.ContatoDTO;
import br.com.unicesumar.aep.imunizamais.web.dto.EnderecoDTO;
import br.com.unicesumar.aep.imunizamais.web.dto.NovaCampanhaRequest;
import br.com.unicesumar.aep.imunizamais.web.dto.NovaVacinaRequest;
import br.com.unicesumar.aep.imunizamais.web.dto.NovoPacienteRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Testes da camada REST usando MockMvc em modo standalone (sem subir o contexto do Spring
 * nem depender de um MongoDB em execucao).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("API REST")
class ApiRestTest {

    @Mock
    private PacienteService pacienteService;
    @Mock
    private VacinacaoService vacinacaoService;
    @Mock
    private VacinaService vacinaService;
    @Mock
    private CampanhaService campanhaService;

    private MockMvc mvcPacientes;
    private MockMvc mvcVacinas;
    private MockMvc mvcCampanhas;
    private ObjectMapper json;

    @BeforeEach
    void setUp() {
        json = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mvcPacientes = construir(new PacienteController(pacienteService, vacinacaoService), validator);
        mvcVacinas = construir(new VacinaController(vacinaService), validator);
        mvcCampanhas = construir(new CampanhaController(campanhaService), validator);
    }

    private MockMvc construir(Object controller, LocalValidatorFactoryBean validator) {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(json))
                .setValidator(validator)
                .build();
    }

    private NovoPacienteRequest novoPaciente() {
        return new NovoPacienteRequest("12345678901", "Maria Souza", LocalDate.of(1990, 5, 12),
                new ContatoDTO("44999990001", "maria@exemplo.com"),
                new EnderecoDTO("Av. Guedner", "1610", "Jardim Aclimacao", "Maringa", "PR", "87050-900"));
    }

    @Test
    @DisplayName("POST /api/pacientes cria o paciente e devolve 201")
    void cadastrarPaciente() throws Exception {
        when(pacienteService.cadastrar(any())).thenReturn(TestFixtures.adulta());

        mvcPacientes.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(novoPaciente())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.endereco.cidade").value("Maringa"));
    }

    @Test
    @DisplayName("POST /api/pacientes com payload invalido devolve 400 e os detalhes")
    void cadastrarPacienteInvalido() throws Exception {
        NovoPacienteRequest invalido = new NovoPacienteRequest("123", "", LocalDate.of(1990, 5, 12),
                new ContatoDTO("44999990001", "email-invalido"),
                new EnderecoDTO("Av. Guedner", "1610", "Centro", "Maringa", "PARANA", "87050-900"));

        mvcPacientes.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("PAYLOAD_INVALIDO"))
                .andExpect(jsonPath("$.detalhes").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/pacientes lista e filtra por cidade")
    void listarPacientes() throws Exception {
        when(pacienteService.listar()).thenReturn(List.of(TestFixtures.adulta()));
        when(pacienteService.listarPorCidade("Maringa")).thenReturn(List.of(TestFixtures.adulta()));

        mvcPacientes.perform(get("/api/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mvcPacientes.perform(get("/api/pacientes").param("cidade", "Maringa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Maria Souza"));
    }

    @Test
    @DisplayName("GET /api/pacientes/{cpf} inexistente devolve 404")
    void pacienteInexistente() throws Exception {
        when(pacienteService.buscarPorCpf("00000000000"))
                .thenThrow(RecursoNaoEncontradoException.de("Paciente", "00000000000"));

        mvcPacientes.perform(get("/api/pacientes/00000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("RECURSO_NAO_ENCONTRADO"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/pacientes/{cpf} devolve o paciente")
    void buscarPaciente() throws Exception {
        when(pacienteService.buscarPorCpf("12345678901")).thenReturn(TestFixtures.adulta());

        mvcPacientes.perform(get("/api/pacientes/12345678901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Souza"));
    }

    @Test
    @DisplayName("POST /api/pacientes/{cpf}/doses registra a aplicacao")
    void aplicarDose() throws Exception {
        Paciente paciente = TestFixtures.adulta();
        paciente.registrarDose(TestFixtures.dose("vac-hepb", 1, TestFixtures.HOJE));
        when(vacinacaoService.registrarAplicacao(eq("12345678901"), any())).thenReturn(paciente);

        AplicacaoDoseRequest request = new AplicacaoDoseRequest("vac-hepb", null,
                TestFixtures.HOJE, "LOTE-A", "UBS Central");

        mvcPacientes.perform(post("/api/pacientes/12345678901/doses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.historicoDoses.length()").value(1))
                .andExpect(jsonPath("$.historicoDoses[0].numeroDose").value(1));
    }

    @Test
    @DisplayName("violacao de regra de negocio devolve 422 com o codigo da regra")
    void regraDeNegocioViolada() throws Exception {
        when(vacinacaoService.registrarAplicacao(eq("12345678901"), any()))
                .thenThrow(new RegraNegocioException("IDADE_MINIMA",
                        "Paciente tem 3 mes(es) e a vacina Triplice Viral exige no minimo 12 mes(es)"));

        AplicacaoDoseRequest request = new AplicacaoDoseRequest("vac-tv", null,
                TestFixtures.HOJE, "LOTE-A", "UBS Central");

        mvcPacientes.perform(post("/api/pacientes/12345678901/doses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro").value("IDADE_MINIMA"))
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("GET /api/pacientes/{cpf}/situacao devolve a situacao vacinal")
    void situacaoVacinal() throws Exception {
        when(vacinacaoService.consultarSituacao("12345678901")).thenReturn(List.of(
                new SituacaoPaciente("vac-hepb", "Hepatite B", 1, 3,
                        SituacaoVacinal.ATRASADA, 2, TestFixtures.HOJE.minusDays(5))));

        mvcPacientes.perform(get("/api/pacientes/12345678901/situacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].situacao").value("ATRASADA"))
                .andExpect(jsonPath("$[0].proximaDose").value(2))
                .andExpect(jsonPath("$[0].dosesRecomendadas").value(3));
    }

    @Test
    @DisplayName("POST e GET /api/vacinas")
    void vacinas() throws Exception {
        when(vacinaService.cadastrar(any())).thenReturn(TestFixtures.hepatiteB());
        when(vacinaService.listar()).thenReturn(List.of(TestFixtures.hepatiteB()));
        when(vacinaService.buscarPorId("vac-hepb")).thenReturn(TestFixtures.hepatiteB());

        NovaVacinaRequest request = new NovaVacinaRequest("Hepatite B", "Fiocruz", 3, 30, 0,
                List.of("Hepatite B"));

        mvcVacinas.perform(post("/api/vacinas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Hepatite B"))
                .andExpect(jsonPath("$.dosesRecomendadas").value(3));

        mvcVacinas.perform(get("/api/vacinas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mvcVacinas.perform(get("/api/vacinas/vac-hepb"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fabricante").value("Fiocruz"));
    }

    @Test
    @DisplayName("POST /api/vacinas rejeita esquema com zero doses")
    void vacinaInvalida() throws Exception {
        NovaVacinaRequest request = new NovaVacinaRequest("", "Fiocruz", 0, 30, 0, List.of());

        mvcVacinas.perform(post("/api/vacinas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("PAYLOAD_INVALIDO"));
    }

    @Test
    @DisplayName("campanhas: cadastro, listagem, cobertura e encerramento")
    void campanhas() throws Exception {
        Campanha campanha = TestFixtures.campanhaVigente("vac-flu");
        when(campanhaService.cadastrar(any())).thenReturn(campanha);
        when(campanhaService.listar()).thenReturn(List.of(campanha));
        when(campanhaService.listarAtivas()).thenReturn(List.of(campanha));
        when(campanhaService.buscarPorId("camp-1")).thenReturn(campanha);
        when(campanhaService.cobertura("camp-1")).thenReturn(
                new CoberturaCampanhaResponse("camp-1", campanha.getNome(), 500, 125, 25d, false, true));
        when(campanhaService.encerrar("camp-1")).thenReturn(campanha);

        NovaCampanhaRequest request = new NovaCampanhaRequest("Campanha de Influenza 2026", "vac-flu",
                6, 1200, "Criancas e adultos", TestFixtures.HOJE, TestFixtures.HOJE.plusDays(30), 500);

        mvcCampanhas.perform(post("/api/campanhas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Campanha de Influenza 2026"))
                .andExpect(jsonPath("$.publicoAlvo.idadeMinimaMeses").value(6));

        mvcCampanhas.perform(get("/api/campanhas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mvcCampanhas.perform(get("/api/campanhas").param("apenasAtivas", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mvcCampanhas.perform(get("/api/campanhas/camp-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vacinaId").value("vac-flu"));

        mvcCampanhas.perform(get("/api/campanhas/camp-1/cobertura"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentualCobertura").value(25.0))
                .andExpect(jsonPath("$.dosesAplicadas").value(125));

        mvcCampanhas.perform(patch("/api/campanhas/camp-1/encerramento"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("campanha inexistente devolve 404")
    void campanhaInexistente() throws Exception {
        when(campanhaService.buscarPorId("nao-existe"))
                .thenThrow(RecursoNaoEncontradoException.de("Campanha", "nao-existe"));

        mvcCampanhas.perform(get("/api/campanhas/nao-existe"))
                .andExpect(status().isNotFound());
    }
}
