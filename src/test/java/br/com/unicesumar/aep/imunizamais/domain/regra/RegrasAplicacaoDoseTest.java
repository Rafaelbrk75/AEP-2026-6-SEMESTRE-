package br.com.unicesumar.aep.imunizamais.domain.regra;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.unicesumar.aep.imunizamais.TestFixtures;
import br.com.unicesumar.aep.imunizamais.domain.Campanha;
import br.com.unicesumar.aep.imunizamais.domain.Paciente;
import br.com.unicesumar.aep.imunizamais.domain.PublicoAlvo;
import br.com.unicesumar.aep.imunizamais.domain.Vacina;
import br.com.unicesumar.aep.imunizamais.exception.RegraNegocioException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Regras de aplicacao de dose")
class RegrasAplicacaoDoseTest {

    private ContextoAplicacao contexto(Paciente paciente, Vacina vacina, Campanha campanha,
                                       LocalDate data, int numeroDose) {
        return new ContextoAplicacao(paciente, vacina, campanha, data, numeroDose);
    }

    @Nested
    @DisplayName("DataAplicacaoRegra")
    class DataAplicacao {

        private final DataAplicacaoRegra regra = new DataAplicacaoRegra(TestFixtures.relogioFixo());

        @Test
        @DisplayName("aceita data de hoje e do passado")
        void aceitaDatasValidas() {
            Paciente paciente = TestFixtures.adulta();
            assertDoesNotThrow(() -> regra.validar(
                    contexto(paciente, TestFixtures.influenza(), null, TestFixtures.HOJE, 1)));
            assertDoesNotThrow(() -> regra.validar(
                    contexto(paciente, TestFixtures.influenza(), null, TestFixtures.HOJE.minusDays(10), 1)));
        }

        @Test
        @DisplayName("recusa data futura")
        void recusaFutura() {
            RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> regra.validar(
                    contexto(TestFixtures.adulta(), TestFixtures.influenza(), null,
                            TestFixtures.HOJE.plusDays(1), 1)));
            assertEquals("DATA_APLICACAO", ex.getRegra());
            assertTrue(ex.getMessage().contains("futura"));
        }

