package br.com.unicesumar.aep.imunizamais.web.dto;

import br.com.unicesumar.aep.imunizamais.domain.SituacaoVacinal;
import java.time.LocalDate;

/**
 * Alerta pronto para um posto de saude (ou uma rotina de notificacao) usar:
 * identifica o paciente e a vacina cuja dose esta pendente, atrasada, ou
 * cuja proxima dose esta prevista para os proximos dias.
 */
public record AlertaVacinacaoResponse(
        String cpf,
        String nomePaciente,
        String telefone,
        String email,
        String vacinaId,
        String nomeVacina,
        SituacaoVacinal situacao,
        Integer proximaDose,
        LocalDate dataPrevistaProximaDose,
        boolean proximaDoseEmBreve) {
}
