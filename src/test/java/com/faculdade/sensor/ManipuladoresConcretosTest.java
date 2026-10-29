package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ManipuladoresConcretosTest {

    private final Ocorrencia alerta = new Ocorrencia("TEMPERATURA", 75.0, Severidade.ALERTA);
    private final Ocorrencia critica = new Ocorrencia("PRESSAO", 13.0, Severidade.CRITICO);
    private final Ocorrencia emergencia = new Ocorrencia("TEMPERATURA", 120.0, Severidade.EMERGENCIA);

    @Test
    void operadorDeveTratarApenasAlertas() {
        ManipuladorOcorrencia operador = new OperadorTurno();

        assertTrue(operador.tratar(alerta).isPresent());
        assertTrue(operador.tratar(critica).isEmpty());
        assertTrue(operador.tratar(emergencia).isEmpty());
    }

    @Test
    void supervisorDeveTratarAlertasECriticosMasNaoEmergencias() {
        ManipuladorOcorrencia supervisor = new SupervisorProducao();

        assertTrue(supervisor.tratar(alerta).isPresent());
        assertTrue(supervisor.tratar(critica).isPresent());
        assertTrue(supervisor.tratar(emergencia).isEmpty());
    }

    @Test
    void gerenteDeveTratarQualquerSeveridade() {
        ManipuladorOcorrencia gerente = new GerenteSeguranca();

        assertTrue(gerente.tratar(alerta).isPresent());
        assertTrue(gerente.tratar(critica).isPresent());
        assertTrue(gerente.tratar(emergencia).isPresent());
    }

    @Test
    void respostaDeveInformarQuemTratouEOQueFoiFeito() {
        RespostaOcorrencia r = new OperadorTurno().tratar(alerta).orElseThrow();

        assertEquals("Operador de turno", r.responsavel());
        assertEquals("registra no diário de turno e acompanha TEMPERATURA (75.0)", r.acao());
    }

    @Test
    void cadaManipuladorDeveTerSuaPropriaAcao() {
        assertEquals("aciona a manutenção e reduz a carga por causa de PRESSAO (13.0)",
                new SupervisorProducao().tratar(critica).orElseThrow().acao());
        assertEquals("para a linha e evacua a área por causa de TEMPERATURA (120.0)",
                new GerenteSeguranca().tratar(emergencia).orElseThrow().acao());
    }

    @Test
    void manipuladorDeveGuardarSomenteAsOcorrenciasQueTratou() {
        ManipuladorOcorrencia operador = new OperadorTurno();

        operador.tratar(alerta);
        operador.tratar(critica);

        assertEquals(java.util.List.of(alerta), operador.getTratadas());
    }

    @Test
    void listaDeTratadasDeveSerSomenteLeitura() {
        ManipuladorOcorrencia operador = new OperadorTurno();
        operador.tratar(alerta);

        assertThrows(UnsupportedOperationException.class, () -> operador.getTratadas().add(critica));
    }

    @Test
    void naoDeveAceitarOcorrenciaNula() {
        assertThrows(NullPointerException.class, () -> new OperadorTurno().tratar(null));
    }

    @Test
    void ocorrenciaNaoDeveAceitarCamposNulos() {
        assertThrows(NullPointerException.class, () -> new Ocorrencia(null, 1.0, Severidade.ALERTA));
        assertThrows(NullPointerException.class, () -> new Ocorrencia("TEMPERATURA", 1.0, null));
    }

    @Test
    void resultadoSemTratamentoDeveSerOptionalVazio() {
        Optional<RespostaOcorrencia> r = new OperadorTurno().tratar(emergencia);

        assertTrue(r.isEmpty());
    }
}
