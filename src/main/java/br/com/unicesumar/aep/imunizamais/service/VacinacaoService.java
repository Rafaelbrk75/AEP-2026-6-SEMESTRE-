package br.com.unicesumar.aep.imunizamais.service;

import br.com.unicesumar.aep.imunizamais.domain.Campanha;
import br.com.unicesumar.aep.imunizamais.domain.DoseAplicada;
import br.com.unicesumar.aep.imunizamais.domain.Paciente;
import br.com.unicesumar.aep.imunizamais.domain.SituacaoPaciente;
import br.com.unicesumar.aep.imunizamais.domain.SituacaoVacinal;
import br.com.unicesumar.aep.imunizamais.domain.Vacina;
import br.com.unicesumar.aep.imunizamais.domain.regra.ContextoAplicacao;
import br.com.unicesumar.aep.imunizamais.domain.regra.RegraAplicacaoDose;
import br.com.unicesumar.aep.imunizamais.exception.RecursoNaoEncontradoException;
import br.com.unicesumar.aep.imunizamais.repository.CampanhaRepository;
import br.com.unicesumar.aep.imunizamais.repository.PacienteRepository;
import br.com.unicesumar.aep.imunizamais.repository.VacinaRepository;
import br.com.unicesumar.aep.imunizamais.web.dto.AplicacaoDoseRequest;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Caso de uso central da PoC: registra a aplicacao de uma dose atravessando
 * todas as regras de negocio e atualiza as colecoes "pacientes" e "campanhas".
 */
@Service
public class VacinacaoService {

    private final PacienteRepository pacienteRepository;
    private final VacinaRepository vacinaRepository;
    private final CampanhaRepository campanhaRepository;
    private final List<RegraAplicacaoDose> regras;
    private final Clock clock;

    public VacinacaoService(PacienteRepository pacienteRepository,
                            VacinaRepository vacinaRepository,
                            CampanhaRepository campanhaRepository,
                            List<RegraAplicacaoDose> regras) {
        this(pacienteRepository, vacinaRepository, campanhaRepository, regras, Clock.systemDefaultZone());
    }

    public VacinacaoService(PacienteRepository pacienteRepository,
                            VacinaRepository vacinaRepository,
                            CampanhaRepository campanhaRepository,
                            List<RegraAplicacaoDose> regras,
                            Clock clock) {
        this.pacienteRepository = pacienteRepository;
        this.vacinaRepository = vacinaRepository;
        this.campanhaRepository = campanhaRepository;
        this.regras = regras;
        this.clock = clock;
    }

    /**
     * Registra uma dose no historico do paciente apos validar todas as regras.
     *
     * @return o paciente atualizado
     */
    public Paciente registrarAplicacao(String cpf, AplicacaoDoseRequest request) {
        Paciente paciente = pacienteRepository.findByCpf(cpf)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Paciente", cpf));

        Vacina vacina = vacinaRepository.findById(request.vacinaId())
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Vacina", request.vacinaId()));

        Campanha campanha = null;
        if (request.campanhaId() != null && !request.campanhaId().isBlank()) {
            campanha = campanhaRepository.findById(request.campanhaId())
                    .orElseThrow(() -> RecursoNaoEncontradoException.de("Campanha", request.campanhaId()));
        }

        int numeroDose = paciente.proximoNumeroDose(vacina.getId());
        ContextoAplicacao contexto = new ContextoAplicacao(paciente, vacina, campanha,
                request.dataAplicacao(), numeroDose);

        for (RegraAplicacaoDose regra : regras) {
            regra.validar(contexto);
        }

        DoseAplicada dose = new DoseAplicada(
                vacina.getId(),
                vacina.getNome(),
                numeroDose,
                request.dataAplicacao(),
                request.lote(),
                request.unidadeSaude(),
                campanha == null ? null : campanha.getId());

        paciente.registrarDose(dose);

        if (campanha != null) {
            campanha.registrarDose();
            campanhaRepository.save(campanha);
        }

        return pacienteRepository.save(paciente);
    }

    /**
     * Calcula a situacao vacinal do paciente para cada vacina do catalogo.
     */
    public List<SituacaoPaciente> consultarSituacao(String cpf) {
        Paciente paciente = pacienteRepository.findByCpf(cpf)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Paciente", cpf));

        LocalDate hoje = LocalDate.now(clock);
        List<SituacaoPaciente> situacoes = new ArrayList<>();

        for (Vacina vacina : vacinaRepository.findAll()) {
            situacoes.add(avaliar(paciente, vacina, hoje));
        }
        return situacoes;
    }

    private SituacaoPaciente avaliar(Paciente paciente, Vacina vacina, LocalDate hoje) {
        int aplicadas = paciente.dosesDaVacina(vacina.getId()).size();

        if (aplicadas >= vacina.getDosesRecomendadas()) {
            return new SituacaoPaciente(vacina.getId(), vacina.getNome(), aplicadas,
                    vacina.getDosesRecomendadas(), SituacaoVacinal.COMPLETO, null, null);
        }

        int proximaDose = aplicadas + 1;

        if (aplicadas == 0) {
            LocalDate elegivelEm = paciente.getDataNascimento().plusMonths(vacina.getIdadeMinimaMeses());
            if (paciente.idadeEmMesesEm(hoje) < vacina.getIdadeMinimaMeses()) {
                return new SituacaoPaciente(vacina.getId(), vacina.getNome(), 0,
                        vacina.getDosesRecomendadas(), SituacaoVacinal.NAO_ELEGIVEL, proximaDose, elegivelEm);
            }
            return new SituacaoPaciente(vacina.getId(), vacina.getNome(), 0,
                    vacina.getDosesRecomendadas(), SituacaoVacinal.PENDENTE, proximaDose, hoje);
        }

        Optional<DoseAplicada> ultima = paciente.ultimaDoseDe(vacina.getId());
        LocalDate prevista = ultima.get().getDataAplicacao().plusDays(vacina.getIntervaloDiasEntreDoses());
        SituacaoVacinal situacao = hoje.isAfter(prevista) ? SituacaoVacinal.ATRASADA : SituacaoVacinal.EM_DIA;

        return new SituacaoPaciente(vacina.getId(), vacina.getNome(), aplicadas,
                vacina.getDosesRecomendadas(), situacao, proximaDose, prevista);
    }
}
