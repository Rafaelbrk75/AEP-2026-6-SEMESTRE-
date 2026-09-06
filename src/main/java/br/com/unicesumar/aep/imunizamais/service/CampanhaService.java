package br.com.unicesumar.aep.imunizamais.service;

import br.com.unicesumar.aep.imunizamais.domain.Campanha;
import br.com.unicesumar.aep.imunizamais.domain.PublicoAlvo;
import br.com.unicesumar.aep.imunizamais.exception.RecursoNaoEncontradoException;
import br.com.unicesumar.aep.imunizamais.exception.RegraNegocioException;
import br.com.unicesumar.aep.imunizamais.repository.CampanhaRepository;
import br.com.unicesumar.aep.imunizamais.repository.VacinaRepository;
import br.com.unicesumar.aep.imunizamais.web.dto.CoberturaCampanhaResponse;
import br.com.unicesumar.aep.imunizamais.web.dto.NovaCampanhaRequest;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Casos de uso da colecao "campanhas", incluindo o indicador de cobertura.
 */
@Service
public class CampanhaService {

    private final CampanhaRepository repositorio;
    private final VacinaRepository vacinaRepository;

    public CampanhaService(CampanhaRepository repositorio, VacinaRepository vacinaRepository) {
        this.repositorio = repositorio;
        this.vacinaRepository = vacinaRepository;
    }

    public Campanha cadastrar(NovaCampanhaRequest request) {
        if (!vacinaRepository.existsById(request.vacinaId())) {
            throw RecursoNaoEncontradoException.de("Vacina", request.vacinaId());
        }
        if (request.dataFim().isBefore(request.dataInicio())) {
            throw new RegraNegocioException("PERIODO_INVALIDO",
                    "A data de fim da campanha nao pode ser anterior a data de inicio");
        }
        if (request.idadeMaximaMeses() < request.idadeMinimaMeses()) {
            throw new RegraNegocioException("PUBLICO_ALVO_INVALIDO",
                    "A idade maxima do publico-alvo nao pode ser menor que a idade minima");
        }

        PublicoAlvo publicoAlvo = new PublicoAlvo(request.idadeMinimaMeses(),
                request.idadeMaximaMeses(), request.descricaoPublicoAlvo());

        Campanha campanha = new Campanha(null, request.nome(), request.vacinaId(), publicoAlvo,
                request.dataInicio(), request.dataFim(), request.metaDoses());

        return repositorio.save(campanha);
    }

    public List<Campanha> listar() {
        return repositorio.findAll();
    }

    public List<Campanha> listarAtivas() {
        return repositorio.findByAtivaTrue();
    }

    public Campanha buscarPorId(String id) {
        return repositorio.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Campanha", id));
    }

    public CoberturaCampanhaResponse cobertura(String id) {
        Campanha campanha = buscarPorId(id);
        return new CoberturaCampanhaResponse(
                campanha.getId(),
                campanha.getNome(),
                campanha.getMetaDoses(),
                campanha.getDosesAplicadas(),
                campanha.percentualCobertura(),
                campanha.metaAtingida(),
                campanha.isAtiva());
    }

    public Campanha encerrar(String id) {
        Campanha campanha = buscarPorId(id);
        campanha.encerrar();
        return repositorio.save(campanha);
    }
}
