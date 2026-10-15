package com.senai.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity 
@Table(name = "clubes")
public class Clube {

@Id     
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer id;

@Column(nullable = false, length = 60)
private String nome;

@Column(nullable = false, length = 3, unique = true, columnDefinition = "bpchar(3)")
private String sigla;

@Column(nullable = false, columnDefinition = "NUMERIC")
private Double caixa = 300.00;

@Column(length = 5)
private String formacao;

@Column(name = "criado_em")
private LocalDateTime criado_em;

@PrePersist
public void prePersist() {
    if (this.criado_em == null) {
            this.criado_em = LocalDateTime.now();
    }
}

public Clube() {
}

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

public String getSigla() {
    return sigla;
}

public void setSigla(String sigla) {
    this.sigla = sigla;
}

public Double getCaixa() {
    return caixa;
}

public void setCaixa(Double caixa) {
    this.caixa = caixa;
}

public String getFormacao() {
    return formacao;
}

public void setFormacao(String formacao) {
    this.formacao = formacao;
}

public LocalDateTime getCriado_em() {
    return criado_em;
}

public void setCriado_em(LocalDateTime criado_em) {
    this.criado_em = criado_em;
}

}