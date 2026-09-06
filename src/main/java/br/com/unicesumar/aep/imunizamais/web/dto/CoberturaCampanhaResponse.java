package br.com.unicesumar.aep.imunizamais.web.dto;

public record CoberturaCampanhaResponse(
        String campanhaId,
        String nome,
        int metaDoses,
        int dosesAplicadas,
        double percentualCobertura,
        boolean metaAtingida,
        boolean ativa) {
}
