package com.senai.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senai.backend.dto.request.CondicaoRequest;
import com.senai.backend.dto.request.JogadorCreateRequest;
import com.senai.backend.dto.request.JogadorUpdateRequest;
import com.senai.backend.model.Clube;
import com.senai.backend.model.Escalacao;
import com.senai.backend.model.Jogador;
import com.senai.backend.repository.ClubeRepository;
import com.senai.backend.repository.EscalacaoRepository;
import com.senai.backend.repository.JogadorRepository;
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
class JogadorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JogadorRepository jogadorRepository;

    @Autowired
    private ClubeRepository clubeRepository;

    @Autowired
    private EscalacaoRepository escalacaoRepository;

    private Jogador jogadorFree;
    private Jogador jogadorComClube;
    private Clube clube;

    @BeforeEach
    void setUp() {
        escalacaoRepository.deleteAll();
        jogadorRepository.deleteAll();
        clubeRepository.deleteAll();

        clube = new Clube();
        clube.setNome("Sport Club do Recife");
        clube.setNomeNormalizado("sport club do recife");
        clube.setSigla("SCR");
        clube.setCaixa(new BigDecimal("100.00"));
        clube = clubeRepository.save(clube);

        jogadorFree = new Jogador();
        jogadorFree.setNome("Lucas Silva");
        jogadorFree.setPosicao("MEI");
        jogadorFree.setAgilidade(80);
        jogadorFree.setIdade(22);
        jogadorFree.setVelocidade(75);
        jogadorFree.setFinalizacao(70);
        jogadorFree.setPasse(80);
        jogadorFree.setDrible(78);
        jogadorFree.setDefesa(60);
        jogadorFree.setFisico(72);
        jogadorFree.setPotencial(85);
        jogadorFree = jogadorRepository.save(jogadorFree);

        jogadorComClube = new Jogador();
        jogadorComClube.setNome("Carlos Souza");
        jogadorComClube.setPosicao("ATA");
        jogadorComClube.setAgilidade(85);
        jogadorComClube.setIdade(26);
        jogadorComClube.setVelocidade(88);
        jogadorComClube.setFinalizacao(84);
        jogadorComClube.setPasse(70);
        jogadorComClube.setDrible(82);
        jogadorComClube.setDefesa(40);
        jogadorComClube.setFisico(75);
        jogadorComClube.setPotencial(89);
        jogadorComClube.setClube(clube);
        jogadorComClube = jogadorRepository.save(jogadorComClube);
    }

    @Test
    @DisplayName("POST /api/v1/players deve criar jogador com sucesso e retornar 201 com Location header")
    void deveCriarJogadorComSucesso() throws Exception {
        JogadorCreateRequest req = new JogadorCreateRequest();
        req.setName("Gabriel Gol");
        req.setPosition("ATA");
        req.setAgility(82);
        req.setAge(24);
        req.setPace(80);
        req.setShooting(80);
        req.setPassing(70);
        req.setDribbling(75);
        req.setDefending(50);
        req.setPhysical(70);
        req.setPotential(85);

        mockMvc.perform(post("/api/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/players/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Gabriel Gol"))
                .andExpect(jsonPath("$.position").value("ATA"))
                .andExpect(jsonPath("$.overall").isNumber())
                .andExpect(jsonPath("$.base_value").isNumber())
                .andExpect(jsonPath("$.market_value").isNumber());
    }

    @Test
    @DisplayName("GET /api/v1/players com paginação e busca por nome sem acento")
    void deveListarJogadoresComPaginacaoEBusca() throws Exception {
        mockMvc.perform(get("/api/v1/players")
                        .param("search", "lucas")
                        .param("page", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("Lucas Silva"))
                .andExpect(jsonPath("$.meta.total_items").value(1))
                .andExpect(jsonPath("$.meta.page").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/players com filtros conflitantes deve retornar 400")
    void deveRetornar400QuandoFiltrosConflitantes() throws Exception {
        mockMvc.perform(get("/api/v1/players")
                        .param("free", "true")
                        .param("club_id", clube.getId().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("PUT /api/v1/players/{id} com alteração de posição em jogador de clube deve retornar 409")
    void deveRetornar409AoAlterarPosicaoDeJogadorComClube() throws Exception {
        JogadorUpdateRequest req = new JogadorUpdateRequest();
        req.setName("Carlos Souza");
        req.setPosition("MEI"); // Era ATA, tem clube
        req.setAgility(85);
        req.setAge(26);

        mockMvc.perform(put("/api/v1/players/" + jogadorComClube.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Jogador com clube vinculado não pode ter sua posição alterada."));
    }

    @Test
    @DisplayName("PUT /api/v1/players/{id} com redução de idade deve retornar 409")
    void deveRetornar409AoReduzirIdade() throws Exception {
        JogadorUpdateRequest req = new JogadorUpdateRequest();
        req.setName("Lucas Silva");
        req.setPosition("MEI");
        req.setAgility(80);
        req.setAge(20); // Era 22, tentou diminuir para 20

        mockMvc.perform(put("/api/v1/players/" + jogadorFree.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A idade do jogador não pode diminuir."));
    }

    @Test
    @DisplayName("PATCH /api/v1/players/{id}/condition de DOENTE para APTO direto deve retornar 409")
    void deveRetornar409AoMudarDeDoenteParaAptoDireto() throws Exception {
        jogadorFree.setCondicao("DOENTE");
        jogadorRepository.save(jogadorFree);

        CondicaoRequest req = new CondicaoRequest();
        req.setCondition("APTO");

        mockMvc.perform(patch("/api/v1/players/" + jogadorFree.getId() + "/condition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Transição direta de DOENTE para APTO não é permitida")));
    }

    @Test
    @DisplayName("PATCH /api/v1/players/{id}/condition para DOENTE deve remover automaticamente da escalação")
    void deveRemoverDaEscalacaoAoMudarParaDoente() throws Exception {
        escalacaoRepository.save(new Escalacao(clube.getId(), jogadorComClube.getId()));

        CondicaoRequest req = new CondicaoRequest();
        req.setCondition("DOENTE");

        mockMvc.perform(patch("/api/v1/players/" + jogadorComClube.getId() + "/condition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.condition").value("DOENTE"));

        boolean aindaEscalado = escalacaoRepository.existsByJogadorId(jogadorComClube.getId());
        org.junit.jupiter.api.Assertions.assertFalse(aindaEscalado, "Jogador DOENTE não pode continuar escalado.");
    }

    @Test
    @DisplayName("DELETE /api/v1/players/{id} com jogador vinculado a clube deve retornar 409")
    void deveRetornar409AoExcluirJogadorComClube() throws Exception {
        mockMvc.perform(delete("/api/v1/players/" + jogadorComClube.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Jogador vinculado a um clube não pode ser excluído."));
    }

    @Test
    @DisplayName("DELETE /api/v1/players/{id} com jogador livre realiza soft-delete e subsequente GET retorna 404")
    void deveRealizarSoftDeleteEmJogadorLivre() throws Exception {
        mockMvc.perform(delete("/api/v1/players/" + jogadorFree.getId()))
                .andExpect(status().isNoContent());

        // Agora buscar deve retornar 404
        mockMvc.perform(get("/api/v1/players/" + jogadorFree.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
