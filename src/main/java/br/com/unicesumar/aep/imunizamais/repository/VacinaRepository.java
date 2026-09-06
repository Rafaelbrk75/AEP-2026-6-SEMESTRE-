package br.com.unicesumar.aep.imunizamais.repository;

import br.com.unicesumar.aep.imunizamais.domain.Vacina;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VacinaRepository extends MongoRepository<Vacina, String> {

    Optional<Vacina> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);
}
