package com.senai.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senai.backend.dto.request.ClubeCreateRequest;
import com.senai.backend.model.Clube;
import com.senai.backend.model.Jogador;
import com.senai.backend.model.Partida;
import com.senai.backend.repository.ClubeRepository;
import com.senai.backend.repository.JogadorRepository;
import com.senai.backend.repository.PartidaRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClubeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClubeRepository clubeRepository;

    @Autowired
    private JogadorRepository jogadorRepository;

    @Autowired
    private PartidaRepository partidaRepository;

    @BeforeEach
    void setUp() {
        partidaRepository.deleteAll();
        jogadorRepository.deleteAll();
        clubeRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/clubs cria clube e retorna 201 com Location header e caixa inicial R$ 300,00")
    void deveCriarClubeComSucesso() throws Exception {
        ClubeCreateRequest req = new ClubeCreateRequest();
        req.setName("Sport Club do Recife");
        req.setAcronym("SCR");

        mockMvc.perform(post("/api/v1/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/clubs/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Sport Club do Recife"))
                .andExpect(jsonPath("$.acronym").value("SCR"))
                .andExpect(jsonPath("$.cash").value(300.00))
                .andExpect(jsonPath("$.squad_value").value(0))
                .andExpect(jsonPath("$.net_worth").value(300.00));
    }

    @Test
    @DisplayName("POST /api/v1/clubs com sigla duplicada retorna 409 Conflict")
    void deveRetornar409QuandoSiglaDuplicada() throws Exception {
        Clube existente = new Clube();
        existente.setNome("Sport Club do Recife");
        existente.setNomeNormalizado("sport club do recife");
        existente.setSigla("SCR");
        clubeRepository.save(existente);

        ClubeCreateRequest req = new ClubeCreateRequest();
        req.setName("Santa Cruz Recife");
        req.setAcronym("SCR"); // Mesma sigla

        mockMvc.perform(post("/api/v1/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("sigla")));
    }

    @Test
    @DisplayName("POST /api/v1/clubs com nome duplicado (mesmo variando acentos/espaços) retorna 409")
    void deveRetornar409QuandoNomeDuplicado() throws Exception {
        Clube existente = new Clube();
        existente.setNome("Sport Club do Recife");
        existente.setNomeNormalizado("sport club do recife");
        existente.setSigla("SCR");
        clubeRepository.save(existente);

        ClubeCreateRequest req = new ClubeCreateRequest();
        req.setName("  sport   club   do   recife  ");
        req.setAcronym("SCD");

        mockMvc.perform(post("/api/v1/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("nome")));
    }

    @Test
    @DisplayName("GET /api/v1/clubs/{id} retorna detalhes do clube com elenco ordenado por posição")
    void deveRetornarDetalhesDoClubeComElencoOrdenado() throws Exception {
        Clube clube = new Clube();
        clube.setNome("Sport Club do Recife");
        clube.setNomeNormalizado("sport club do recife");
        clube.setSigla("SCR");
        clube.setCaixa(new BigDecimal("300.00"));
        clube = clubeRepository.save(clube);

        Jogador atacante = new Jogador();
        atacante.setNome("Carlos Silva");
        atacante.setPosicao("ATA");
        atacante.setAgilidade(80);
        atacante.setIdade(25);
        atacante.setVelocidade(80);
        atacante.setFinalizacao(80);
        atacante.setPasse(70);
        atacante.setDrible(70);
        atacante.setDefesa(50);
        atacante.setFisico(70);
        atacante.setPotencial(85);
        atacante.setClube(clube);
        jogadorRepository.save(atacante);

        Jogador goleiro = new Jogador();
        goleiro.setNome("Marcos Goleiro");
        goleiro.setPosicao("GOL");
        goleiro.setAgilidade(80);
        goleiro.setIdade(28);
        goleiro.setVelocidade(60);
        goleiro.setFinalizacao(40);
        goleiro.setPasse(60);
        goleiro.setDrible(50);
        goleiro.setDefesa(85);
        goleiro.setFisico(80);
        goleiro.setPotencial(85);
        goleiro.setClube(clube);
        jogadorRepository.save(goleiro);

        mockMvc.perform(get("/api/v1/clubs/" + clube.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sport Club do Recife"))
                .andExpect(jsonPath("$.players", hasSize(2)))
                // GOL deve vir antes de ATA
                .andExpect(jsonPath("$.players[0].position").value("GOL"))
                .andExpect(jsonPath("$.players[1].position").value("ATA"));
    }

    @Test
    @DisplayName("DELETE /api/v1/clubs/{id} com jogadores no elenco retorna 409 Conflict")
    void deveRetornar409AoExcluirClubeComJogadores() throws Exception {
        Clube clube = new Clube();
        clube.setNome("Sport Club do Recife");
        clube.setNomeNormalizado("sport club do recife");
        clube.setSigla("SCR");
        clube = clubeRepository.save(clube);

        Jogador jogador = new Jogador();
        jogador.setNome("Carlos Silva");
        jogador.setPosicao("ATA");
        jogador.setAgilidade(80);
        jogador.setIdade(25);
        jogador.setVelocidade(80);
        jogador.setFinalizacao(80);
        jogador.setPasse(70);
        jogador.setDrible(70);
        jogador.setDefesa(50);
        jogador.setFisico(70);
        jogador.setPotencial(85);
        jogador.setClube(clube);
        jogadorRepository.save(jogador);

        mockMvc.perform(delete("/api/v1/clubs/" + clube.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("possui jogadores vinculados")));
    }

    @Test
    @DisplayName("DELETE /api/v1/clubs/{id} com clube sem jogadores e sem partidas retorna 204 No Content")
    void deveExcluirClubeVazioComSucesso() throws Exception {
        Clube clube = new Clube();
        clube.setNome("Sport Club do Recife");
        clube.setNomeNormalizado("sport club do recife");
        clube.setSigla("SCR");
        clube = clubeRepository.save(clube);

        mockMvc.perform(delete("/api/v1/clubs/" + clube.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/clubs/" + clube.getId()))
                .andExpect(status().isNotFound());
    }
}
