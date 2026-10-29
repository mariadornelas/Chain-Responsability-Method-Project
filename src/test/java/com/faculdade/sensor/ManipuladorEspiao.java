package com.faculdade.sensor;

import java.util.List;

/**
 * Dublê de teste: manipulador com competência configurável que registra,
 * numa lista COMPARTILHADA, cada vez que a cadeia o consulta. Assim o teste
 * enxerga a ordem de visita de toda a cadeia. Não faz parte do padrão.
 */
class ManipuladorEspiao extends ManipuladorOcorrencia {

    private final String nome;
    private final Severidade maxima;
    private final List<String> visitas;

    ManipuladorEspiao(String nome, Severidade maxima, List<String> visitas) {
        this.nome = nome;
        this.maxima = maxima;
        this.visitas = visitas;
    }

    @Override
    protected boolean podeTratar(Ocorrencia ocorrencia) {
        visitas.add(nome);
        return super.podeTratar(ocorrencia);
    }

    @Override
    protected Severidade severidadeMaxima() {
        return maxima;
    }

    @Override
    protected String nome() {
        return nome;
    }

    @Override
    protected String acao(Ocorrencia ocorrencia) {
        return "tratou " + ocorrencia.severidade();
    }
}
