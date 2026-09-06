package br.com.unicesumar.aep.imunizamais.service;

import br.com.unicesumar.aep.imunizamais.domain.Endereco;
import br.com.unicesumar.aep.imunizamais.domain.PostoSaude;
import br.com.unicesumar.aep.imunizamais.exception.RecursoNaoEncontradoException;
import br.com.unicesumar.aep.imunizamais.exception.RegraNegocioException;
import br.com.unicesumar.aep.imunizamais.repository.PostoSaudeRepository;
import br.com.unicesumar.aep.imunizamais.web.dto.EnderecoDTO;
import br.com.unicesumar.aep.imunizamais.web.dto.NovoPostoSaudeRequest;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Casos de uso da colecao "postos_saude".
 */
@Service
public class PostoSaudeService {

    private final PostoSaudeRepository repositorio;

    public PostoSaudeService(PostoSaudeRepository repositorio) {
        this.repositorio = repositorio;
    }

    public PostoSaude cadastrar(NovoPostoSaudeRequest request) {
        if (repositorio.existsByNomeIgnoreCase(request.nome())) {
            throw new RegraNegocioException("POSTO_DUPLICADO",
                    "Ja existe um posto de saude cadastrado com o nome " + request.nome());
        }

        EnderecoDTO e = request.endereco();
        Endereco endereco = new Endereco(e.logradouro(), e.numero(), e.bairro(), e.cidade(), e.uf(), e.cep());

        PostoSaude posto = new PostoSaude(null, request.nome(), request.telefone(),
                request.capacidadeDiariaDoses(), endereco);

        return repositorio.save(posto);
    }

    public List<PostoSaude> listar() {
        return repositorio.findAll();
    }

    public List<PostoSaude> listarAtivos() {
        return repositorio.findByAtivoTrue();
    }

    public PostoSaude buscarPorId(String id) {
        return repositorio.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Posto de Saude", id));
    }

    public PostoSaude desativar(String id) {
        PostoSaude posto = buscarPorId(id);
        posto.desativar();
        return repositorio.save(posto);
    }
}
