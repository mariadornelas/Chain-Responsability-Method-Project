package com.faculdade.sensor;

import java.util.Locale;

public class OperadorTurno extends ManipuladorOcorrencia {

    @Override
    protected Severidade severidadeMaxima() {
        return Severidade.ALERTA;
    }

    @Override
    protected String nome() {
        return "Operador de turno";
    }

    @Override
    protected String acao(Ocorrencia o) {
        return String.format(Locale.ROOT, "registra no diário de turno e acompanha %s (%.1f)",
                o.sensor(), o.valor());
    }
}
