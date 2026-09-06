package br.com.unicesumar.aep.imunizamais.domain.regra;

import br.com.unicesumar.aep.imunizamais.exception.RegraNegocioException;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/**
 * A data de aplicacao nao pode ser futura nem anterior ao nascimento do paciente.
 */
@Component
public class DataAplicacaoRegra implements RegraAplicacaoDose {

    private final Clock clock;

    public DataAplicacaoRegra() {
        this(Clock.systemDefaultZone());
    }

    public DataAplicacaoRegra(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String codigo() {
        return "DATA_APLICACAO";
    }

    @Override
    public void validar(ContextoAplicacao contexto) {
        LocalDate hoje = LocalDate.now(clock);
        LocalDate data = contexto.getDataAplicacao();

        if (data.isAfter(hoje)) {
            throw new RegraNegocioException(codigo(),
                    "A data de aplicacao nao pode ser futura: " + data);
        }
        if (data.isBefore(contexto.getPaciente().getDataNascimento())) {
            throw new RegraNegocioException(codigo(),
                    "A data de aplicacao e anterior ao nascimento do paciente");
        }
    }
}
