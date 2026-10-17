package com.senai.backend.model;

import com.senai.backend.domain.calculator.MarketValueCalculator;
import com.senai.backend.domain.calculator.OverallCalculator;
import jakarta.persistence.*;
import java.math.BigDecimal;
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

    @Column(name = "valor_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorBase;

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

    @Transient
    private Integer overall;

    @Transient
    private BigDecimal valorMercado;

    @Column(name = "excluido_em")
    private LocalDateTime excluidoEm;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public Jogador() {
    }

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
        if (this.atualizadoEm == null) {
            this.atualizadoEm = LocalDateTime.now();
        }
        calcularAtributosCadastro();
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
        calcularAtributosDinamicos();
    }

    @PostLoad
    public void postLoad() {
        calcularAtributosDinamicos();
    }

    public void calcularAtributosCadastro() {
        calcularAtributosDinamicos();
        if (this.overall != null && this.potencial != null && this.idade != null && this.valorBase == null) {
            this.valorBase = MarketValueCalculator.calcularValorBase(
                    this.overall, this.potencial, this.idade
            );
        }
        if (this.valorBase != null && this.condicao != null && this.idade != null && this.agilidade != null) {
            this.valorMercado = MarketValueCalculator.calcularValorMercado(
                    this.valorBase, this.condicao, this.idade, this.agilidade
            );
        }
    }

    public void calcularAtributosDinamicos() {
        if (this.posicao != null && this.velocidade != null && this.finalizacao != null &&
            this.passe != null && this.drible != null && this.defesa != null && this.fisico != null) {
            
            this.overall = OverallCalculator.calcular(
                    this.posicao, this.velocidade, this.finalizacao, 
                    this.passe, this.drible, this.defesa, this.fisico
            );
        }

        if (this.valorBase != null && this.condicao != null && this.idade != null && this.agilidade != null) {
            this.valorMercado = MarketValueCalculator.calcularValorMercado(
                    this.valorBase, this.condicao, this.idade, this.agilidade
            );
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getPosicao() { return posicao; }
    public void setPosicao(String posicao) { this.posicao = posicao; }

    public Integer getAgilidade() { return agilidade; }
    public void setAgilidade(Integer agilidade) { this.agilidade = agilidade; }

    public Integer getIdade() { return idade; }
    public void setIdade(Integer idade) { this.idade = idade; }

    public BigDecimal getValorBase() { return valorBase; }
    public void setValorBase(BigDecimal valorBase) { this.valorBase = valorBase; }

    public String getCondicao() { return condicao; }
    public void setCondicao(String condicao) { this.condicao = condicao; }

    public Clube getClube() { return clube; }
    public void setClube(Clube clube) { this.clube = clube; }

    public Integer getVelocidade() { return velocidade; }
    public void setVelocidade(Integer velocidade) { this.velocidade = velocidade; }

    public Integer getFinalizacao() { return finalizacao; }
    public void setFinalizacao(Integer finalizacao) { this.finalizacao = finalizacao; }

    public Integer getPasse() { return passe; }
    public void setPasse(Integer passe) { this.passe = passe; }

    public Integer getDrible() { return drible; }
    public void setDrible(Integer drible) { this.drible = drible; }

    public Integer getDefesa() { return defesa; }
    public void setDefesa(Integer defesa) { this.defesa = defesa; }

    public Integer getFisico() { return fisico; }
    public void setFisico(Integer fisico) { this.fisico = fisico; }

    public Integer getPotencial() { return potencial; }
    public void setPotencial(Integer potencial) { this.potencial = potencial; }

    public Integer getOverall() { 
        if (this.overall == null) {
            calcularAtributosDinamicos();
        }
        return overall; 
    }
    public void setOverall(Integer overall) { this.overall = overall; }

    public BigDecimal getValorMercado() { 
        if (this.valorMercado == null) {
            calcularAtributosDinamicos();
        }
        return valorMercado; 
    }
    public void setValorMercado(BigDecimal valorMercado) { this.valorMercado = valorMercado; }

    public LocalDateTime getExcluidoEm() { return excluidoEm; }
    public void setExcluidoEm(LocalDateTime excluidoEm) { this.excluidoEm = excluidoEm; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}