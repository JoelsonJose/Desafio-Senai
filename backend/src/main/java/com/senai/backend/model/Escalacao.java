package com.senai.backend.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "escalacoes")
@IdClass(Escalacao.EscalacaoId.class)
public class Escalacao {

    @Id
    @Column(name = "clube_id", nullable = false)
    private Integer clubeId;

    @Id
    @Column(name = "jogador_id", nullable = false, unique = true)
    private Integer jogadorId;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    public Escalacao() {
    }

    public Escalacao(Integer clubeId, Integer jogadorId) {
        this.clubeId = clubeId;
        this.jogadorId = jogadorId;
        this.criadoEm = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
    }

    public Integer getClubeId() { return clubeId; }
    public void setClubeId(Integer clubeId) { this.clubeId = clubeId; }

    public Integer getJogadorId() { return jogadorId; }
    public void setJogadorId(Integer jogadorId) { this.jogadorId = jogadorId; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public static class EscalacaoId implements Serializable {
        private Integer clubeId;
        private Integer jogadorId;

        public EscalacaoId() {}

        public EscalacaoId(Integer clubeId, Integer jogadorId) {
            this.clubeId = clubeId;
            this.jogadorId = jogadorId;
        }

        public Integer getClubeId() { return clubeId; }
        public void setClubeId(Integer clubeId) { this.clubeId = clubeId; }

        public Integer getJogadorId() { return jogadorId; }
        public void setJogadorId(Integer jogadorId) { this.jogadorId = jogadorId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EscalacaoId that)) return false;
            return Objects.equals(clubeId, that.clubeId) && Objects.equals(jogadorId, that.jogadorId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clubeId, jogadorId);
        }
    }
}
