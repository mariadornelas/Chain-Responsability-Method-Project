package com.faculdade.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class ManipuladorOcorrencia {

    private ManipuladorOcorrencia proximo;
    private final List<Ocorrencia> tratadas = new ArrayList<>();

    public ManipuladorOcorrencia definirProximo(ManipuladorOcorrencia proximo) {
        Objects.requireNonNull(proximo, "próximo não pode ser nulo");
        for (ManipuladorOcorrencia elo = proximo; elo != null; elo = elo.proximo) {
            if (elo == this) {
                throw new IllegalArgumentException("a cadeia não pode formar um ciclo");
            }
        }
        this.proximo = proximo;
        return proximo;
    }

    public final Optional<RespostaOcorrencia> tratar(Ocorrencia ocorrencia) {
        Objects.requireNonNull(ocorrencia, "ocorrência não pode ser nula");
        if (podeTratar(ocorrencia)) {
            tratadas.add(ocorrencia);
            return Optional.of(new RespostaOcorrencia(nome(), acao(ocorrencia)));
        }
        if (proximo == null) {
            return Optional.empty();
        }
        return proximo.tratar(ocorrencia);
    }

    public List<Ocorrencia> getTratadas() {
        return Collections.unmodifiableList(tratadas);
    }

    protected boolean podeTratar(Ocorrencia ocorrencia) {
        return ocorrencia.severidade().compareTo(severidadeMaxima()) <= 0;
    }

    protected abstract Severidade severidadeMaxima();

    protected abstract String nome();

    protected abstract String acao(Ocorrencia ocorrencia);
}
