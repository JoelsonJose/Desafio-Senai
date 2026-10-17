package com.senai.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "partidas")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "mandante_id", nullable = false)
    private Clube mandante;

    @ManyToOne
    @JoinColumn(name = "visitante_id", nullable = false)
    private Clube visitante;

    @Column(name = "gols_mandante", nullable = false)
    private Integer golsMandante;

    @Column(name = "gols_visitante", nullable = false)
    private Integer golsVisitante;

    @Column(name = "formacao_mandante", nullable = false, length = 5)
    private String formacaoMandante;

    @Column(name = "formacao_visitante", nullable = false, length = 5)
    private String formacaoVisitante;

    @Column(name = "jogada_em")
    private LocalDateTime jogadaEm;

    public Partida() {
    }

    @PrePersist
    public void prePersist() {
        if (this.jogadaEm == null) {
            this.jogadaEm = LocalDateTime.now();
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Clube getMandante() { return mandante; }
    public void setMandante(Clube mandante) { this.mandante = mandante; }

    public Clube getVisitante() { return visitante; }
    public void setVisitante(Clube visitante) { this.visitante = visitante; }

    public Integer getGolsMandante() { return golsMandante; }
    public void setGolsMandante(Integer golsMandante) { this.golsMandante = golsMandante; }

    public Integer getGolsVisitante() { return golsVisitante; }
    public void setGolsVisitante(Integer golsVisitante) { this.golsVisitante = golsVisitante; }

    public String getFormacaoMandante() { return formacaoMandante; }
    public void setFormacaoMandante(String formacaoMandante) { this.formacaoMandante = formacaoMandante; }

    public String getFormacaoVisitante() { return formacaoVisitante; }
    public void setFormacaoVisitante(String formacaoVisitante) { this.formacaoVisitante = formacaoVisitante; }

    public LocalDateTime getJogadaEm() { return jogadaEm; }
    public void setJogadaEm(LocalDateTime jogadaEm) { this.jogadaEm = jogadaEm; }
}
