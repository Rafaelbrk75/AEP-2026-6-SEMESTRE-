package br.com.unicesumar.aep.imunizamais.domain.regra;

import br.com.unicesumar.aep.imunizamais.exception.RegraNegocioException;
import org.springframework.stereotype.Component;

/**
 * O paciente precisa ter a idade minima exigida pela vacina na data da aplicacao.
 */
@Component
public class IdadeMinimaRegra implements RegraAplicacaoDose {

    @Override
    public String codigo() {
        return "IDADE_MINIMA";
    }

    @Override
    public void validar(ContextoAplicacao contexto) {
        int idade = contexto.idadeDoPacienteEmMeses();
        int minima = contexto.getVacina().getIdadeMinimaMeses();

        if (idade < minima) {
            throw new RegraNegocioException(codigo(),
                    "Paciente tem " + idade + " mes(es) e a vacina "
                            + contexto.getVacina().getNome() + " exige no minimo " + minima + " mes(es)");
        }
    }
}
