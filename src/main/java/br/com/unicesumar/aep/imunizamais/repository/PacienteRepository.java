package br.com.unicesumar.aep.imunizamais.repository;

import br.com.unicesumar.aep.imunizamais.domain.Paciente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends MongoRepository<Paciente, String> {

    Optional<Paciente> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    List<Paciente> findByEnderecoCidadeIgnoreCase(String cidade);
}
