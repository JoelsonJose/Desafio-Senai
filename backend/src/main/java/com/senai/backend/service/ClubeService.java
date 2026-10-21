package com.senai.backend.service;

import com.senai.backend.dto.request.ClubeCreateRequest;
import com.senai.backend.dto.response.ClubeDetailResponse;
import com.senai.backend.dto.response.ClubeResponse;
import com.senai.backend.dto.response.JogadorResponse;
import com.senai.backend.exception.BusinessException;
import com.senai.backend.exception.ResourceNotFoundException;
import com.senai.backend.model.Clube;
import com.senai.backend.model.Contrato;
import com.senai.backend.model.Jogador;
import com.senai.backend.repository.ClubeRepository;
import com.senai.backend.repository.ContratoRepository;
import com.senai.backend.repository.EscalacaoRepository;
import com.senai.backend.repository.JogadorRepository;
import com.senai.backend.repository.PartidaRepository;
import com.senai.backend.repository.TransacaoRepository;
import com.senai.backend.util.ClubeMapper;
import com.senai.backend.util.JogadorMapper;
import com.senai.backend.util.ValidationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClubeService {

    private final ClubeRepository clubeRepository;
    private final JogadorRepository jogadorRepository;
    private final ContratoRepository contratoRepository;
    private final EscalacaoRepository escalacaoRepository;
    private final PartidaRepository partidaRepository;
    private final TransacaoRepository transacaoRepository;

    private static final Map<String, Integer> POSICAO_ORDEM = Map.of(
            "GOL", 1,
            "ZAG", 2,
            "LAT", 3,
            "MEI", 4,
            "ATA", 5
    );

    public ClubeService(ClubeRepository clubeRepository,
                        JogadorRepository jogadorRepository,
                        ContratoRepository contratoRepository,
                        EscalacaoRepository escalacaoRepository,
                        PartidaRepository partidaRepository,
                        TransacaoRepository transacaoRepository) {
        this.clubeRepository = clubeRepository;
        this.jogadorRepository = jogadorRepository;
        this.contratoRepository = contratoRepository;
        this.escalacaoRepository = escalacaoRepository;
        this.partidaRepository = partidaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<ClubeResponse> listarTodos(String sort) {
        List<Clube> clubes = clubeRepository.findAll();

        Collator collator = Collator.getInstance(new Locale("pt", "BR"));
        collator.setStrength(Collator.PRIMARY);

        if (sort != null && !List.of("name", "net_worth_asc", "net_worth_desc").contains(sort)) {
            throw new IllegalArgumentException("O parâmetro sort deve ser name, net_worth_asc ou net_worth_desc.");
        }

        if ("net_worth_asc".equals(sort) || "net_worth_desc".equals(sort)) {
            Comparator<Clube> byWorth = Comparator.comparing(this::calcularPatrimonio);
            if ("net_worth_desc".equals(sort)) {
                byWorth = byWorth.reversed();
            }
            clubes.sort(byWorth
                    .thenComparing(Clube::getNome, collator)
                    .thenComparing(Clube::getId));
        } else {
            // Padrão: alfabético por nome com desempate por id
            clubes.sort((c1, c2) -> {
                int cmp = collator.compare(c1.getNome(), c2.getNome());
                if (cmp == 0) {
                    return c1.getId().compareTo(c2.getId());
                }
                return cmp;
            });
        }

        return clubes.stream().map(c -> {
            BigDecimal squadValue = calcularValorElenco(c.getId());
            BigDecimal netWorth = c.getCaixa().add(squadValue);
            return ClubeMapper.toResponse(c, squadValue, netWorth);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Clube buscarEntidadePorId(Integer id) {
        return clubeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clube não encontrado com o ID: " + id));
    }

    @Transactional(readOnly = true)
    public ClubeDetailResponse buscarDetalhePorId(Integer id) {
        Clube clube = buscarEntidadePorId(id);
        BigDecimal squadValue = calcularValorElenco(id);
        BigDecimal netWorth = clube.getCaixa().add(squadValue);

        List<JogadorResponse> elenco = obterElencoOrdenado(id);
        return ClubeMapper.toDetailResponse(clube, squadValue, netWorth, elenco);
    }

    @Transactional
    public ClubeResponse criar(ClubeCreateRequest request) {
        request.validate();

        // RN10: Limite máximo de 20 clubes na liga
        long totalClubes = clubeRepository.count();
        if (totalClubes >= 20) {
            throw new BusinessException("O limite máximo de 20 clubes na liga já foi atingido.");
        }

        String nome = request.getName();
        String sigla = request.getAcronym();
        String nomeNormalizado = nome.replaceAll("\\s+", " ").toLowerCase();

        // RN10: Unicidade de sigla
        if (clubeRepository.existsBySigla(sigla)) {
            throw new BusinessException("Já existe um clube cadastrado com a sigla informada.");
        }

        // RN10: Unicidade de nome (normalizado)
        if (clubeRepository.existsByNomeNormalizado(nomeNormalizado)) {
            throw new BusinessException("Já existe um clube cadastrado com este nome.");
        }

        Clube clube = new Clube();
        clube.setNome(nome);
        clube.setNomeNormalizado(nomeNormalizado);
        clube.setSigla(sigla);
        clube.setCaixa(new BigDecimal("300.00")); // Caixa inicial obrigatório de R$ 300,00

        Clube salvo = clubeRepository.save(clube);
        return ClubeMapper.toResponse(salvo, BigDecimal.ZERO, salvo.getCaixa());
    }

    @Transactional
    public void deletar(Integer id) {
        Clube clube = buscarEntidadePorId(id);

        // RN12: Se tiver jogadores no elenco, não pode excluir -> 409 Conflict
        List<Jogador> jogadores = jogadorRepository.findByClubeIdAndExcluidoEmIsNull(id);
        if (!jogadores.isEmpty()) {
            throw new BusinessException("Clube não pode ser excluído pois possui jogadores vinculados ao seu elenco.");
        }

        // RN12: Se participou de partidas oficiais, não pode excluir -> 409 Conflict
        if (partidaRepository.existsByMandanteIdOrVisitanteId(id, id)) {
            throw new BusinessException("Clube não pode ser excluído pois possui histórico de partidas disputadas.");
        }

        // Limpa escalação se houver resíduo
        escalacaoRepository.deleteByClubeId(id);

        clubeRepository.delete(clube);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularValorElenco(Integer clubeId) {
        List<Jogador> elenco = jogadorRepository.findByClubeIdAndExcluidoEmIsNull(clubeId);
        BigDecimal soma = BigDecimal.ZERO;
        for (Jogador j : elenco) {
            BigDecimal vm = j.getValorMercado();
            if (vm != null) {
                soma = soma.add(vm);
            }
        }
        return soma;
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularPatrimonio(Clube clube) {
        BigDecimal valorElenco = calcularValorElenco(clube.getId());
        return clube.getCaixa().add(valorElenco);
    }

    @Transactional(readOnly = true)
    public List<JogadorResponse> obterElencoOrdenado(Integer clubeId) {
        List<Jogador> elenco = jogadorRepository.findByClubeIdAndExcluidoEmIsNull(clubeId);

        Collator collator = Collator.getInstance(new Locale("pt", "BR"));
        collator.setStrength(Collator.PRIMARY);

        // RN11: ordenados por posição (GOL, ZAG, LAT, MEI, ATA), depois por nome, depois por id
        elenco.sort((j1, j2) -> {
            int p1 = POSICAO_ORDEM.getOrDefault(j1.getPosicao(), 99);
            int p2 = POSICAO_ORDEM.getOrDefault(j2.getPosicao(), 99);
            if (p1 != p2) {
                return Integer.compare(p1, p2);
            }
            int cmpNome = collator.compare(j1.getNome(), j2.getNome());
            if (cmpNome != 0) {
                return cmpNome;
            }
            return Integer.compare(j1.getId(), j2.getId());
        });

        return elenco.stream().map(j -> {
            Contrato contratoAtivo = contratoRepository.findByJogadorIdAndSituacao(j.getId(), "ATIVO").orElse(null);
            return JogadorMapper.toResponse(j, contratoAtivo);
        }).collect(Collectors.toList());
    }
}
