package com.faculdade.sensor;

import java.util.Locale;

public class GerenteSeguranca extends ManipuladorOcorrencia {

    @Override
    protected Severidade severidadeMaxima() {
        return Severidade.EMERGENCIA;
    }

    @Override
    protected String nome() {
        return "Gerente de segurança";
    }

    @Override
    protected String acao(Ocorrencia o) {
        return String.format(Locale.ROOT, "para a linha e evacua a área por causa de %s (%.1f)",
                o.sensor(), o.valor());
    }
}
