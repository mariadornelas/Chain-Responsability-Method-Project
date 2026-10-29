package com.faculdade.sensor;

import java.util.Objects;

public record Ocorrencia(String sensor, double valor, Severidade severidade) {

    public Ocorrencia {
        Objects.requireNonNull(sensor, "sensor não pode ser nulo");
        Objects.requireNonNull(severidade, "severidade não pode ser nula");
    }
}
