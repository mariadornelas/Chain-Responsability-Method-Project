package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * O que caracteriza o Chain of Responsibility: o cliente só conhece o
 * primeiro elo, o pedido sobe pela cadeia até alguém tratar, e a cadeia
 * para nesse ponto.
 */
class CadeiaResponsabilidadeTest {

    private final Ocorrencia alerta = new Ocorrencia("TEMPERATURA", 75.0, Severidade.ALERTA);
    private final Ocorrencia critica = new Ocorrencia("PRESSAO", 13.0, Severidade.CRITICO);
    private final Ocorrencia emergencia = new Ocorrencia("TEMPERATURA", 120.0, Severidade.EMERGENCIA);

    private final ManipuladorOcorrencia operador = new OperadorTurno();
    private final ManipuladorOcorrencia supervisor = new SupervisorProducao();
    private final ManipuladorOcorrencia gerente = new GerenteSeguranca();

    private void montarCadeiaCompleta() {
        operador.definirProximo(supervisor).definirProximo(gerente);
    }

    // ---- quem trata o quê ----

    @Test
    void cadaSeveridadeDeveSerTratadaPeloPrimeiroElosuficientementeCompetente() {
        montarCadeiaCompleta();

        assertEquals("Operador de turno", operador.tratar(alerta).orElseThrow().responsavel());
        assertEquals("Supervisor de produção", operador.tratar(critica).orElseThrow().responsavel());
        assertEquals("Gerente de segurança", operador.tratar(emergencia).orElseThrow().responsavel());
    }

    @Test
    void somenteQuemTratouDeveRegistrarAOcorrencia() {
        montarCadeiaCompleta();

        operador.tratar(alerta);
        operador.tratar(critica);
        operador.tratar(emergencia);

        assertEquals(List.of(alerta), operador.getTratadas());
        assertEquals(List.of(critica), supervisor.getTratadas());
        assertEquals(List.of(emergencia), gerente.getTratadas());
    }

    @Test
    void cadeiaDeveSerRetornadaDoUltimoElo() {
        ManipuladorOcorrencia devolvido = operador.definirProximo(supervisor);

        assertSame(supervisor, devolvido);
    }

    // ---- a cadeia para no primeiro que trata ----

    @Test
    void cadeiaDevePararNoPrimeiroQueTrataSemConsultarOsSeguintes() {
        List<String> visitas = new ArrayList<>();
        ManipuladorEspiao a = new ManipuladorEspiao("A", Severidade.ALERTA, visitas);
        ManipuladorEspiao b = new ManipuladorEspiao("B", Severidade.CRITICO, visitas);
        ManipuladorEspiao c = new ManipuladorEspiao("C", Severidade.EMERGENCIA, visitas);
        a.definirProximo(b).definirProximo(c);

        a.tratar(critica);   // A não pode, B pode: C nem é consultado

        assertEquals(List.of("A", "B"), visitas);
    }

    @Test
    void cadeiaDeveConsultarOsElosNaOrdemEmQueForamEncadeados() {
        List<String> visitas = new ArrayList<>();
        ManipuladorEspiao a = new ManipuladorEspiao("A", Severidade.ALERTA, visitas);
        ManipuladorEspiao b = new ManipuladorEspiao("B", Severidade.CRITICO, visitas);
        ManipuladorEspiao c = new ManipuladorEspiao("C", Severidade.EMERGENCIA, visitas);
        a.definirProximo(b).definirProximo(c);

        a.tratar(emergencia);

        assertEquals(List.of("A", "B", "C"), visitas);
    }

    // ---- a ordem da cadeia decide ----

    @Test
    void ordemDaCadeiaDeveDecidirQuemTrata() {
        ManipuladorOcorrencia g = new GerenteSeguranca();
        g.definirProximo(new OperadorTurno());

        // O gerente é capaz de tudo e está na frente: trata até um simples alerta.
        assertEquals("Gerente de segurança", g.tratar(alerta).orElseThrow().responsavel());
    }

    // ---- ocorrência sem tratamento ----

