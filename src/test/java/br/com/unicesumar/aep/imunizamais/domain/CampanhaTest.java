package br.com.unicesumar.aep.imunizamais.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.unicesumar.aep.imunizamais.TestFixtures;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Campanha")
class CampanhaTest {

    @Test
    @DisplayName("reconhece o periodo de vigencia inclusive nos limites")
    void vigencia() {
        Campanha campanha = TestFixtures.campanhaVigente("vac-flu");
        assertTrue(campanha.vigenteEm(TestFixtures.HOJE));
        assertTrue(campanha.vigenteEm(campanha.getDataInicio()));
        assertTrue(campanha.vigenteEm(campanha.getDataFim()));
        assertFalse(campanha.vigenteEm(campanha.getDataInicio().minusDays(1)));
        assertFalse(campanha.vigenteEm(campanha.getDataFim().plusDays(1)));
    }

    @Test
    @DisplayName("so aceita paciente dentro do publico-alvo")
    void publicoAlvo() {
        Campanha campanha = TestFixtures.campanhaVigente("vac-flu");
        assertTrue(campanha.aptaPara(TestFixtures.HOJE, 400));
        assertFalse(campanha.aptaPara(TestFixtures.HOJE, 3));
        assertFalse(campanha.aptaPara(TestFixtures.HOJE, 5000));
    }

    @Test
    @DisplayName("campanha encerrada nao esta apta")
    void encerrada() {
        Campanha campanha = TestFixtures.campanhaVigente("vac-flu");
        campanha.encerrar();
        assertFalse(campanha.isAtiva());
        assertFalse(campanha.aptaPara(TestFixtures.HOJE, 400));
    }

    @Test
    @DisplayName("acumula doses e calcula a cobertura")
    void cobertura() {
        Campanha campanha = new Campanha("c", "Teste", "v", new PublicoAlvo(0, 1200, "todos"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 4);

        assertEquals(0d, campanha.percentualCobertura());
        assertFalse(campanha.metaAtingida());

        campanha.registrarDose();
        campanha.registrarDose();
        assertEquals(2, campanha.getDosesAplicadas());
        assertEquals(50d, campanha.percentualCobertura());

        campanha.registrarDose();
        campanha.registrarDose();
        assertTrue(campanha.metaAtingida());
        assertEquals(100d, campanha.percentualCobertura());
    }

    @Test
    @DisplayName("limita a cobertura em 100% mesmo acima da meta")
    void coberturaLimitada() {
        Campanha campanha = new Campanha("c", "Teste", "v", new PublicoAlvo(0, 1200, "todos"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 1);
        campanha.registrarDose();
        campanha.registrarDose();
        assertEquals(100d, campanha.percentualCobertura());
    }

    @Test
    @DisplayName("meta zerada nao gera divisao por zero")
    void metaZerada() {
        Campanha campanha = new Campanha("c", "Teste", "v", new PublicoAlvo(0, 1200, "todos"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 0);
        assertEquals(0d, campanha.percentualCobertura());
        assertFalse(campanha.metaAtingida());
    }

    @Test
    @DisplayName("expoe dados basicos e aceita id gerado")
    void dadosBasicos() {
        Campanha campanha = TestFixtures.campanhaVigente("vac-flu");
        campanha.setId("nova-id");
        assertEquals("nova-id", campanha.getId());
        assertEquals("vac-flu", campanha.getVacinaId());
        assertEquals("Campanha de Influenza 2026", campanha.getNome());
        assertEquals(500, campanha.getMetaDoses());
        assertEquals(6, campanha.getPublicoAlvo().getIdadeMinimaMeses());
        assertEquals(1200, campanha.getPublicoAlvo().getIdadeMaximaMeses());
        assertEquals("Criancas a partir de 6 meses e adultos",
                campanha.getPublicoAlvo().getDescricao());
    }
}