        @Test
        @DisplayName("recusa data anterior ao nascimento")
        void recusaAnteriorAoNascimento() {
            RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> regra.validar(
                    contexto(TestFixtures.adulta(), TestFixtures.influenza(), null,
                            LocalDate.of(1980, 1, 1), 1)));
            assertTrue(ex.getMessage().contains("nascimento"));
        }

        @Test
        @DisplayName("construtor padrao usa o relogio do sistema")
        void construtorPadrao() {
            assertEquals("DATA_APLICACAO", new DataAplicacaoRegra().codigo());
        }
    }

    @Nested
    @DisplayName("IdadeMinimaRegra")
    class IdadeMinima {

        private final IdadeMinimaRegra regra = new IdadeMinimaRegra();

        @Test
        @DisplayName("aceita paciente com idade suficiente")
        void aceita() {
            assertDoesNotThrow(() -> regra.validar(contexto(TestFixtures.adulta(),
                    TestFixtures.tripliceViral(), null, TestFixtures.HOJE, 1)));
        }

        @Test
        @DisplayName("recusa paciente abaixo da idade minima")
        void recusa() {
            RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                    () -> regra.validar(contexto(TestFixtures.bebeCom3Meses(),
                            TestFixtures.tripliceViral(), null, TestFixtures.HOJE, 1)));
            assertEquals("IDADE_MINIMA", ex.getRegra());
            assertTrue(ex.getMessage().contains("12"));
        }
    }

    @Nested
    @DisplayName("EsquemaCompletoRegra")
    class EsquemaCompleto {

        private final EsquemaCompletoRegra regra = new EsquemaCompletoRegra();

        @Test
        @DisplayName("aceita dose dentro do esquema")
        void aceita() {
            assertDoesNotThrow(() -> regra.validar(contexto(TestFixtures.adulta(),
                    TestFixtures.hepatiteB(), null, TestFixtures.HOJE, 3)));
        }

        @Test
        @DisplayName("recusa dose alem do esquema recomendado")
        void recusa() {
            RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                    () -> regra.validar(contexto(TestFixtures.adulta(),
                            TestFixtures.hepatiteB(), null, TestFixtures.HOJE, 4)));
            assertEquals("ESQUEMA_COMPLETO", ex.getRegra());
        }
    }

    @Nested
    @DisplayName("IntervaloEntreDosesRegra")
    class Intervalo {

        private final IntervaloEntreDosesRegra regra = new IntervaloEntreDosesRegra();

        @Test
        @DisplayName("primeira dose nao exige intervalo")
        void primeiraDose() {
            assertDoesNotThrow(() -> regra.validar(contexto(TestFixtures.adulta(),
                    TestFixtures.hepatiteB(), null, TestFixtures.HOJE, 1)));
        }

        @Test
        @DisplayName("aceita quando o intervalo minimo foi cumprido")
        void intervaloCumprido() {
            Paciente paciente = TestFixtures.adulta();
            paciente.registrarDose(TestFixtures.dose("vac-hepb", 1, TestFixtures.HOJE.minusDays(30)));
            assertDoesNotThrow(() -> regra.validar(contexto(paciente,
                    TestFixtures.hepatiteB(), null, TestFixtures.HOJE, 2)));
        }

        @Test
        @DisplayName("recusa quando o intervalo minimo nao foi cumprido")
        void intervaloNaoCumprido() {
            Paciente paciente = TestFixtures.adulta();
            paciente.registrarDose(TestFixtures.dose("vac-hepb", 1, TestFixtures.HOJE.minusDays(10)));
            RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                    () -> regra.validar(contexto(paciente, TestFixtures.hepatiteB(), null,
                            TestFixtures.HOJE, 2)));
            assertEquals("INTERVALO_ENTRE_DOSES", ex.getRegra());
            assertTrue(ex.getMessage().contains("30"));
        }
    }

    @Nested
    @DisplayName("CampanhaVigenteRegra")
    class CampanhaVigente {

        private final CampanhaVigenteRegra regra = new CampanhaVigenteRegra();

        @Test
        @DisplayName("dose fora de campanha e sempre aceita")
        void semCampanha() {
            assertDoesNotThrow(() -> regra.validar(contexto(TestFixtures.adulta(),
                    TestFixtures.influenza(), null, TestFixtures.HOJE, 1)));
        }

        @Test
        @DisplayName("aceita campanha vigente com publico compativel")
        void campanhaValida() {
            assertDoesNotThrow(() -> regra.validar(contexto(TestFixtures.adulta(),
                    TestFixtures.influenza(), TestFixtures.campanhaVigente("vac-flu"),
                    TestFixtures.HOJE, 1)));
        }

        @Test
        @DisplayName("recusa campanha de outra vacina")
        void vacinaDivergente() {
            RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                    () -> regra.validar(contexto(TestFixtures.adulta(), TestFixtures.hepatiteB(),
                            TestFixtures.campanhaVigente("vac-flu"), TestFixtures.HOJE, 1)));
            assertTrue(ex.getMessage().contains("nao contempla"));
        }

        @Test
        @DisplayName("recusa campanha encerrada")
        void campanhaEncerrada() {
            Campanha campanha = TestFixtures.campanhaVigente("vac-flu");
            campanha.encerrar();
            RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                    () -> regra.validar(contexto(TestFixtures.adulta(), TestFixtures.influenza(),
                            campanha, TestFixtures.HOJE, 1)));
            assertTrue(ex.getMessage().contains("encerrada"));
        }

        @Test
        @DisplayName("recusa data fora do periodo da campanha")
        void foraDoPeriodo() {
            Campanha campanha = TestFixtures.campanhaVigente("vac-flu");
            RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                    () -> regra.validar(contexto(TestFixtures.adulta(), TestFixtures.influenza(),
                            campanha, campanha.getDataInicio().minusDays(1), 1)));
            assertTrue(ex.getMessage().contains("fora do periodo"));
        }

        @Test
        @DisplayName("recusa paciente fora do publico-alvo")
        void foraDoPublicoAlvo() {
            Campanha campanha = new Campanha("camp-2", "Infantil", "vac-flu",
                    new PublicoAlvo(12, 60, "Criancas de 1 a 5 anos"),
                    TestFixtures.HOJE.minusDays(5), TestFixtures.HOJE.plusDays(5), 100);

            RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                    () -> regra.validar(contexto(TestFixtures.adulta(), TestFixtures.influenza(),
                            campanha, TestFixtures.HOJE, 1)));
            assertEquals("CAMPANHA_VIGENTE", ex.getRegra());
            assertTrue(ex.getMessage().contains("publico-alvo"));
        }
    }

    @Test
    @DisplayName("contexto expoe os dados usados pelas regras")
    void contextoExpoeDados() {
        Paciente paciente = TestFixtures.adulta();
        Vacina vacina = TestFixtures.influenza();
        Campanha campanha = TestFixtures.campanhaVigente("vac-flu");
        ContextoAplicacao ctx = contexto(paciente, vacina, campanha, TestFixtures.HOJE, 2);

        assertEquals(paciente, ctx.getPaciente());
        assertEquals(vacina, ctx.getVacina());
        assertTrue(ctx.getCampanha().isPresent());
        assertEquals(TestFixtures.HOJE, ctx.getDataAplicacao());
        assertEquals(2, ctx.getNumeroDose());
        assertEquals(paciente.idadeEmMesesEm(TestFixtures.HOJE), ctx.idadeDoPacienteEmMeses());
        assertTrue(contexto(paciente, vacina, null, TestFixtures.HOJE, 1).getCampanha().isEmpty());
    }
}
