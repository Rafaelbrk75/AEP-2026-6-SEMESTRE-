package br.com.unicesumar.aep.imunizamais.service;

import br.com.unicesumar.aep.imunizamais.domain.Contato;
import br.com.unicesumar.aep.imunizamais.domain.Endereco;
import br.com.unicesumar.aep.imunizamais.domain.Paciente;
import br.com.unicesumar.aep.imunizamais.exception.RecursoNaoEncontradoException;
import br.com.unicesumar.aep.imunizamais.exception.RegraNegocioException;
import br.com.unicesumar.aep.imunizamais.repository.PacienteRepository;
import br.com.unicesumar.aep.imunizamais.web.dto.ContatoDTO;
import br.com.unicesumar.aep.imunizamais.web.dto.EnderecoDTO;
import br.com.unicesumar.aep.imunizamais.web.dto.NovoPacienteRequest;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Casos de uso da colecao "pacientes".
 */
@Service
public class PacienteService {

    private final PacienteRepository repositorio;

    public PacienteService(PacienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Paciente cadastrar(NovoPacienteRequest request) {
        if (repositorio.existsByCpf(request.cpf())) {
            throw new RegraNegocioException("PACIENTE_DUPLICADO",
                    "Ja existe um paciente cadastrado com o CPF " + request.cpf());
        }

        ContatoDTO c = request.contato();
        EnderecoDTO e = request.endereco();

        Paciente paciente = new Paciente(
                null,
                request.cpf(),
                request.nome(),
                request.dataNascimento(),
                new Contato(c.telefone(), c.email()),
                new Endereco(e.logradouro(), e.numero(), e.bairro(), e.cidade(), e.uf(), e.cep()));

        return repositorio.save(paciente);
    }

    public List<Paciente> listar() {
        return repositorio.findAll();
    }

    public List<Paciente> listarPorCidade(String cidade) {
        return repositorio.findByEnderecoCidadeIgnoreCase(cidade);
    }

    public Paciente buscarPorCpf(String cpf) {
        return repositorio.findByCpf(cpf)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Paciente", cpf));
    }
}
