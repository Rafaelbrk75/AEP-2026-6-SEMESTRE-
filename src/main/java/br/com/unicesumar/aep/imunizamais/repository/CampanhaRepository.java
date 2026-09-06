package br.com.unicesumar.aep.imunizamais.repository;

import br.com.unicesumar.aep.imunizamais.domain.Campanha;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampanhaRepository extends MongoRepository<Campanha, String> {

    List<Campanha> findByAtivaTrue();

    List<Campanha> findByVacinaId(String vacinaId);
}
