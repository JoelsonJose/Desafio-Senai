package com.senai.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contratos")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "jogador_id", nullable = false)
    private Jogador jogador;

    @ManyToOne
    @JoinColumn(name = "clube_id", nullable = false)
    private Clube clube;

    @Column(name = "valor_compra", nullable = false, precision = 6, scale = 2)
    private BigDecimal valorCompra;

    @Column(name = "fidelidade_ate", nullable = false)
    private LocalDateTime fidelidadeAte;

    @Column(nullable = false, length = 12)
    private String situacao = "ATIVO"; 

    @Column(name = "iniciado_em", nullable = false)
    private LocalDateTime iniciadoEm;

    @Column(name = "encerrado_em")
    private LocalDateTime encerradoEm;

    public Contrato() {
    }

    @PrePersist
    public void prePersist() {
        if (this.iniciadoEm == null) {
            this.iniciadoEm = LocalDateTime.now();
        }
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Jogador getJogador() { return jogador; }
    public void setJogador(Jogador jogador) { this.jogador = jogador; }

    public Clube getClube() { return clube; }
    public void setClube(Clube clube) { this.clube = clube; }

    public BigDecimal getValorCompra() { return valorCompra; }
    public void setValorCompra(BigDecimal valorCompra) { this.valorCompra = valorCompra; }

    public LocalDateTime getFidelidadeAte() { return fidelidadeAte; }
    public void setFidelidadeAte(LocalDateTime fidelidadeAte) { this.fidelidadeAte = fidelidadeAte; }

    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }

    public LocalDateTime getIniciadoEm() { return iniciadoEm; }
    public void setIniciadoEm(LocalDateTime iniciadoEm) { this.iniciadoEm = iniciadoEm; }

    public LocalDateTime getEncerradoEm() { return encerradoEm; }
    public void setEncerradoEm(LocalDateTime encerradoEm) { this.encerradoEm = encerradoEm; }
}