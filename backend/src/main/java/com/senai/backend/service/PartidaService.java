package com.senai.backend.service;

import com.senai.backend.dto.request.LineupRequest;
import com.senai.backend.dto.request.MatchRequest;
import com.senai.backend.dto.response.*;
import com.senai.backend.exception.BusinessException;
import com.senai.backend.exception.ResourceNotFoundException;
import com.senai.backend.model.*;
import com.senai.backend.repository.*;
import com.senai.backend.util.DateTimeUtils;
import com.senai.backend.util.JogadorMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PartidaService {

    private final ClubeRepository clubeRepository;
    private final JogadorRepository jogadorRepository;
    private final EscalacaoRepository escalacaoRepository;
    private final PartidaRepository partidaRepository;
    private final DueloPartidaRepository dueloPartidaRepository;
    private final ContratoRepository contratoRepository;

    private static final Map<String, Integer> POSICAO_ORDEM = Map.of(
            "GOL", 1,
            "ZAG", 2,
            "LAT", 3,
            "MEI", 4,
            "ATA", 5
    );

    // Setores por formação para os 4 jogadores de linha: [Defesa (ZAG/LAT), Meio (MEI), Ataque (ATA)]
    private static final Map<String, int[]> FORMACAO_REQUISITOS = Map.of(
            "3-1", new int[]{3, 0, 1},
            "2-2", new int[]{2, 0, 2},
            "2-1-1", new int[]{2, 1, 1},
            "1-2-1", new int[]{1, 2, 1}
    );

    public PartidaService(ClubeRepository clubeRepository,
                          JogadorRepository jogadorRepository,
                          EscalacaoRepository escalacaoRepository,
                          PartidaRepository partidaRepository,
                          DueloPartidaRepository dueloPartidaRepository,
                          ContratoRepository contratoRepository) {
        this.clubeRepository = clubeRepository;
        this.jogadorRepository = jogadorRepository;
        this.escalacaoRepository = escalacaoRepository;
        this.partidaRepository = partidaRepository;
        this.dueloPartidaRepository = dueloPartidaRepository;
        this.contratoRepository = contratoRepository;
    }

    @Transactional
    public LineupResponse definirEscalacao(Integer clubeId, LineupRequest request) {
        request.validate();

        Clube clube = clubeRepository.findById(clubeId)
                .orElseThrow(() -> new ResourceNotFoundException("Clube não encontrado com o ID: " + clubeId));

        String formation = request.getFormation();
        List<Integer> playerIds = request.getPlayerIds();

        // Busca jogadores e valida se todos existem
        List<Jogador> jogadores = new ArrayList<>();
        for (Integer pId : playerIds) {
            Jogador j = jogadorRepository.findByIdAndExcluidoEmIsNull(pId)
                    .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado com o ID: " + pId));
            jogadores.add(j);
        }

        // RN19: Cada titular precisa ser do elenco do clube
        for (Jogador j : jogadores) {
            if (j.getClube() == null || !j.getClube().getId().equals(clubeId)) {
                throw new BusinessException("O jogador " + j.getNome() + " (ID: " + j.getId() + ") não pertence ao elenco do clube.");
            }
        }

        // RN19: Nenhum titular pode estar DOENTE
        for (Jogador j : jogadores) {
            if ("DOENTE".equalsIgnoreCase(j.getCondicao())) {
                throw new BusinessException("Jogador na condição DOENTE não pode ser escalado: " + j.getNome());
            }
        }

        // RN19: Validação das posições da formação
        int countGol = 0;
        int countDef = 0; // ZAG ou LAT
        int countMei = 0; // MEI
        int countAta = 0; // ATA

        for (Jogador j : jogadores) {
            String pos = j.getPosicao().toUpperCase();
            switch (pos) {
                case "GOL": countGol++; break;
                case "ZAG":
                case "LAT": countDef++; break;
                case "MEI": countMei++; break;
                case "ATA": countAta++; break;
                default: break;
            }
        }

        int[] req = FORMACAO_REQUISITOS.get(formation);
        if (countGol != 1 || countDef != req[0] || countMei != req[1] || countAta != req[2]) {
            throw new BusinessException("A composição dos jogadores não atende aos requisitos da formação " + formation + ".");
        }

        // Atualiza formação no clube
        clube.setFormacao(formation);
        clubeRepository.save(clube);

        // Substitui a escalação inteira do clube
        escalacaoRepository.deleteByClubeId(clubeId);
        for (Integer pId : playerIds) {
            escalacaoRepository.save(new Escalacao(clubeId, pId));
        }

        // Ordena jogadores na ordem do elenco para a resposta
        ordenarJogadoresOrdemElenco(jogadores);

        List<JogadorResponse> playerResponses = jogadores.stream()
                .map(j -> JogadorMapper.toResponse(j, buscarContratoAtivoSeHouver(j)))
                .collect(Collectors.toList());

        return new LineupResponse(clubeId, formation, true, playerResponses);
    }

    @Transactional(readOnly = true)
    public LineupResponse buscarEscalacao(Integer clubeId) {
        Clube clube = clubeRepository.findById(clubeId)
                .orElseThrow(() -> new ResourceNotFoundException("Clube não encontrado com o ID: " + clubeId));

        List<Escalacao> escalacoes = escalacaoRepository.findByClubeId(clubeId);
        String formation = clube.getFormacao();

        if (escalacoes.isEmpty() && formation == null) {
            return new LineupResponse(clubeId, null, false, Collections.emptyList());
        }

        List<Jogador> titulares = new ArrayList<>();
        for (Escalacao e : escalacoes) {
            jogadorRepository.findByIdAndExcluidoEmIsNull(e.getJogadorId()).ifPresent(j -> {
                // Se o jogador ainda pertence ao clube e não está DOENTE
                if (j.getClube() != null && j.getClube().getId().equals(clubeId) && !"DOENTE".equalsIgnoreCase(j.getCondicao())) {
                    titulares.add(j);
                }
            });
        }

        ordenarJogadoresOrdemElenco(titulares);
        boolean complete = (titulares.size() == 5 && formation != null);

        List<JogadorResponse> playerResponses = titulares.stream()
                .map(j -> JogadorMapper.toResponse(j, buscarContratoAtivoSeHouver(j)))
                .collect(Collectors.toList());

        return new LineupResponse(clubeId, formation, complete, playerResponses);
    }

    @Transactional
    public MatchResponse jogarPartida(MatchRequest request) {
        request.validate();

        Clube mandante = clubeRepository.findById(request.getHomeClubId())
                .orElseThrow(() -> new ResourceNotFoundException("Clube mandante não encontrado com o ID: " + request.getHomeClubId()));

        Clube visitante = clubeRepository.findById(request.getAwayClubId())
                .orElseThrow(() -> new ResourceNotFoundException("Clube visitante não encontrado com o ID: " + request.getAwayClubId()));

        // RN20: Conferir escalação completa de ambos os clubes
        List<Jogador> titularesMandante = obterTitularesValidos(mandante.getId());
        List<Jogador> titularesVisitante = obterTitularesValidos(visitante.getId());

        if (titularesMandante.size() != 5 || mandante.getFormacao() == null) {
            throw new BusinessException("O clube mandante (" + mandante.getNome() + ") não possui uma escalação completa de 5 titulares.");
        }

        if (titularesVisitante.size() != 5 || visitante.getFormacao() == null) {
            throw new BusinessException("O clube visitante (" + visitante.getNome() + ") não possui uma escalação completa de 5 titulares.");
        }

        ordenarJogadoresOrdemElenco(titularesMandante);
        ordenarJogadoresOrdemElenco(titularesVisitante);

        // Goleiro e os 4 jogadores de linha (index 0: GOL, index 1: 1º linha, index 2: 2º linha, index 3: 3º linha, index 4: 4º linha)
        Jogador golMandante = titularesMandante.get(0);
        Jogador linha1Mandante = titularesMandante.get(1);
        Jogador linha2Mandante = titularesMandante.get(2);
        Jogador linha3Mandante = titularesMandante.get(3);
        Jogador linha4Mandante = titularesMandante.get(4);

        Jogador golVisitante = titularesVisitante.get(0);
        Jogador linha1Visitante = titularesVisitante.get(1);
        Jogador linha2Visitante = titularesVisitante.get(2);
        Jogador linha3Visitante = titularesVisitante.get(3);
        Jogador linha4Visitante = titularesVisitante.get(4);

        // Cria registro da partida
        Partida partida = new Partida();
        partida.setMandante(mandante);
        partida.setVisitante(visitante);
        partida.setFormacaoMandante(mandante.getFormacao());
        partida.setFormacaoVisitante(visitante.getFormacao());
        partida.setJogadaEm(LocalDateTime.now());

        List<DueloPartida> duelosGravados = new ArrayList<>();
        int golsMandante = 0;
        int golsVisitante = 0;

        // Duelo 1: Mandante ataca - 4º linha Mandante vs Goleiro Visitante
        DueloPartida d1 = criarDuelo(partida, 1, mandante, linha4Mandante, golVisitante);
        if (d1.getGol()) golsMandante++;
        duelosGravados.add(d1);

        // Duelo 2: Mandante ataca - 3º linha Mandante vs 1º linha Visitante
        DueloPartida d2 = criarDuelo(partida, 2, mandante, linha3Mandante, linha1Visitante);
        if (d2.getGol()) golsMandante++;
        duelosGravados.add(d2);

        // Duelo 3: Mandante ataca - 2º linha Mandante vs 2º linha Visitante
        DueloPartida d3 = criarDuelo(partida, 3, mandante, linha2Mandante, linha2Visitante);
        if (d3.getGol()) golsMandante++;
        duelosGravados.add(d3);

        // Duelo 4: Visitante ataca - 4º linha Visitante vs Goleiro Mandante
        DueloPartida d4 = criarDuelo(partida, 4, visitante, linha4Visitante, golMandante);
        if (d4.getGol()) golsVisitante++;
        duelosGravados.add(d4);

        // Duelo 5: Visitante ataca - 3º linha Visitante vs 1º linha Mandante
        DueloPartida d5 = criarDuelo(partida, 5, visitante, linha3Visitante, linha1Mandante);
        if (d5.getGol()) golsVisitante++;
        duelosGravados.add(d5);

        // Duelo 6: Visitante ataca - 2º linha Visitante vs 2º linha Mandante
        DueloPartida d6 = criarDuelo(partida, 6, visitante, linha2Visitante, linha2Mandante);
        if (d6.getGol()) golsVisitante++;
        duelosGravados.add(d6);

        partida.setGolsMandante(golsMandante);
        partida.setGolsVisitante(golsVisitante);
        partida = partidaRepository.save(partida);

        for (DueloPartida d : duelosGravados) {
            d.setPartida(partida);
            dueloPartidaRepository.save(d);
        }

        return montarMatchResponse(partida, duelosGravados);
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> listarPartidas(Integer clubId) {
        if (clubId != null && !clubeRepository.existsById(clubId)) {
            throw new ResourceNotFoundException("Clube não encontrado com o ID: " + clubId);
        }

        List<Partida> partidas = (clubId != null)
                ? partidaRepository.findByMandanteIdOrVisitanteIdOrderByJogadaEmDescIdDesc(clubId, clubId)
                : partidaRepository.findAllByOrderByJogadaEmDescIdDesc();

        return partidas.stream()
                .map(p -> montarMatchResponse(p, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MatchResponse buscarPartidaPorId(Integer id) {
        Partida partida = partidaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partida não encontrada com o ID: " + id));

        List<DueloPartida> duelos = dueloPartidaRepository.findByPartidaIdOrderByOrdemAsc(id);
        return montarMatchResponse(partida, duelos);
    }

    @Transactional(readOnly = true)
    public List<StandingResponse> obterClassificacao() {
        List<Clube> todosClubes = clubeRepository.findAll();
        List<Partida> todasPartidas = partidaRepository.findAll();

        Map<Integer, ClubStats> statsMap = new HashMap<>();
        for (Clube c : todosClubes) {
            statsMap.put(c.getId(), new ClubStats(c.getId(), c.getNome()));
        }

        for (Partida p : todasPartidas) {
            ClubStats home = statsMap.get(p.getMandante().getId());
            ClubStats away = statsMap.get(p.getVisitante().getId());

            if (home != null && away != null) {
                home.played++;
                away.played++;

                home.goalsFor += p.getGolsMandante();
                home.goalsAgainst += p.getGolsVisitante();

                away.goalsFor += p.getGolsVisitante();
                away.goalsAgainst += p.getGolsMandante();

                if (p.getGolsMandante() > p.getGolsVisitante()) {
                    home.wins++;
                    home.points += 3;
                    away.losses++;
                } else if (p.getGolsMandante() < p.getGolsVisitante()) {
                    away.wins++;
                    away.points += 3;
                    home.losses++;
                } else {
                    home.draws++;
                    home.points += 1;
                    away.draws++;
                    away.points += 1;
                }
            }
        }

        Collator collator = Collator.getInstance(new Locale("pt", "BR"));
        collator.setStrength(Collator.PRIMARY);

        List<ClubStats> listaStats = new ArrayList<>(statsMap.values());
        listaStats.sort((s1, s2) -> {
            // 1. Mais pontos
            if (s1.points != s2.points) {
                return Integer.compare(s2.points, s1.points);
            }
            // 2. Maior saldo de gols
            int sg1 = s1.goalsFor - s1.goalsAgainst;
            int sg2 = s2.goalsFor - s2.goalsAgainst;
            if (sg1 != sg2) {
                return Integer.compare(sg2, sg1);
            }
            // 3. Mais gols marcados
            if (s1.goalsFor != s2.goalsFor) {
                return Integer.compare(s2.goalsFor, s1.goalsFor);
            }
            // 4. Nome em ordem alfabética (Unicode pt-BR)
            return collator.compare(s1.clubName, s2.clubName);
        });

        List<StandingResponse> response = new ArrayList<>();
        for (int i = 0; i < listaStats.size(); i++) {
            ClubStats s = listaStats.get(i);
            int goalDiff = s.goalsFor - s.goalsAgainst;
            response.add(new StandingResponse(
                    i + 1,
                    s.clubId,
                    s.clubName,
                    s.played,
                    s.wins,
                    s.draws,
                    s.losses,
                    s.goalsFor,
                    s.goalsAgainst,
                    goalDiff,
                    s.points
            ));
        }

        return response;
    }

    private DueloPartida criarDuelo(Partida partida, int ordem, Clube clubeAtacante, Jogador atacante, Jogador defensor) {
        DueloPartida duelo = new DueloPartida();
        duelo.setPartida(partida);
        duelo.setOrdem(ordem);
        duelo.setClubeAtacante(clubeAtacante);
        duelo.setAtacante(atacante);
        duelo.setDefensor(defensor);
        duelo.setNomeAtacante(atacante.getNome());
        duelo.setNomeDefensor(defensor.getNome());
        duelo.setPosicaoAtacante(atacante.getPosicao());
        duelo.setPosicaoDefensor(defensor.getPosicao());
        duelo.setCondicaoAtacante(atacante.getCondicao());
        duelo.setCondicaoDefensor(defensor.getCondicao());

        // Atributo do atacante pela posição
        String attrAtacante = switch (atacante.getPosicao().toUpperCase()) {
            case "ATA" -> "shooting";
            case "MEI" -> "passing";
            default -> "dribbling"; // ZAG e LAT
        };

        int valorBaseAtaque = switch (atacante.getPosicao().toUpperCase()) {
            case "ATA" -> atacante.getFinalizacao();
            case "MEI" -> atacante.getPasse();
            default -> atacante.getDrible();
        };

        // Atributo do defensor é sempre defesa
        String attrDefensor = "defending";
        int valorBaseDefesa = defensor.getDefesa();

        // Aplica efeito de MAL_CONDICIONADO: 15% a menos (arredondado para o mais próximo, meio arredondado para cima)
        int valorAtaqueFinal = aplicarEfeitoCondicao(valorBaseAtaque, atacante.getCondicao());
        int valorDefesaFinal = aplicarEfeitoCondicao(valorBaseDefesa, defensor.getCondicao());

        duelo.setAtributoAtaque(attrAtacante);
        duelo.setAtributoDefesa(attrDefensor);
        duelo.setValorAtaque(valorAtaqueFinal);
        duelo.setValorDefesa(valorDefesaFinal);

        // Gol quando ataque > defesa
        duelo.setGol(valorAtaqueFinal > valorDefesaFinal);

        return duelo;
    }

    private int aplicarEfeitoCondicao(int valor, String condicao) {
        if ("MAL_CONDICIONADO".equalsIgnoreCase(condicao)) {
            // valor * 0.85 arredondado para inteiro mais próximo com meio para cima
            return (int) Math.round(valor * 0.85);
        }
        return valor;
    }

    private List<Jogador> obterTitularesValidos(Integer clubeId) {
        List<Escalacao> escalacoes = escalacaoRepository.findByClubeId(clubeId);
        List<Jogador> titulares = new ArrayList<>();
        for (Escalacao e : escalacoes) {
            jogadorRepository.findByIdAndExcluidoEmIsNull(e.getJogadorId()).ifPresent(j -> {
                if (j.getClube() != null && j.getClube().getId().equals(clubeId) && !"DOENTE".equalsIgnoreCase(j.getCondicao())) {
                    titulares.add(j);
                }
            });
        }
        return titulares;
    }

    private void ordenarJogadoresOrdemElenco(List<Jogador> jogadores) {
        Collator collator = Collator.getInstance(new Locale("pt", "BR"));
        collator.setStrength(Collator.PRIMARY);

        jogadores.sort((j1, j2) -> {
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
    }

    private MatchResponse montarMatchResponse(Partida partida, List<DueloPartida> duelos) {
        String result;
        if (partida.getGolsMandante() > partida.getGolsVisitante()) {
            result = "MANDANTE";
        } else if (partida.getGolsMandante() < partida.getGolsVisitante()) {
            result = "VISITANTE";
        } else {
            result = "EMPATE";
        }

        MatchClubResponse home = new MatchClubResponse(
                partida.getMandante().getId(),
                partida.getMandante().getNome(),
                partida.getFormacaoMandante(),
                partida.getGolsMandante()
        );

        MatchClubResponse away = new MatchClubResponse(
                partida.getVisitante().getId(),
                partida.getVisitante().getNome(),
                partida.getFormacaoVisitante(),
                partida.getGolsVisitante()
        );

        List<MatchDuelResponse> duelResponses = null;
        if (duelos != null) {
            duelResponses = duelos.stream().map(d -> {
                MatchDuelPlayerResponse attacker = new MatchDuelPlayerResponse(
                        d.getAtacante().getId(),
                        d.getNomeAtacante(),
                        d.getPosicaoAtacante(),
                        d.getCondicaoAtacante(),
                        d.getAtributoAtaque(),
                        d.getValorAtaque()
                );

                MatchDuelPlayerResponse defender = new MatchDuelPlayerResponse(
                        d.getDefensor().getId(),
                        d.getNomeDefensor(),
                        d.getPosicaoDefensor(),
                        d.getCondicaoDefensor(),
                        d.getAtributoDefesa(),
                        d.getValorDefesa()
                );

                return new MatchDuelResponse(
                        d.getOrdem(),
                        d.getClubeAtacante().getId(),
                        attacker,
                        defender,
                        d.getGol()
                );
            }).collect(Collectors.toList());
        }

        return new MatchResponse(
                partida.getId(),
                DateTimeUtils.formatResponse(partida.getJogadaEm()),
                home,
                away,
                result,
                duelResponses
        );
    }

    private Contrato buscarContratoAtivoSeHouver(Jogador jogador) {
        if (jogador.getClube() == null) {
            return null;
        }
        return contratoRepository.findByJogadorIdAndSituacao(jogador.getId(), "ATIVO").orElse(null);
    }

    private static class ClubStats {
        int clubId;
        String clubName;
        int played = 0;
        int wins = 0;
        int draws = 0;
        int losses = 0;
        int goalsFor = 0;
        int goalsAgainst = 0;
        int points = 0;

        ClubStats(int clubId, String clubName) {
            this.clubId = clubId;
            this.clubName = clubName;
        }
    }
}
