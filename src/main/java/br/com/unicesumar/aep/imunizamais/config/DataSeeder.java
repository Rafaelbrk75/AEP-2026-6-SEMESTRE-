package br.com.unicesumar.aep.imunizamais.config;

import br.com.unicesumar.aep.imunizamais.domain.Campanha;
import br.com.unicesumar.aep.imunizamais.domain.Contato;
import br.com.unicesumar.aep.imunizamais.domain.DoseAplicada;
import br.com.unicesumar.aep.imunizamais.domain.Endereco;
import br.com.unicesumar.aep.imunizamais.domain.Paciente;
import br.com.unicesumar.aep.imunizamais.domain.PostoSaude;
import br.com.unicesumar.aep.imunizamais.domain.PublicoAlvo;
import br.com.unicesumar.aep.imunizamais.domain.Vacina;
import br.com.unicesumar.aep.imunizamais.repository.CampanhaRepository;
import br.com.unicesumar.aep.imunizamais.repository.PacienteRepository;
import br.com.unicesumar.aep.imunizamais.repository.PostoSaudeRepository;
import br.com.unicesumar.aep.imunizamais.repository.VacinaRepository;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Carga inicial de dados para demonstracao da PoC.
 * Desative com APP_SEED_ENABLED=false.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final VacinaRepository vacinaRepository;
    private final PacienteRepository pacienteRepository;
    private final CampanhaRepository campanhaRepository;
    private final PostoSaudeRepository postoSaudeRepository;

    public DataSeeder(VacinaRepository vacinaRepository,
                      PacienteRepository pacienteRepository,
                      CampanhaRepository campanhaRepository,
                      PostoSaudeRepository postoSaudeRepository) {
        this.vacinaRepository = vacinaRepository;
        this.pacienteRepository = pacienteRepository;
        this.campanhaRepository = campanhaRepository;
        this.postoSaudeRepository = postoSaudeRepository;
    }

    @Override
    public void run(String... args) {
        if (vacinaRepository.count() > 0) {
            log.info("Base ja populada. Seed ignorado.");
            return;
        }

        Vacina influenza = vacinaRepository.save(new Vacina(null, "Influenza", "Butantan",
                1, 0, 6, List.of("Gripe sazonal")));
        Vacina hepatiteB = vacinaRepository.save(new Vacina(null, "Hepatite B", "Fiocruz",
                3, 30, 0, List.of("Hepatite B")));
        Vacina tripliceViral = vacinaRepository.save(new Vacina(null, "Triplice Viral", "Fiocruz",
                2, 90, 12, List.of("Sarampo", "Caxumba", "Rubeola")));

        PostoSaude ubsCentral = postoSaudeRepository.save(new PostoSaude(null, "UBS Central",
                "44898887777", 150,
                new Endereco("Av. Brasil", "500", "Centro", "Maringa", "PR", "87013-000")));
        postoSaudeRepository.save(new PostoSaude(null, "UBS Zona 7",
                "44898886666", 80,
                new Endereco("Rua Pioneiro Jose Ferreira", "300", "Zona 7", "Maringa", "PR", "87020-100")));

        LocalDate hoje = LocalDate.now();
        campanhaRepository.save(new Campanha(null, "Campanha de Influenza 2026",
                influenza.getId(),
                new PublicoAlvo(6, 1200, "Criancas a partir de 6 meses e adultos"),
                hoje.minusDays(30), hoje.plusDays(60), 500));

        campanhaRepository.save(new Campanha(null, "Multivacinacao Infantil",
                tripliceViral.getId(),
                new PublicoAlvo(12, 60, "Criancas de 1 a 5 anos"),
                hoje.minusDays(10), hoje.plusDays(40), 200));

        Paciente maria = new Paciente(null, "12345678901", "Maria Souza",
                LocalDate.of(1990, 5, 12),
                new Contato("44999990001", "maria@exemplo.com"),
                new Endereco("Av. Guedner", "1610", "Jardim Aclimacao", "Maringa", "PR", "87050-900"));
        // Dose aplicada ha 40 dias: com intervalo minimo de 30 dias entre doses de Hepatite B,
        // a 2a dose ja fica ATRASADA - exemplo pronto para testar GET /api/pacientes/alertas.
        maria.registrarDose(new DoseAplicada(hepatiteB.getId(), hepatiteB.getNome(), 1,
                hoje.minusDays(40), "LOTE-2026-01", ubsCentral.getId(), ubsCentral.getNome(), null));
        pacienteRepository.save(maria);

        pacienteRepository.save(new Paciente(null, "98765432100", "Joao Pedro Lima",
                hoje.minusMonths(18),
                new Contato("44999990002", "responsavel.joao@exemplo.com"),
                new Endereco("Rua Pioneiro Jose Ferreira", "245", "Zona 7", "Maringa", "PR", "87020-100")));

        log.info("Seed concluido: {} vacinas, {} postos de saude, {} campanhas, {} pacientes.",
                vacinaRepository.count(), postoSaudeRepository.count(), campanhaRepository.count(),
                pacienteRepository.count());
        log.info("Hepatite B id={} (usado nos exemplos do README)", hepatiteB.getId());
    }
}