    @Test
    void cadeiaSemUmElosuficienteDeveDevolverVazio() {
        operador.definirProximo(supervisor);   // sem gerente

        assertTrue(operador.tratar(emergencia).isEmpty());
        assertTrue(operador.getTratadas().isEmpty());
        assertTrue(supervisor.getTratadas().isEmpty());
    }

    // ---- composição dinâmica ----

    @Test
    void cadeiaPodeSerEstendidaEmTempoDeExecucao() {
        operador.definirProximo(supervisor);
        assertTrue(operador.tratar(emergencia).isEmpty());

        supervisor.definirProximo(gerente);   // agora a cadeia ganha o último elo

        assertEquals("Gerente de segurança", operador.tratar(emergencia).orElseThrow().responsavel());
    }

    @Test
    void trocarOProximoDeveSubstituirOAnterior() {
        ManipuladorOcorrencia outroGerente = new GerenteSeguranca();
        operador.definirProximo(gerente);
        operador.definirProximo(outroGerente);

        operador.tratar(critica);

        assertTrue(gerente.getTratadas().isEmpty());
        assertEquals(List.of(critica), outroGerente.getTratadas());
    }

    @Test
    void mesmoManipuladorPodeSerReaproveitadoEmOutraCadeia() {
        ManipuladorOcorrencia outroOperador = new OperadorTurno();
        operador.definirProximo(gerente);
        outroOperador.definirProximo(gerente);

        operador.tratar(emergencia);
        outroOperador.tratar(emergencia);

        assertEquals(2, gerente.getTratadas().size());
    }

    // ---- proteção da cadeia ----

    @Test
    void naoDeveAceitarProximoNulo() {
        assertThrows(NullPointerException.class, () -> operador.definirProximo(null));
    }

    @Test
    void naoDevePermitirQueUmElosejaSeuProprioProximo() {
        assertThrows(IllegalArgumentException.class, () -> operador.definirProximo(operador));
    }

    @Test
    void naoDevePermitirCicloNaCadeia() {
        operador.definirProximo(supervisor).definirProximo(gerente);

        assertThrows(IllegalArgumentException.class, () -> gerente.definirProximo(operador));
        assertThrows(IllegalArgumentException.class, () -> gerente.definirProximo(supervisor));
    }

    @Test
    void tentativaDeCicloNaoDeveAlterarACadeiaExistente() {
        operador.definirProximo(supervisor).definirProximo(gerente);

        assertThrows(IllegalArgumentException.class, () -> gerente.definirProximo(operador));

        // a cadeia continua funcionando e termina no gerente
        assertEquals("Gerente de segurança", operador.tratar(emergencia).orElseThrow().responsavel());
    }

    // ---- desacoplamento ----

    @Test
    void clienteDeveFuncionarConhecendoApenasOTipoAbstratoDoPrimeiroElo() {
        montarCadeiaCompleta();
        ManipuladorOcorrencia inicio = operador;   // o cliente só enxerga isto

        for (Ocorrencia o : List.of(alerta, critica, emergencia)) {
            assertTrue(assertDoesNotThrow(() -> inicio.tratar(o)).isPresent());
        }
    }

    @Test
    void novoManipuladorPodeEntrarNaCadeiaSemAlterarNenhumExistente() {
        // Um elo novo, criado só no teste, que só aceita ALERTA e age diferente.
        ManipuladorOcorrencia robo = new ManipuladorOcorrencia() {
            @Override
            protected Severidade severidadeMaxima() {
                return Severidade.ALERTA;
            }

            @Override
            protected String nome() {
                return "Robô de monitoramento";
            }

            @Override
            protected String acao(Ocorrencia o) {
                return "envia notificação automática";
            }
        };
        robo.definirProximo(operador).definirProximo(supervisor);

        assertEquals("Robô de monitoramento", robo.tratar(alerta).orElseThrow().responsavel());
        assertEquals("Supervisor de produção", robo.tratar(critica).orElseThrow().responsavel());
    }
}
