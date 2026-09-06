package br.com.unicesumar.aep.imunizamais.domain.regra;

import br.com.unicesumar.aep.imunizamais.domain.DoseAplicada;
import br.com.unicesumar.aep.imunizamais.exception.RegraNegocioException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Respeita o intervalo minimo, em dias, entre a ultima dose aplicada e a proxima.
 */
@Component
public class IntervaloEntreDosesRegra implements RegraAplicacaoDose {

    @Override
    public String codigo() {
        return "INTERVALO_ENTRE_DOSES";
    }

    @Override
    public void validar(ContextoAplicacao contexto) {
        Optional<DoseAplicada> ultima = contexto.getPaciente()
                .ultimaDoseDe(contexto.getVacina().getId());

        if (ultima.isEmpty()) {
            return;
        }

        int intervaloExigido = contexto.getVacina().getIntervaloDiasEntreDoses();
        LocalDate dataUltima = ultima.get().getDataAplicacao();
        long decorridos = ChronoUnit.DAYS.between(dataUltima, contexto.getDataAplicacao());

        if (decorridos < intervaloExigido) {
            throw new RegraNegocioException(codigo(),
                    "Intervalo minimo de " + intervaloExigido + " dia(s) nao respeitado: "
                            + "ultima dose em " + dataUltima + " (" + decorridos + " dia(s) decorridos)");
        }
    }
}
