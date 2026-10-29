package com.faculdade.sensor;

import java.util.Locale;

public class SupervisorProducao extends ManipuladorOcorrencia {

    @Override
    protected Severidade severidadeMaxima() {
        return Severidade.CRITICO;
    }

    @Override
    protected String nome() {
        return "Supervisor de produção";
    }

    @Override
    protected String acao(Ocorrencia o) {
        return String.format(Locale.ROOT, "aciona a manutenção e reduz a carga por causa de %s (%.1f)",
                o.sensor(), o.valor());
    }
}
