package com.senai.backend.service;

import com.senai.backend.dto.request.CompraRequest;
import com.senai.backend.dto.response.*;
import com.senai.backend.exception.BusinessException;
import com.senai.backend.exception.ResourceNotFoundException;
import com.senai.backend.model.*;
import com.senai.backend.repository.*;
import com.senai.backend.util.DateTimeUtils;
import com.senai.backend.util.TransacaoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MercadoService {

    private final ClubeRepository clubeRepository;
    private final JogadorRepository jogadorRepository;
    private final ContratoRepository contratoRepository;
    private final TransacaoRepository transacaoRepository;
    private final EscalacaoRepository escalacaoRepository;

    public MercadoService(ClubeRepository clubeRepository,
                          JogadorRepository jogadorRepository,
                          ContratoRepository contratoRepository,
                          TransacaoRepository transacaoRepository,
                          EscalacaoRepository escalacaoRepository) {
        this.clubeRepository = clubeRepository;
        this.jogadorRepository = jogadorRepository;
        this.contratoRepository = contratoRepository;
        this.transacaoRepository = transacaoRepository;
        this.escalacaoRepository = escalacaoRepository;
    }

    @Transactional
    public CompraResponse comprarJogador(Integer jogadorId, CompraRequest request) {
        OffsetDateTime parsedLoyalty = request.validateAndGetLoyaltyUntil();
        LocalDateTime fidelidadeAte = parsedLoyalty.toLocalDateTime();
        Integer clubeId = request.getClubId();

        Clube clube = clubeRepository.findById(clubeId)
                .orElseThrow(() -> new ResourceNotFoundException("Clube não encontrado com o ID: " + clubeId));

        Jogador jogador = jogadorRepository.findByIdAndExcluidoEmIsNull(jogadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado com o ID: " + jogadorId));

        // RN13: Jogador já pertence a um clube -> 409 Conflict
        if (jogador.getClube() != null) {
            throw new BusinessException("O jogador já possui vínculo com outro clube.");
        }

        // RN13: Recompra proibida se o mesmo clube já vendeu esse jogador -> 409 Conflict
        boolean jaVendeuAntes = transacaoRepository.findByClubeIdOrderByCriadoEmDescIdDesc(clubeId).stream()
                .anyMatch(t -> t.getJogador().getId().equals(jogadorId) && "VENDA".equalsIgnoreCase(t.getTipo()));
        if (jaVendeuAntes) {
            throw new BusinessException("Um clube não pode recomprar um jogador que ele mesmo já vendeu anteriormente.");
        }

        // RN13: Elenco máximo de 12 jogadores -> 409 Conflict
        List<Jogador> elencoAtual = jogadorRepository.findByClubeIdAndExcluidoEmIsNull(clubeId);
        if (elencoAtual.size() >= 12) {
            throw new BusinessException("O clube já atingiu o limite máximo de 12 jogadores no elenco.");
        }

        BigDecimal valorMercado = jogador.getValorMercado();
        BigDecimal taxa = BigDecimal.ZERO;

        // RN13: Se o valor de mercado for estritamente maior que R$ 30,00, taxa federativa de 5%
        if (valorMercado.compareTo(new BigDecimal("30.00")) > 0) {
            taxa = valorMercado.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal custoTotal = valorMercado.add(taxa);

        // RN13: Custo total não pode ultrapassar 50% do caixa disponível do clube -> 409 Conflict
        BigDecimal limiteCaixa = clube.getCaixa().multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
        if (custoTotal.compareTo(limiteCaixa) > 0) {
            throw new BusinessException("O custo total da operação (" + custoTotal + ") ultrapassa o limite permitido de 50% do caixa do clube (" + limiteCaixa + ").");
        }

        // Atualiza caixa do clube
        clube.setCaixa(clube.getCaixa().subtract(custoTotal).setScale(2, RoundingMode.HALF_UP));
        clubeRepository.save(clube);

        // Vincula jogador ao clube
        jogador.setClube(clube);
        jogadorRepository.save(jogador);

        // Cria contrato de fidelidade
        Contrato contrato = new Contrato();
        contrato.setJogador(jogador);
        contrato.setClube(clube);
        contrato.setValorCompra(valorMercado);
        contrato.setFidelidadeAte(fidelidadeAte);
        contrato.setSituacao("ATIVO");
        contrato = contratoRepository.save(contrato);

        // Registra transação
        Transacao transacao = new Transacao();
        transacao.setClube(clube);
        transacao.setJogador(jogador);
        transacao.setNomeJogador(jogador.getNome());
        transacao.setTipo("COMPRA");
        transacao.setValor(valorMercado);
        transacao.setTaxa(taxa);
        transacao.setMulta(BigDecimal.ZERO);
        transacao.setLucro(null);
        transacao.setVariacaoCaixa(custoTotal.negate());
        transacao = transacaoRepository.save(transacao);

        return TransacaoMapper.toCompraResponse(transacao, contrato, clube.getCaixa());
    }

    @Transactional
    public VendaResponse venderJogador(Integer jogadorId) {
        Jogador jogador = jogadorRepository.findByIdAndExcluidoEmIsNull(jogadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado com o ID: " + jogadorId));

        if (jogador.getClube() == null) {
            throw new BusinessException("O jogador é um agente livre e não pertence a nenhum clube para poder ser vendido.");
        }

        Clube clube = jogador.getClube();

        Contrato contrato = contratoRepository.findByJogadorIdAndSituacao(jogadorId, "ATIVO")
                .orElseThrow(() -> new ResourceNotFoundException("Contrato ativo não encontrado para o jogador informado."));

        LocalDateTime agora = LocalDateTime.now();
        BigDecimal valorVenda = jogador.getValorMercado();

        // Cálculo dos dias restantes de fidelidade (RN14)
        long horasRestantes = Duration.between(agora, contrato.getFidelidadeAte()).toHours();
        long diasRestantes = 0;
        if (horasRestantes > 0) {
            diasRestantes = (long) Math.ceil((double) horasRestantes / 24.0);
        }

        int percentualMulta = 0;
        if (diasRestantes >= 1 && diasRestantes <= 30) {
            percentualMulta = 5;
        } else if (diasRestantes >= 31 && diasRestantes <= 180) {
            percentualMulta = 10;
        } else if (diasRestantes >= 181) {
            percentualMulta = 20;
        }

        // Multa rescisória com teto de R$ 10,00
        BigDecimal multa = valorVenda.multiply(new BigDecimal(percentualMulta))
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        if (multa.compareTo(new BigDecimal("10.00")) > 0) {
            multa = new BigDecimal("10.00");
        }

        String novaSituacaoContrato = (percentualMulta > 0) ? "RESCINDIDO" : "ENCERRADO";

        // Cálculo de lucro e comissão federativa escalonada (RN14)
        BigDecimal valorCompra = contrato.getValorCompra();
        BigDecimal lucro = valorVenda.subtract(valorCompra);

        BigDecimal comissao = BigDecimal.ZERO;
        if (lucro.compareTo(BigDecimal.ZERO) > 0) {
            if (lucro.compareTo(new BigDecimal("10.00")) <= 0) {
                comissao = lucro.multiply(new BigDecimal("0.10"));
            } else {
                BigDecimal faixa1 = new BigDecimal("10.00").multiply(new BigDecimal("0.10"));
                BigDecimal faixa2 = lucro.subtract(new BigDecimal("10.00")).multiply(new BigDecimal("0.20"));
                comissao = faixa1.add(faixa2);
            }
            comissao = comissao.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal creditoCaixa = valorVenda.subtract(multa).subtract(comissao).setScale(2, RoundingMode.HALF_UP);

        // Atualiza caixa do clube
        clube.setCaixa(clube.getCaixa().add(creditoCaixa).setScale(2, RoundingMode.HALF_UP));
        clubeRepository.save(clube);

        // Encerra ou rescinde contrato
        contrato.setSituacao(novaSituacaoContrato);
        contrato.setEncerradoEm(agora);
        contratoRepository.save(contrato);

        // Se o jogador estava escalado pelo clube, remove-o da escalação
        escalacaoRepository.deleteByJogadorId(jogadorId);

        // Desvincula jogador do clube (torna-se agente livre)
        jogador.setClube(null);
        jogadorRepository.save(jogador);

        // Registra transação
        Transacao transacao = new Transacao();
        transacao.setClube(clube);
        transacao.setJogador(jogador);
        transacao.setNomeJogador(jogador.getNome());
        transacao.setTipo("VENDA");
        transacao.setValor(valorVenda);
        transacao.setTaxa(comissao);
        transacao.setMulta(multa);
        transacao.setLucro(lucro);
        transacao.setVariacaoCaixa(creditoCaixa);
        transacao = transacaoRepository.save(transacao);

        return TransacaoMapper.toVendaResponse(
                transacao,
                valorCompra,
                diasRestantes,
                percentualMulta,
                novaSituacaoContrato,
                clube.getCaixa()
        );
    }

    @Transactional(readOnly = true)
    public TransacoesListResponse listarTransacoesClube(Integer clubeId, String type, Integer playerId) {
        List<Transacao> filtradas = obterTransacoesFiltradas(clubeId, type, playerId);

        BigDecimal totalSpent = BigDecimal.ZERO;
        BigDecimal totalReceived = BigDecimal.ZERO;
        BigDecimal totalFees = BigDecimal.ZERO;
        BigDecimal totalPenalties = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;

        for (Transacao t : filtradas) {
            if ("COMPRA".equalsIgnoreCase(t.getTipo())) {
                totalSpent = totalSpent.add(t.getValor().add(t.getTaxa()));
                totalFees = totalFees.add(t.getTaxa());
            } else if ("VENDA".equalsIgnoreCase(t.getTipo())) {
                totalReceived = totalReceived.add(t.getVariacaoCaixa());
                totalFees = totalFees.add(t.getTaxa());
                totalPenalties = totalPenalties.add(t.getMulta());
                if (t.getLucro() != null) {
                    totalProfit = totalProfit.add(t.getLucro());
                }
            }
        }

        TransacoesSummaryResponse summary = new TransacoesSummaryResponse(
                totalSpent.setScale(2, RoundingMode.HALF_UP),
                totalReceived.setScale(2, RoundingMode.HALF_UP),
                totalFees.setScale(2, RoundingMode.HALF_UP),
                totalPenalties.setScale(2, RoundingMode.HALF_UP),
                totalProfit.setScale(2, RoundingMode.HALF_UP)
        );

        List<TransacaoResponse> dtoList = filtradas.stream()
                .map(TransacaoMapper::toResponse)
                .collect(Collectors.toList());

        return new TransacoesListResponse(dtoList, summary);
    }

    @Transactional(readOnly = true)
    public byte[] exportarTransacoesCsv(Integer clubeId, String type, Integer playerId) {
        List<Transacao> filtradas = obterTransacoesFiltradas(clubeId, type, playerId);

        StringBuilder sb = new StringBuilder();
        // Cabeçalho da RN18
        sb.append("data_hora;tipo;jogador;valor;taxa_ou_comissao;multa;lucro;variacao_caixa\n");

        for (Transacao t : filtradas) {
            String dataHora = DateTimeUtils.formatResponse(t.getCriadoEm());
            String tipo = t.getTipo();
            String nome = formatarNomeCsv(t.getNomeJogador());
            String valor = formatarMoedaCsv(t.getValor());
            String taxa = formatarMoedaCsv(t.getTaxa());
            String multa = formatarMoedaCsv(t.getMulta());
            String lucro = t.getLucro() != null ? formatarMoedaCsv(t.getLucro()) : "";
            String variacao = formatarMoedaCsv(t.getVariacaoCaixa());

            sb.append(dataHora).append(";")
                    .append(tipo).append(";")
                    .append(nome).append(";")
                    .append(valor).append(";")
                    .append(taxa).append(";")
                    .append(multa).append(";")
                    .append(lucro).append(";")
                    .append(variacao).append("\n");
        }

        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private List<Transacao> obterTransacoesFiltradas(Integer clubeId, String type, Integer playerId) {
        if (!clubeRepository.existsById(clubeId)) {
            throw new ResourceNotFoundException("Clube não encontrado com o ID: " + clubeId);
        }

        if (playerId != null) {
            // Jogador inexistente ou excluído -> 404
            jogadorRepository.findByIdAndExcluidoEmIsNull(playerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado com o ID: " + playerId));
        }

        Set<String> tiposFiltro = null;
        if (type != null) {
            if (type.trim().isEmpty()) {
                throw new IllegalArgumentException("O parâmetro type não pode ser vazio.");
            }
            String[] split = type.split(",");
            tiposFiltro = new HashSet<>();
            for (String s : split) {
                // RN17: case-sensitive estrito, sem espaços
                if (!s.equals("COMPRA") && !s.equals("VENDA")) {
                    throw new IllegalArgumentException("Tipo de transação inválido: " + s + ". Valores aceitos: COMPRA, VENDA.");
                }
                if (!tiposFiltro.add(s)) {
                    throw new IllegalArgumentException("O parâmetro type não pode conter valores repetidos.");
                }
            }
        }

        List<Transacao> transacoes = transacaoRepository.findByClubeIdOrderByCriadoEmDescIdDesc(clubeId);
        final Set<String> tiposFinal = tiposFiltro;

        return transacoes.stream().filter(t -> {
            if (tiposFinal != null && !tiposFinal.contains(t.getTipo())) {
                return false;
            }
            if (playerId != null && !t.getJogador().getId().equals(playerId)) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    private String formatarNomeCsv(String nome) {
        if (nome == null) return "";
        if (nome.contains(";") || nome.contains("\"")) {
            return "\"" + nome.replace("\"", "\"\"") + "\"";
        }
        return nome;
    }

    private String formatarMoedaCsv(BigDecimal valor) {
        if (valor == null) return "0,00";
        return valor.setScale(2, RoundingMode.HALF_UP).toString().replace(".", ",");
    }
}