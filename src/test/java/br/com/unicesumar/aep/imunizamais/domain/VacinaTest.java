package br.com.unicesumar.aep.imunizamais.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.unicesumar.aep.imunizamais.TestFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Vacina")
class VacinaTest {

    @Test
    @DisplayName("identifica esquema com reforco")
    void exigeReforco() {
        assertTrue(TestFixtures.hepatiteB().exigeReforco());
        assertFalse(TestFixtures.influenza().exigeReforco());
    }

    @Test
    @DisplayName("aceita apenas numeros de dose dentro do esquema")
    void doseValida() {
        Vacina vacina = TestFixtures.hepatiteB();
        assertTrue(vacina.doseValida(1));
        assertTrue(vacina.doseValida(3));
        assertFalse(vacina.doseValida(4));
        assertFalse(vacina.doseValida(0));
    }

    @Test
    @DisplayName("expoe a lista de doencas de forma imutavel")
    void doencasImutaveis() {
        Vacina vacina = TestFixtures.tripliceViral();
        assertEquals(3, vacina.getDoencasPrevenidas().size());
        assertThrows(UnsupportedOperationException.class,
                () -> vacina.getDoencasPrevenidas().add("Outra"));
    }

    @Test
    @DisplayName("aceita lista nula de doencas sem quebrar")
    void listaNulaVirandoVazia() {
        Vacina vacina = new Vacina("v", "BCG", "Fiocruz", 1, 0, 0, null);
        assertTrue(vacina.getDoencasPrevenidas().isEmpty());
        assertEquals("BCG", vacina.getNome());
        assertEquals("Fiocruz", vacina.getFabricante());
        assertEquals(0, vacina.getIntervaloDiasEntreDoses());
        assertEquals(0, vacina.getIdadeMinimaMeses());
    }

    @Test
    @DisplayName("permite atribuir id gerado pelo banco")
    void setId() {
        Vacina vacina = new Vacina(null, "BCG", "Fiocruz", 1, 0, 0, List.of());
        vacina.setId("gerado");
        assertEquals("gerado", vacina.getId());
    }
}
