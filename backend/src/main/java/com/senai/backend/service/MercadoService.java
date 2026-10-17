package com.senai.backend.service;

import com.senai.backend.exception.BusinessException;
import com.senai.backend.exception.ResourceNotFoundException;
import com.senai.backend.model.*;
import com.senai.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MercadoService {

    private final ClubeRepository clubeRepository;
    private final JogadorRepository jogadorRepository;
    private final ContratoRepository contratoRepository;
    private final TransacaoRepository transacaoRepository;

    public MercadoService(ClubeRepository clubeRepository,
                          JogadorRepository jogadorRepository,
                          ContratoRepository contratoRepository,
                          TransacaoRepository transacaoRepository) {
        this.clubeRepository = clubeRepository;
        this.jogadorRepository = jogadorRepository;
        this.contratoRepository = contratoRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional
    public Transacao comprarJogador(Integer jogadorId, Integer clubeId, LocalDateTime fidelidadeAte) {
        Clube clube = clubeRepository.findById(clubeId)
                .orElseThrow(() -> new ResourceNotFoundException("Clube não encontrado com o ID: " + clubeId));
        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado com o ID: " + jogadorId));

        if (jogador.getExcluidoEm() != null) {
            throw new ResourceNotFoundException("Jogador não encontrado.");
        }

        if (jogador.getClube() != null) {
            throw new BusinessException("O jogador já pertence a um clube e não está livre para compra.");
        }

        boolean jaVendeuAntes = transacaoRepository.findByClubeIdOrderByCriadoEmDescIdDesc(clubeId).stream()
                .anyMatch(t -> t.getJogador().getId().equals(jogadorId) && "VENDA".equals(t.getTipo()));
        if (jaVendeuAntes) {
            throw new BusinessException("Um clube não pode recomprar um jogador que ele mesmo já vendeu.");
        }

        List<Jogador> elencoAtual = jogadorRepository.findByClubeId(clubeId);
        if (elencoAtual.size() >= 12) {
            throw new BusinessException("O clube atingiu o limite máximo de 12 jogadores no elenco.");
        }

        LocalDateTime agora = LocalDateTime.now();
        if (fidelidadeAte.isBefore(agora) || fidelidadeAte.isAfter(agora.plusDays(1095))) {
            throw new BusinessException("A data de fidelidade deve ser futura e no máximo 1.095 dias após a compra.");
        }

        BigDecimal valorMercado = jogador.getValorMercado();
        BigDecimal taxa = BigDecimal.ZERO;
        
        if (valorMercado.compareTo(new BigDecimal("30.00")) > 0) {
            taxa = valorMercado.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal custoTotal = valorMercado.add(taxa);
        
        BigDecimal limiteCaixa = clube.getCaixa().multiply(new BigDecimal("0.50"));
        if (custoTotal.compareTo(limiteCaixa) > 0) {
            throw new BusinessException("O custo total da compra passa do limite de 50% do caixa do clube.");
        }

        clube.setCaixa(clube.getCaixa().subtract(custoTotal).setScale(2, RoundingMode.HALF_UP));
        clubeRepository.save(clube);

        jogador.setClube(clube);
        jogadorRepository.save(jogador);

        Contrato contrato = new Contrato();
        contrato.setJogador(jogador);
        contrato.setClube(clube);
        contrato.setValorCompra(valorMercado);
        contrato.setFidelidadeAte(fidelidadeAte);
        contrato.setSituacao("ATIVO");
        contratoRepository.save(contrato);

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
        transacaoRepository.save(transacao);

        return transacao;
    }

    @Transactional
    public Transacao venderJogador(Integer jogadorId) {
        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado com o ID: " + jogadorId));

        if (jogador.getExcluidoEm() != null || jogador.getClube() == null) {
            throw new BusinessException("O jogador não pertence a nenhum clube para poder ser vendido.");
        }

        Clube clube = jogador.getClube();

        Contrato contrato = contratoRepository.findByJogadorIdAndSituacao(jogadorId, "ATIVO")
                .orElseThrow(() -> new ResourceNotFoundException("Contrato ativo não encontrado para este jogador."));

        LocalDateTime agora = LocalDateTime.now();
        BigDecimal valorVenda = jogador.getValorMercado();

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

        BigDecimal multa = valorVenda.multiply(new BigDecimal(percentualMulta))
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        if (multa.compareTo(new BigDecimal("10.00")) > 0) {
            multa = new BigDecimal("10.00");
        }

        String novaSituacaoContrato = (percentualMulta > 0) ? "RESCINDIDO" : "ENCERRADO";

        BigDecimal valorCompra = contrato.getValorCompra();
        BigDecimal lucro = valorVenda.subtract(valorCompra);

        BigDecimal comissao = BigDecimal.ZERO;
        if (lucro.compareTo(BigDecimal.ZERO) > 0) {
            if (lucro.compareTo(new BigDecimal("10.00")) <= 0) {
                comissao = lucro.multiply(new BigDecimal("0.10"));
            } else {
                BigDecimal parte1 = new BigDecimal("10.00").multiply(new BigDecimal("0.10"));
                BigDecimal parte2 = lucro.subtract(new BigDecimal("10.00")).multiply(new BigDecimal("0.20"));
                comissao = parte1.add(parte2);
            }
            comissao = comissao.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal creditoCaixa = valorVenda.subtract(multa).subtract(comissao).setScale(2, RoundingMode.HALF_UP);

        clube.setCaixa(clube.getCaixa().add(creditoCaixa).setScale(2, RoundingMode.HALF_UP));
        clubeRepository.save(clube);

        contrato.setSituacao(novaSituacaoContrato);
        contrato.setEncerradoEm(agora);
        contratoRepository.save(contrato);

        jogador.setClube(null);
        jogadorRepository.save(jogador);

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
        transacaoRepository.save(transacao);

        return transacao;
    }

    public List<Transacao> listarTransacoesClube(Integer clubeId) {
        if (!clubeRepository.existsById(clubeId)) {
            throw new ResourceNotFoundException("Clube não encontrado com o ID: " + clubeId);
        }
        return transacaoRepository.findByClubeIdOrderByCriadoEmDescIdDesc(clubeId);
    }
}