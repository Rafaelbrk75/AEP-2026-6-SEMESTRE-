package br.com.unicesumar.aep.imunizamais.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento da colecao "postos_saude": as unidades de saude onde as doses sao
 * aplicadas. Referenciado pelo id em DoseAplicada (postoSaudeId).
 */
@Document(collection = "postos_saude")
public class PostoSaude {

    @Id
    private String id;

    @Indexed(unique = true)
    private String nome;

    private String telefone;
    private int capacidadeDiariaDoses;
    private Endereco endereco;
    private boolean ativo = true;

    protected PostoSaude() {
    }

    public PostoSaude(String id, String nome, String telefone, int capacidadeDiariaDoses, Endereco endereco) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.capacidadeDiariaDoses = capacidadeDiariaDoses;
        this.endereco = endereco;
        this.ativo = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public int getCapacidadeDiariaDoses() {
        return capacidadeDiariaDoses;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public boolean isAtivo() {
        return ativo;
    }

    /** Desativa o posto; doses novas nao poderao mais ser registradas nele. */
    public void desativar() {
        this.ativo = false;
    }
}
