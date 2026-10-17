package com.senai.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senai.backend.dto.request.LineupRequest;
import com.senai.backend.dto.request.MatchRequest;
import com.senai.backend.model.Clube;
import com.senai.backend.model.Jogador;
import com.senai.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PartidaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClubeRepository clubeRepository;

    @Autowired
    private JogadorRepository jogadorRepository;

    @Autowired
    private EscalacaoRepository escalacaoRepository;

    @Autowired
    private DueloPartidaRepository dueloPartidaRepository;

    @Autowired
    private PartidaRepository partidaRepository;

    private Clube clubeMandante;
    private Clube clubeVisitante;
    private List<Jogador> elencoMandante;
    private List<Jogador> elencoVisitante;

    @BeforeEach
    void setUp() {
        dueloPartidaRepository.deleteAll();
        partidaRepository.deleteAll();
        escalacaoRepository.deleteAll();
        jogadorRepository.deleteAll();
        clubeRepository.deleteAll();

        clubeMandante = new Clube();
        clubeMandante.setNome("Águias FC");
        clubeMandante.setNomeNormalizado("aguias fc");
        clubeMandante.setSigla("AFC");
        clubeMandante.setCaixa(new BigDecimal("300.00"));
        clubeMandante = clubeRepository.save(clubeMandante);

        clubeVisitante = new Clube();
        clubeVisitante.setNome("Delta FC");
        clubeVisitante.setNomeNormalizado("delta fc");
        clubeVisitante.setSigla("DFC");
        clubeVisitante.setCaixa(new BigDecimal("300.00"));
        clubeVisitante = clubeRepository.save(clubeVisitante);

        // Cria elenco mandante: 1 GOL, 1 ZAG, 1 LAT, 1 MEI, 1 ATA (Formação 2-1-1)
        elencoMandante = new ArrayList<>();
        elencoMandante.add(criarJogador("Rui Prado", "GOL", 77, 77, clubeMandante));
        elencoMandante.add(criarJogador("Téo Brandão", "ZAG", 84, 84, clubeMandante));
        elencoMandante.add(criarJogador("Davi Lins", "LAT", 74, 66, clubeMandante));
        elencoMandante.add(criarJogador("Lauro Neves", "MEI", 82, 82, clubeMandante));
        elencoMandante.add(criarJogador("Otto Rangel", "ATA", 75, 75, clubeMandante));

        // Cria elenco visitante: 1 GOL, 1 ZAG, 1 LAT, 1 MEI, 1 ATA
        elencoVisitante = new ArrayList<>();
        elencoVisitante.add(criarJogador("Ivo Teles", "GOL", 72, 72, clubeVisitante));
        elencoVisitante.add(criarJogador("Hugo Vaz", "ZAG", 84, 84, clubeVisitante));
        elencoVisitante.add(criarJogador("Lino Matos", "LAT", 79, 63, clubeVisitante));
        elencoVisitante.add(criarJogador("Nei Soares", "MEI", 70, 70, clubeVisitante));
        elencoVisitante.add(criarJogador("Ugo Falcão", "ATA", 81, 81, clubeVisitante));
    }

    private Jogador criarJogador(String nome, String posicao, int attrAtacante, int defesa, Clube clube) {
        Jogador j = new Jogador();
        j.setNome(nome);
        j.setPosicao(posicao);
        j.setAgilidade(80);
        j.setIdade(24);
        j.setVelocidade(75);
        j.setFinalizacao(attrAtacante);
        j.setPasse(attrAtacante);
        j.setDrible(attrAtacante);
        j.setDefesa(defesa);
        j.setFisico(75);
        j.setPotencial(85);
        j.setCondicao("APTO");
        j.setClube(clube);
        return jogadorRepository.save(j);
    }

    @Test
    @DisplayName("PUT /api/v1/clubs/{id}/lineup define escalação com 5 titulares na formação 2-1-1")
    void deveDefinirEscalacaoComSucesso() throws Exception {
        LineupRequest req = new LineupRequest();
        req.setFormation("2-1-1");
        req.setPlayerIds(elencoMandante.stream().map(Jogador::getId).toList());

        mockMvc.perform(put("/api/v1/clubs/" + clubeMandante.getId() + "/lineup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.club_id").value(clubeMandante.getId()))
                .andExpect(jsonPath("$.formation").value("2-1-1"))
                .andExpect(jsonPath("$.complete").value(true))
                .andExpect(jsonPath("$.players", hasSize(5)))
                // Goleiro primeiro
                .andExpect(jsonPath("$.players[0].position").value("GOL"));
    }

    @Test
    @DisplayName("PUT /api/v1/clubs/{id}/lineup retorna 409 quando posições não fecham com a formação")
    void deveRetornar409QuandoPosicoesNaoFechamFormacao() throws Exception {
        LineupRequest req = new LineupRequest();
        req.setFormation("3-1"); // Requer 3 defensores, mas mandante só tem 2 (ZAG e LAT) e 1 MEI
        req.setPlayerIds(elencoMandante.stream().map(Jogador::getId).toList());

        mockMvc.perform(put("/api/v1/clubs/" + clubeMandante.getId() + "/lineup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("formação 3-1")));
    }

    @Test
    @DisplayName("POST /api/v1/matches executa partida com 6 duelos, grava placar e retorna 201 com Location")
    void deveJogarPartidaComSucesso() throws Exception {
        // Escala mandante no 2-1-1
        LineupRequest reqMandante = new LineupRequest();
        reqMandante.setFormation("2-1-1");
        reqMandante.setPlayerIds(elencoMandante.stream().map(Jogador::getId).toList());
        mockMvc.perform(put("/api/v1/clubs/" + clubeMandante.getId() + "/lineup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqMandante))).andExpect(status().isOk());

        // Escala visitante no 2-1-1
        LineupRequest reqVisitante = new LineupRequest();
        reqVisitante.setFormation("2-1-1");
        reqVisitante.setPlayerIds(elencoVisitante.stream().map(Jogador::getId).toList());
        mockMvc.perform(put("/api/v1/clubs/" + clubeVisitante.getId() + "/lineup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqVisitante))).andExpect(status().isOk());

        // Joga a partida
        MatchRequest matchReq = new MatchRequest();
        matchReq.setHomeClubId(clubeMandante.getId());
        matchReq.setAwayClubId(clubeVisitante.getId());

        mockMvc.perform(post("/api/v1/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(matchReq)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/matches/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.home.club_id").value(clubeMandante.getId()))
                .andExpect(jsonPath("$.away.club_id").value(clubeVisitante.getId()))
                .andExpect(jsonPath("$.home.goals").isNumber())
                .andExpect(jsonPath("$.away.goals").isNumber())
                .andExpect(jsonPath("$.result").isNotEmpty())
                .andExpect(jsonPath("$.duels", hasSize(6)))
                .andExpect(jsonPath("$.duels[0].order").value(1))
                .andExpect(jsonPath("$.duels[5].order").value(6));
    }

    @Test
    @DisplayName("GET /api/v1/standings calcula classificação de todos os clubes ordenados por pontos, saldo, gols e nome")
    void deveObterClassificacaoCorretamente() throws Exception {
        mockMvc.perform(get("/api/v1/standings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].points").value(0))
                .andExpect(jsonPath("$[0].position").value(1))
                .andExpect(jsonPath("$[1].position").value(2));
    }
}
