package com.senai.backend.service;

import com.senai.backend.dto.request.CondicaoRequest;
import com.senai.backend.dto.request.JogadorCreateRequest;
import com.senai.backend.dto.request.JogadorUpdateRequest;
import com.senai.backend.dto.response.JogadorResponse;
import com.senai.backend.dto.response.PageMetaResponse;
import com.senai.backend.dto.response.PaginatedResponse;
import com.senai.backend.exception.BusinessException;
import com.senai.backend.exception.ResourceNotFoundException;
import com.senai.backend.model.Contrato;
import com.senai.backend.model.Jogador;
import com.senai.backend.repository.ClubeRepository;
import com.senai.backend.repository.ContratoRepository;
import com.senai.backend.repository.EscalacaoRepository;
import com.senai.backend.repository.JogadorRepository;
import com.senai.backend.util.JogadorMapper;
import com.senai.backend.util.ValidationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Collator;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JogadorService {

    private final JogadorRepository jogadorRepository;
    private final ContratoRepository contratoRepository;
    private final EscalacaoRepository escalacaoRepository;
    private final ClubeRepository clubeRepository;

    private static final Map<String, Integer> POSICAO_ORDEM = Map.of(
            "GOL", 1,
            "ZAG", 2,
            "LAT", 3,
            "MEI", 4,
            "ATA", 5
    );

    public JogadorService(JogadorRepository jogadorRepository,
                          ContratoRepository contratoRepository,
                          EscalacaoRepository escalacaoRepository,
                          ClubeRepository clubeRepository) {
        this.jogadorRepository = jogadorRepository;
        this.contratoRepository = contratoRepository;
        this.escalacaoRepository = escalacaoRepository;
        this.clubeRepository = clubeRepository;
    }

    @Transactional
    public JogadorResponse criar(JogadorCreateRequest request) {
        request.validate();

        Jogador jogador = new Jogador();
        jogador.setNome(request.getName());
        jogador.setPosicao(request.getPosition());
        jogador.setAgilidade(request.getAgility());
        jogador.setIdade(request.getAge());
        jogador.setCondicao("APTO");

        jogador.setVelocidade(request.getPace());
        jogador.setFinalizacao(request.getShooting());
        jogador.setPasse(request.getPassing());
        jogador.setDrible(request.getDribbling());
        jogador.setDefesa(request.getDefending());
        jogador.setFisico(request.getPhysical());
        jogador.setPotencial(request.getPotential());

        Jogador salvo = jogadorRepository.save(jogador);
        return JogadorMapper.toResponse(salvo, null);
    }

    @Transactional(readOnly = true)
    public JogadorResponse buscarPorId(Integer id) {
        Jogador jogador = jogadorRepository.findByIdAndExcluidoEmIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado com o ID: " + id));

        Contrato contratoAtivo = buscarContratoAtivoSeHouver(jogador);
        return JogadorMapper.toResponse(jogador, contratoAtivo);
    }

    @Transactional(readOnly = true)
    public List<JogadorResponse> listarPorClube(Integer clubeId) {
        if (!clubeRepository.existsById(clubeId)) {
            throw new ResourceNotFoundException("Clube não encontrado com o ID: " + clubeId);
        }

        List<Jogador> jogadores = jogadorRepository.findByClubeIdAndExcluidoEmIsNull(clubeId);
        return jogadores.stream()
                .map(j -> JogadorMapper.toResponse(j, buscarContratoAtivoSeHouver(j)))
                .collect(Collectors.toList());
    }

    @Transactional
    public JogadorResponse atualizar(Integer id, JogadorUpdateRequest request) {
        request.validate();

        Jogador jogador = jogadorRepository.findByIdAndExcluidoEmIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado com o ID: " + id));

        // RN08: Se o jogador tem clube, não pode alterar de posição -> 409 Conflict
        if (jogador.getClube() != null && !jogador.getPosicao().equalsIgnoreCase(request.getPosition())) {
            throw new BusinessException("Jogador com clube vinculado não pode ter sua posição alterada.");
        }

        // RN08: Idade não pode diminuir -> 409 Conflict
        if (request.getAge() < jogador.getIdade()) {
            throw new BusinessException("A idade do jogador não pode diminuir.");
        }

        // Atualiza apenas os 4 campos permitidos por RN08
        jogador.setNome(request.getName());
        jogador.setPosicao(request.getPosition());
        jogador.setAgilidade(request.getAgility());
        jogador.setIdade(request.getAge());

        Jogador salvo = jogadorRepository.save(jogador);
        Contrato contratoAtivo = buscarContratoAtivoSeHouver(salvo);
        return JogadorMapper.toResponse(salvo, contratoAtivo);
    }

    @Transactional
    public JogadorResponse atualizarCondicao(Integer id, CondicaoRequest request) {
        request.validate();

        Jogador jogador = jogadorRepository.findByIdAndExcluidoEmIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado com o ID: " + id));

        String condicaoAtual = jogador.getCondicao();
        String novaCondicao = request.getCondition();

        // Se for a mesma condição, não faz nada e retorna 200
        if (condicaoAtual.equalsIgnoreCase(novaCondicao)) {
            Contrato contratoAtivo = buscarContratoAtivoSeHouver(jogador);
            return JogadorMapper.toResponse(jogador, contratoAtivo);
        }

        // RN19: Transição direta de DOENTE para APTO é proibida -> 409 Conflict
        if ("DOENTE".equalsIgnoreCase(condicaoAtual) && "APTO".equalsIgnoreCase(novaCondicao)) {
            throw new BusinessException("Transição direta de DOENTE para APTO não é permitida. O jogador deve passar por MAL_CONDICIONADO.");
        }

        jogador.setCondicao(novaCondicao);

        // Se a nova condição for DOENTE, remove automaticamente da escalação (titular não pode estar DOENTE)
        if ("DOENTE".equalsIgnoreCase(novaCondicao)) {
            escalacaoRepository.deleteByJogadorId(id);
        }

        Jogador salvo = jogadorRepository.save(jogador);
        Contrato contratoAtivo = buscarContratoAtivoSeHouver(salvo);
        return JogadorMapper.toResponse(salvo, contratoAtivo);
    }

    @Transactional
    public void deletar(Integer id) {
        Jogador jogador = jogadorRepository.findByIdAndExcluidoEmIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado com o ID: " + id));

        // RN09: Jogador com clube não pode ser excluído -> 409 Conflict
        if (jogador.getClube() != null) {
            throw new BusinessException("Jogador vinculado a um clube não pode ser excluído.");
        }

        jogador.setExcluidoEm(LocalDateTime.now());
        jogadorRepository.save(jogador);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<JogadorResponse> listarPaginado(
            String search,
            String position,
            String condition,
            Boolean free,
            Integer clubId,
            Integer minAge,
            Integer maxAge,
            BigDecimal minValue,
            BigDecimal maxValue,
            String sortParam,
            int page,
            int limit
    ) {
        // Validações de contradição de filtros (400 Bad Request)
        if (minAge != null && maxAge != null && minAge > maxAge) {
            throw new IllegalArgumentException("Filtro de idade inválido: min_age não pode ser maior que max_age.");
        }

        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
            throw new IllegalArgumentException("Filtro de valor de mercado inválido: min_value não pode ser maior que max_value.");
        }

        if (Boolean.TRUE.equals(free) && clubId != null) {
            throw new IllegalArgumentException("Filtros conflitantes: não é possível buscar jogadores sem clube (free=true) especificando um club_id.");
        }

        if (clubId != null && !clubeRepository.existsById(clubId)) {
            throw new ResourceNotFoundException("Clube não encontrado com o ID: " + clubId);
        }

        // Busca todos os jogadores ativos (não soft-deleted)
        List<Jogador> todosJogadores = jogadorRepository.findAllByExcluidoEmIsNull();

        // Aplica filtros em memória (garante suporte a acentos no search e valor de mercado calculado dinamicamente)
        List<Jogador> filtrados = todosJogadores.stream().filter(j -> {
            if (search != null && !search.trim().isEmpty()) {
                String searchNormalizado = ValidationUtils.stripAccents(search.trim()).toLowerCase();
                String nomeNormalizado = ValidationUtils.stripAccents(j.getNome()).toLowerCase();
                if (!nomeNormalizado.contains(searchNormalizado)) {
                    return false;
                }
            }

            if (position != null && !position.trim().isEmpty()) {
                if (!j.getPosicao().equalsIgnoreCase(position.trim())) {
                    return false;
                }
            }

            if (condition != null && !condition.trim().isEmpty()) {
                if (!j.getCondicao().equalsIgnoreCase(condition.trim())) {
                    return false;
                }
            }

            if (free != null) {
                if (free && j.getClube() != null) {
                    return false;
                }
                if (!free && j.getClube() == null) {
                    return false;
                }
            }

            if (clubId != null) {
                if (j.getClube() == null || !j.getClube().getId().equals(clubId)) {
                    return false;
                }
            }

            if (minAge != null && j.getIdade() < minAge) {
                return false;
            }

            if (maxAge != null && j.getIdade() > maxAge) {
                return false;
            }

            BigDecimal vm = j.getValorMercado();
            if (minValue != null && vm != null && vm.compareTo(minValue) < 0) {
                return false;
            }

            if (maxValue != null && vm != null && vm.compareTo(maxValue) > 0) {
                return false;
            }

            return true;
        }).collect(Collectors.toList());

        // Ordenação
        Comparator<Jogador> comparator = buildComparator(sortParam);
        filtrados.sort(comparator);

        // Paginação
        long totalItems = filtrados.size();
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / limit);

        List<Jogador> paginados;
        int startIndex = (page - 1) * limit;
        if (startIndex >= totalItems || startIndex < 0) {
            paginados = Collections.emptyList();
        } else {
            int toIndex = Math.min(startIndex + limit, (int) totalItems);
            paginados = filtrados.subList(startIndex, toIndex);
        }

        List<JogadorResponse> responses = paginados.stream()
                .map(j -> JogadorMapper.toResponse(j, buscarContratoAtivoSeHouver(j)))
                .collect(Collectors.toList());

        PageMetaResponse meta = new PageMetaResponse(page, limit, totalItems, totalPages);
        return new PaginatedResponse<>(responses, meta);
    }

    private Comparator<Jogador> buildComparator(String sortParam) {
        if (sortParam == null || sortParam.trim().isEmpty()) {
            return Comparator.comparing(Jogador::getId);
        }

        String[] tokens = sortParam.split(",");
        Comparator<Jogador> combined = null;

        Collator collator = Collator.getInstance(new Locale("pt", "BR"));
        collator.setStrength(Collator.PRIMARY); // ignora acentos e maiúsculas/minúsculas

        for (String token : tokens) {
            String trimmed = token.trim();
            Comparator<Jogador> currentComp = null;

            switch (trimmed) {
                case "name":
                    currentComp = (j1, j2) -> collator.compare(j1.getNome(), j2.getNome());
                    break;
                case "value_asc":
                    currentComp = Comparator.comparing(Jogador::getValorMercado, Comparator.nullsLast(BigDecimal::compareTo));
                    break;
                case "value_desc":
                    currentComp = Comparator.comparing(Jogador::getValorMercado, Comparator.nullsLast(Comparator.reverseOrder()));
                    break;
                case "age_asc":
                    currentComp = Comparator.comparing(Jogador::getIdade);
                    break;
                case "age_desc":
                    currentComp = Comparator.comparing(Jogador::getIdade).reversed();
                    break;
                case "position":
                    currentComp = Comparator.comparingInt(j -> POSICAO_ORDEM.getOrDefault(j.getPosicao(), 99));
                    break;
                default:
                    currentComp = Comparator.comparing(Jogador::getId);
                    break;
            }

            if (combined == null) {
                combined = currentComp;
            } else {
                combined = combined.thenComparing(currentComp);
            }
        }

        // Desempate padrão por ID
        return combined != null ? combined.thenComparing(Jogador::getId) : Comparator.comparing(Jogador::getId);
    }

    private Contrato buscarContratoAtivoSeHouver(Jogador jogador) {
        if (jogador.getClube() == null) {
            return null;
        }
        return contratoRepository.findByJogadorIdAndSituacao(jogador.getId(), "ATIVO").orElse(null);
    }
}