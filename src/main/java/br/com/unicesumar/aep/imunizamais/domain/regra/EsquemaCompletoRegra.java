package br.com.unicesumar.aep.imunizamais.domain.regra;

import br.com.unicesumar.aep.imunizamais.exception.RegraNegocioException;
import org.springframework.stereotype.Component;

/**
 * Nao se aplica dose alem do numero recomendado no esquema da vacina.
 */
@Component
public class EsquemaCompletoRegra implements RegraAplicacaoDose {

    @Override
    public String codigo() {
        return "ESQUEMA_COMPLETO";
    }

    @Override
    public void validar(ContextoAplicacao contexto) {
        if (!contexto.getVacina().doseValida(contexto.getNumeroDose())) {
            throw new RegraNegocioException(codigo(),
                    "O esquema da vacina " + contexto.getVacina().getNome() + " preve apenas "
                            + contexto.getVacina().getDosesRecomendadas()
                            + " dose(s) e o paciente ja as recebeu");
        }
    }
}
