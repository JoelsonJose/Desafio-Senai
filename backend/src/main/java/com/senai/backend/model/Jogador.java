package com.senai.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jogadores")
public class Jogador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 3)
    private String posicao;

    @Column(nullable = false)
    private Integer agilidade;

    @Column(nullable = false)
    private Integer idade;

    @Column(name = "valor_base", nullable = false, columnDefinition = "NUMERIC")
    private Double valorBase;

    @Column(nullable = false, length = 20)
    private String condicao = "APTO";

    @ManyToOne
    @JoinColumn(name = "clube_id")
    private Clube clube;

    @Column(nullable = false)
    private Integer velocidade;

    @Column(nullable = false)
    private Integer finalizacao;

    @Column(nullable = false)
    private Integer passe;

    @Column(nullable = false)
    private Integer drible;

    @Column(nullable = false)
    private Integer defesa;

    @Column(nullable = false)
    private Integer fisico;

    @Column(nullable = false)
    private Integer potencial;

    @Column(name = "excluido_em")
    private LocalDateTime excluidoEm;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    public Jogador() {
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPosicao() {
        return posicao;
    }

    public void setPosicao(String posicao) {
        this.posicao = posicao;
    }

    public Integer getAgilidade() {
        return agilidade;
    }

    public void setAgilidade(Integer agilidade) {
        this.agilidade = agilidade;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public Double getValorBase() {
        return valorBase;
    }

    public void setValorBase(Double valorBase) {
        this.valorBase = valorBase;
    }

    public String getCondicao() {
        return condicao;
    }

    public void setCondicao(String condicao) {
        this.condicao = condicao;
    }

    public Clube getClube() {
        return clube;
    }

    public void setClube(Clube clube) {
        this.clube = clube;
    }

    public Integer getVelocidade() {
        return velocidade;
    }

    public void setVelocidade(Integer velocidade) {
        this.velocidade = velocidade;
    }

    public Integer getFinalizacao() {
        return finalizacao;
    }

    public void setFinalizacao(Integer finalizacao) {
        this.finalizacao = finalizacao;
    }

    public Integer getPasse() {
        return passe;
    }

    public void setPasse(Integer passe) {
        this.passe = passe;
    }

    public Integer getDrible() {
        return drible;
    }

    public void setDrible(Integer drible) {
        this.drible = drible;
    }

    public Integer getDefesa() {
        return defesa;
    }

    public void setDefesa(Integer defesa) {
        this.defesa = defesa;
    }

    public Integer getFisico() {
        return fisico;
    }

    public void setFisico(Integer fisico) {
        this.fisico = fisico;
    }

    public Integer getPotencial() {
        return potencial;
    }

    public void setPotencial(Integer potencial) {
        this.potencial = potencial;
    }

    public LocalDateTime getExcluidoEm() {
        return excluidoEm;
    }

    public void setExcluidoEm(LocalDateTime excluidoEm) {
        this.excluidoEm = excluidoEm;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}