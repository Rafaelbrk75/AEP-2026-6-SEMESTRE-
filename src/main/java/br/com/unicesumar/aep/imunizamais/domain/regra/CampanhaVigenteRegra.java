package br.com.unicesumar.aep.imunizamais.domain.regra;

import br.com.unicesumar.aep.imunizamais.domain.Campanha;
import br.com.unicesumar.aep.imunizamais.exception.RegraNegocioException;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Quando a dose e vinculada a uma campanha, a campanha precisa estar ativa,
 * vigente na data e o paciente precisa pertencer ao publico-alvo.
 */
@Component
public class CampanhaVigenteRegra implements RegraAplicacaoDose {

    @Override
    public String codigo() {
        return "CAMPANHA_VIGENTE";
    }

    @Override
    public void validar(ContextoAplicacao contexto) {
        Optional<Campanha> possivelCampanha = contexto.getCampanha();
        if (possivelCampanha.isEmpty()) {
            return;
        }

        Campanha campanha = possivelCampanha.get();

        if (!campanha.getVacinaId().equals(contexto.getVacina().getId())) {
            throw new RegraNegocioException(codigo(),
                    "A campanha " + campanha.getNome() + " nao contempla a vacina "
                            + contexto.getVacina().getNome());
        }

        if (!campanha.isAtiva()) {
            throw new RegraNegocioException(codigo(),
                    "A campanha " + campanha.getNome() + " esta encerrada");
        }

        if (!campanha.vigenteEm(contexto.getDataAplicacao())) {
            throw new RegraNegocioException(codigo(),
                    "A data " + contexto.getDataAplicacao() + " esta fora do periodo da campanha "
                            + campanha.getNome());
        }

        if (!campanha.aptaPara(contexto.getDataAplicacao(), contexto.idadeDoPacienteEmMeses())) {
            throw new RegraNegocioException(codigo(),
                    "Paciente fora do publico-alvo da campanha " + campanha.getNome()
                            + " (" + campanha.getPublicoAlvo().getDescricao() + ")");
        }
    }
}
