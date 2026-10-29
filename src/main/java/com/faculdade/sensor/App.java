package com.faculdade.sensor;

import java.util.Optional;

public class App {

    public static void main(String[] args) {
        ManipuladorOcorrencia operador = new OperadorTurno();
        ManipuladorOcorrencia supervisor = new SupervisorProducao();
        ManipuladorOcorrencia gerente = new GerenteSeguranca();
        operador.definirProximo(supervisor).definirProximo(gerente);

        System.out.println("=== Cadeia completa: Operador -> Supervisor -> Gerente ===");
        enviar(operador, new Ocorrencia("TEMPERATURA", 75.0, Severidade.ALERTA));
        enviar(operador, new Ocorrencia("PRESSAO", 13.0, Severidade.CRITICO));
        enviar(operador, new Ocorrencia("TEMPERATURA", 120.0, Severidade.EMERGENCIA));

        System.out.println();
        System.out.println("=== Cadeia sem o gerente: a emergência fica sem tratamento ===");
        ManipuladorOcorrencia op2 = new OperadorTurno();
        op2.definirProximo(new SupervisorProducao());
        enviar(op2, new Ocorrencia("TEMPERATURA", 120.0, Severidade.EMERGENCIA));

        System.out.println();
        System.out.println("=== A ordem da cadeia decide quem trata ===");
        ManipuladorOcorrencia gerenteNaFrente = new GerenteSeguranca();
        gerenteNaFrente.definirProximo(new OperadorTurno());
        enviar(gerenteNaFrente, new Ocorrencia("TEMPERATURA", 75.0, Severidade.ALERTA));

        System.out.println();
        System.out.println("Ocorrências tratadas por cada um (cadeia completa):");
        System.out.println("  operador:   " + operador.getTratadas().size());
        System.out.println("  supervisor: " + supervisor.getTratadas().size());
        System.out.println("  gerente:    " + gerente.getTratadas().size());
    }

    private static void enviar(ManipuladorOcorrencia inicio, Ocorrencia ocorrencia) {
        Optional<RespostaOcorrencia> resposta = inicio.tratar(ocorrencia);
        String texto = resposta
                .map(r -> r.responsavel() + " " + r.acao())
                .orElse("SEM TRATAMENTO: ninguém na cadeia pôde resolver");
        System.out.println(ocorrencia.severidade() + " -> " + texto);
    }
}
