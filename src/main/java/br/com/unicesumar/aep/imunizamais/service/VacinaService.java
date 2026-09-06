package br.com.unicesumar.aep.imunizamais.service;

import br.com.unicesumar.aep.imunizamais.domain.Vacina;
import br.com.unicesumar.aep.imunizamais.exception.RecursoNaoEncontradoException;
import br.com.unicesumar.aep.imunizamais.exception.RegraNegocioException;
import br.com.unicesumar.aep.imunizamais.repository.VacinaRepository;
import br.com.unicesumar.aep.imunizamais.web.dto.NovaVacinaRequest;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Casos de uso da colecao "vacinas".
 */
@Service
public class VacinaService {

    private final VacinaRepository repositorio;

    public VacinaService(VacinaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Vacina cadastrar(NovaVacinaRequest request) {
        if (repositorio.existsByNomeIgnoreCase(request.nome())) {
            throw new RegraNegocioException("VACINA_DUPLICADA",
                    "Ja existe uma vacina cadastrada com o nome " + request.nome());
        }
        Vacina vacina = new Vacina(null, request.nome(), request.fabricante(),
                request.dosesRecomendadas(), request.intervaloDiasEntreDoses(),
                request.idadeMinimaMeses(), request.doencasPrevenidas());
        return repositorio.save(vacina);
    }

    public List<Vacina> listar() {
        return repositorio.findAll();
    }

    public Vacina buscarPorId(String id) {
        return repositorio.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Vacina", id));
    }
}
