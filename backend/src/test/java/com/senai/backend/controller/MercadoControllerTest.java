package com.senai.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senai.backend.dto.request.CompraRequest;
import com.senai.backend.model.Clube;
import com.senai.backend.model.Jogador;
import com.senai.backend.repository.ClubeRepository;
import com.senai.backend.repository.ContratoRepository;
import com.senai.backend.repository.JogadorRepository;
import com.senai.backend.repository.TransacaoRepository;
import com.senai.backend.util.DateTimeUtils;
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
import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MercadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClubeRepository clubeRepository;

    @Autowired
    private JogadorRepository jogadorRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    private Clube clubeComprador;
    private Jogador jogadorLivre;

    @BeforeEach
    void setUp() {
        transacaoRepository.deleteAll();
        contratoRepository.deleteAll();
        jogadorRepository.deleteAll();
        clubeRepository.deleteAll();

        clubeComprador = new Clube();
        clubeComprador.setNome("Sport Club do Recife");
        clubeComprador.setNomeNormalizado("sport club do recife");
        clubeComprador.setSigla("SCR");
        clubeComprador.setCaixa(new BigDecimal("300.00"));
        clubeComprador = clubeRepository.save(clubeComprador);

        jogadorLivre = new Jogador();
        jogadorLivre.setNome("Lucas Moura");
        jogadorLivre.setPosicao("ATA");
        jogadorLivre.setAgilidade(80);
        jogadorLivre.setIdade(24);
        jogadorLivre.setVelocidade(80);
        jogadorLivre.setFinalizacao(75);
        jogadorLivre.setPasse(70);
        jogadorLivre.setDrible(78);
        jogadorLivre.setDefesa(45);
        jogadorLivre.setFisico(70);
        jogadorLivre.setPotencial(85);
        jogadorLivre = jogadorRepository.save(jogadorLivre);
    }

    @Test
    @DisplayName("POST /api/v1/players/{id}/buy compra jogador livre com sucesso e atualiza caixa do clube")
    void deveComprarJogadorLivreComSucesso() throws Exception {
        OffsetDateTime fidelidade = DateTimeUtils.now().plusDays(60);
        String fidelidadeStr = DateTimeUtils.formatResponse(fidelidade).substring(0, 16) + "-03:00";

        CompraRequest req = new CompraRequest();
        req.setClubId(clubeComprador.getId());
        req.setLoyaltyUntil(fidelidadeStr);

        mockMvc.perform(post("/api/v1/players/" + jogadorLivre.getId() + "/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("COMPRA"))
                .andExpect(jsonPath("$.club_id").value(clubeComprador.getId()))
                .andExpect(jsonPath("$.player_id").value(jogadorLivre.getId()))
                .andExpect(jsonPath("$.value").isNumber())
                .andExpect(jsonPath("$.cash_change").isNumber())
                .andExpect(jsonPath("$.club_cash").isNumber())
                .andExpect(jsonPath("$.contract").isMap());
    }

    @Test
    @DisplayName("POST /api/v1/players/{id}/buy retorna 409 quando custo ultrapassa 50% do caixa")
    void deveRetornar409QuandoCompraUltrapassaCinquentaPorcentoDoCaixa() throws Exception {
        // Reduz caixa para R$ 10,00 (50% = R$ 5,00)
        clubeComprador.setCaixa(new BigDecimal("10.00"));
        clubeRepository.save(clubeComprador);

        OffsetDateTime fidelidade = DateTimeUtils.now().plusDays(60);
        String fidelidadeStr = DateTimeUtils.formatResponse(fidelidade).substring(0, 16) + "-03:00";

        CompraRequest req = new CompraRequest();
        req.setClubId(clubeComprador.getId());
        req.setLoyaltyUntil(fidelidadeStr);

        mockMvc.perform(post("/api/v1/players/" + jogadorLivre.getId() + "/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("ultrapassa o limite permitido de 50%")));
    }

    @Test
    @DisplayName("POST /api/v1/players/{id}/sell vende jogador de clube com sucesso, aplica comissão/multa e credita caixa")
    void deveVenderJogadorComSucesso() throws Exception {
        // Primeiro compra o jogador
        OffsetDateTime fidelidade = DateTimeUtils.now().plusDays(20); // 20 dias restantes -> 5% de multa
        String fidelidadeStr = DateTimeUtils.formatResponse(fidelidade).substring(0, 16) + "-03:00";

        CompraRequest compraReq = new CompraRequest();
        compraReq.setClubId(clubeComprador.getId());
        compraReq.setLoyaltyUntil(fidelidadeStr);

        mockMvc.perform(post("/api/v1/players/" + jogadorLivre.getId() + "/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compraReq)))
                .andExpect(status().isOk());

        // Agora realiza a venda
        mockMvc.perform(post("/api/v1/players/" + jogadorLivre.getId() + "/sell"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("VENDA"))
                .andExpect(jsonPath("$.player_id").value(jogadorLivre.getId()))
                .andExpect(jsonPath("$.penalty").isNumber())
                .andExpect(jsonPath("$.remaining_days").value(greaterThan(0)))
                .andExpect(jsonPath("$.contract_status").value("RESCINDIDO"));
    }

    @Test
    @DisplayName("POST /api/v1/players/{id}/buy retorna 409 quando clube tenta recomprar jogador vendido")
    void deveRetornar409AoTentarRecomprarJogadorVendido() throws Exception {
        OffsetDateTime fidelidade = DateTimeUtils.now().plusDays(20);
        String fidelidadeStr = DateTimeUtils.formatResponse(fidelidade).substring(0, 16) + "-03:00";

        CompraRequest compraReq = new CompraRequest();
        compraReq.setClubId(clubeComprador.getId());
        compraReq.setLoyaltyUntil(fidelidadeStr);

        // Compra e depois vende
        mockMvc.perform(post("/api/v1/players/" + jogadorLivre.getId() + "/buy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compraReq))).andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/players/" + jogadorLivre.getId() + "/sell")).andExpect(status().isOk());

        // Tenta recomprar com o mesmo clube
        mockMvc.perform(post("/api/v1/players/" + jogadorLivre.getId() + "/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compraReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("recomprar")));
    }

    @Test
    @DisplayName("GET /api/v1/clubs/{id}/transactions retorna histórico com lista e summary consolidado")
    void deveListarTransacoesComResumoConsolidado() throws Exception {
        OffsetDateTime fidelidade = DateTimeUtils.now().plusDays(40);
        String fidelidadeStr = DateTimeUtils.formatResponse(fidelidade).substring(0, 16) + "-03:00";

        CompraRequest compraReq = new CompraRequest();
        compraReq.setClubId(clubeComprador.getId());
        compraReq.setLoyaltyUntil(fidelidadeStr);

        // Gera uma transação de compra
        mockMvc.perform(post("/api/v1/players/" + jogadorLivre.getId() + "/buy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compraReq))).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/clubs/" + clubeComprador.getId() + "/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].type").value("COMPRA"))
                .andExpect(jsonPath("$.summary.total_spent").isNumber())
                .andExpect(jsonPath("$.summary.total_received").isNumber())
                .andExpect(jsonPath("$.summary.total_fees").isNumber())
                .andExpect(jsonPath("$.summary.total_penalties").isNumber())
                .andExpect(jsonPath("$.summary.total_profit").isNumber());
    }
}
