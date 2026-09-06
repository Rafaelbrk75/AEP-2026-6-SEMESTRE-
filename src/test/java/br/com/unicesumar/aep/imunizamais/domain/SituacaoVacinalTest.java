package br.com.unicesumar.aep.imunizamais.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.unicesumar.aep.imunizamais.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Situacao vacinal")
class SituacaoVacinalTest {

    @Test
    @DisplayName("apenas pendente e atrasada exigem acao")
    void exigeAcao() {
        assertTrue(SituacaoVacinal.PENDENTE.exigeAcao());
        assertTrue(SituacaoVacinal.ATRASADA.exigeAcao());
        assertFalse(SituacaoVacinal.EM_DIA.exigeAcao());
        assertFalse(SituacaoVacinal.COMPLETO.exigeAcao());
        assertFalse(SituacaoVacinal.NAO_ELEGIVEL.exigeAcao());
    }

    @Test
    @DisplayName("todo item do enum possui descricao")
    void descricoes() {
        for (SituacaoVacinal valor : SituacaoVacinal.values()) {
            assertFalse(valor.getDescricao().isBlank());
        }
    }

    @Test
    @DisplayName("objeto de valor da situacao expoe o resultado calculado")
    void objetoDeValor() {
        SituacaoPaciente situacao = new SituacaoPaciente("vac-hepb", "Hepatite B", 1, 3,
                SituacaoVacinal.EM_DIA, 2, TestFixtures.HOJE.plusDays(10));

        assertEquals("vac-hepb", situacao.getVacinaId());
        assertEquals("Hepatite B", situacao.getNomeVacina());
        assertEquals(1, situacao.getDosesAplicadas());
        assertEquals(3, situacao.getDosesRecomendadas());
        assertEquals(SituacaoVacinal.EM_DIA, situacao.getSituacao());
        assertEquals(2, situacao.getProximaDose());
        assertEquals(TestFixtures.HOJE.plusDays(10), situacao.getDataPrevistaProximaDose());
        assertFalse(situacao.exigeAcao());

        SituacaoPaciente completo = new SituacaoPaciente("v", "V", 1, 1,
                SituacaoVacinal.COMPLETO, null, null);
        assertNull(completo.getProximaDose());
        assertNull(completo.getDataPrevistaProximaDose());
    }
}
