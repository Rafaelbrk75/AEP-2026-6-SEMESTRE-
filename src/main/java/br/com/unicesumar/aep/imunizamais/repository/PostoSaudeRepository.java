package br.com.unicesumar.aep.imunizamais.repository;

import br.com.unicesumar.aep.imunizamais.domain.PostoSaude;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostoSaudeRepository extends MongoRepository<PostoSaude, String> {

    boolean existsByNomeIgnoreCase(String nome);

    List<PostoSaude> findByAtivoTrue();
}
