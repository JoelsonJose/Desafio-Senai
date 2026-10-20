import { Fragment } from 'react';
import { AlertTriangle, ArrowLeft, CheckCircle2, Goal, Swords } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';
import Button from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import useAsyncData from '../../hooks/useAsyncData';
import { getMatch } from '../../services/matchesService';
import { formatDateTime } from '../../utils/format';

const ATTRIBUTE_LABEL = { pace: 'Velocidade', shooting: 'Finalização', passing: 'Passe', dribbling: 'Drible', defending: 'Defesa', physical: 'Físico' };
const POSITION_ORDER = { GOL: 0, ZAG: 1, LAT: 2, MEI: 3, ATA: 4 };
const POSITION_LABEL = { GOL: 'Goleiro', ZAG: 'Zagueiro', LAT: 'Lateral', MEI: 'Meio-campo', ATA: 'Atacante' };

const FORMATION_POINTS = {
  '3-1': [[20, 30], [24, 50], [20, 70], [43, 50]],
  '2-2': [[20, 35], [20, 65], [43, 35], [43, 65]],
  '2-1-1': [[20, 35], [20, 65], [34, 35], [44, 65]],
  '1-2-1': [[20, 50], [35, 35], [35, 65], [44, 50]],
};

function playerInitials(name = '') {
  return name.split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0]).join('').toUpperCase();
}

function outcomeLabel(result) {
  if (result === 'EMPATE') return 'Empate';
  if (result === 'MANDANTE') return 'Vitória do mandante';
  return 'Vitória do visitante';
}

function collectTeamPlayers(match, club) {
  const players = new Map();
  for (const duel of match.duels ?? []) {
    const isAttacking = duel.attacking_club_id === club.club_id;
    const participant = isAttacking ? duel.attacker : duel.defender;
    if (!participant) continue;
    const current = players.get(participant.player_id) ?? { ...participant, attributes: {} };
    current.name = participant.player_name;
    current.position = participant.position;
    current.condition = participant.condition;
    current.attributes[participant.attribute] = participant.value;
    players.set(participant.player_id, current);
  }
  const roster = [...players.values()].sort((a, b) => (POSITION_ORDER[a.position] ?? 99) - (POSITION_ORDER[b.position] ?? 99) || a.name.localeCompare(b.name, 'pt-BR') || a.player_id - b.player_id);
  return roster;
}

function PlayerMarker({ player, left, top, team, goalScorers }) {
  if (!player) return null;
  const isScorer = goalScorers.has(player.player_id);
  const attributeValues = Object.entries(player.attributes).map(([key, value]) => `${ATTRIBUTE_LABEL[key] ?? key} ${value}`).join(' · ');
  return <div className="match-player" style={{ left: `${left}%`, top: `${top}%` }}>
    <span className={`match-player-avatar ${team === 'away' ? 'match-player-avatar-away' : ''} ${player.condition === 'MAL_CONDICIONADO' ? 'match-player-avatar-warn' : ''}`}>{playerInitials(player.name)}</span>
    {player.condition === 'MAL_CONDICIONADO' && <span className="match-player-condition" title="Mal condicionado"><AlertTriangle size={12} /></span>}
    {isScorer && <span className="match-player-goal" title="Fez gol neste duelo"><Goal size={12} /></span>}
    <span className="match-player-name">{player.name}</span>
    <span className="match-player-detail">{player.position} · {attributeValues || POSITION_LABEL[player.position]}</span>
  </div>;
}

function MatchPitch({ match }) {
  const homeRoster = collectTeamPlayers(match, match.home);
  const awayRoster = collectTeamPlayers(match, match.away);
  const goalScorers = new Set((match.duels ?? []).filter((duel) => duel.goal).map((duel) => duel.attacker.player_id));
  const homePoints = FORMATION_POINTS[match.home.formation] ?? FORMATION_POINTS['2-1-1'];
  const awayPoints = FORMATION_POINTS[match.away.formation] ?? FORMATION_POINTS['2-1-1'];
  return <div className="match-pitch" aria-label="Campo da partida com os titulares e gols">
    <svg className="match-pitch-lines" viewBox="0 0 1200 460" preserveAspectRatio="none" aria-hidden="true">
      <rect x="14" y="14" width="1172" height="432" rx="10" />
      <path d="M600 14v432M14 112h105v236H14M1186 112h-105v236h105" />
      <path d="M14 168h39v124H14M1186 168h-39v124h39" />
      <circle cx="600" cy="230" r="76" /><circle cx="600" cy="230" r="3" className="match-pitch-dot" />
      <circle cx="87" cy="230" r="3" className="match-pitch-dot" /><circle cx="1113" cy="230" r="3" className="match-pitch-dot" />
      <path d="M119 112a112 112 0 0 1 0 236M1081 112a112 112 0 0 0 0 236" />
      <path d="M0 192h14v76H0M1200 192h-14v76h14" />
    </svg>
    <div className="match-team-tag match-team-home"><span />{match.home.club_name} · {match.home.formation} · ataca <span aria-hidden="true">→</span></div>
    <div className="match-team-tag match-team-away"><span />{match.away.club_name} · {match.away.formation} · ataca <span aria-hidden="true">←</span></div>
    {homeRoster.map((player, index) => {
      const [left, top] = index === 0 ? [6, 50] : [homePoints[index - 1]?.[0] ?? 45, homePoints[index - 1]?.[1] ?? 50];
      return <PlayerMarker key={`home-${player.player_id}`} player={player} left={left} top={top} team="home" goalScorers={goalScorers} />;
    })}
    {awayRoster.map((player, index) => {
      const [localLeft, top] = index === 0 ? [6, 50] : [awayPoints[index - 1]?.[0] ?? 45, awayPoints[index - 1]?.[1] ?? 50];
      return <PlayerMarker key={`away-${player.player_id}`} player={player} left={100 - localLeft} top={index === 0 ? top : 100 - top} team="away" goalScorers={goalScorers} />;
    })}
  </div>;
}

function DuelTable({ match }) {
  const duels = match.duels ?? [];
  const clubs = [match.home, match.away];
  return <section className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
    <div className="border-b border-slate-200 px-5 py-4"><h2 className="font-semibold text-slate-900">Duelos</h2><p className="mt-1 text-sm text-slate-500">Os três ataques do mandante e depois os três do visitante.</p></div>
    <div className="overflow-x-auto">
      <table className="w-full min-w-[850px] border-collapse text-sm">
        <thead><tr className="border-b border-slate-200 bg-slate-50 text-left text-xs font-medium text-slate-500"><th className="px-4 py-3">Duelo</th><th className="px-4 py-3">Atacante</th><th className="px-4 py-3">Ataque</th><th className="px-4 py-3">Defensor</th><th className="px-4 py-3">Defesa</th><th className="px-4 py-3 text-right">Resultado</th></tr></thead>
        <tbody>{clubs.map((club) => {
          const rows = duels.filter((duel) => duel.attacking_club_id === club.club_id);
          const goals = rows.filter((duel) => duel.goal).length;
          return <Fragment key={club.club_id}>
            <tr key={`group-${club.club_id}`} className="border-b border-slate-200 bg-slate-50"><td colSpan="6" className="px-4 py-2"><div className="flex items-center justify-between gap-3 text-xs"><span className="font-semibold text-slate-700"><b className="mr-2 rounded bg-white px-2 py-1 text-slate-600">{club.club_name.slice(0, 3).toUpperCase()}</b>Ataques do {club.club_name} ({club.club_id === match.home.club_id ? 'mandante' : 'visitante'})</span><span className="font-medium text-slate-600">{goals} {goals === 1 ? 'gol' : 'gols'}</span></div></td></tr>
            {rows.map((duel) => <tr key={duel.order} className="border-b border-slate-100 last:border-0 hover:bg-slate-50/60">
              <td className="px-4 py-3"><span className="rounded-md bg-slate-100 px-2 py-1 text-xs font-semibold text-slate-600">{duel.order}</span></td>
              <td className="px-4 py-3"><strong className="block font-medium text-slate-900">{duel.attacker.player_name}</strong><span className="text-xs text-slate-500">{POSITION_LABEL[duel.attacker.position] ?? duel.attacker.position} · {club.club_name}</span>{duel.attacker.condition === 'MAL_CONDICIONADO' && <span className="mt-1 inline-flex items-center gap-1 text-xs text-amber-700"><AlertTriangle size={12} /> Mal condicionado</span>}</td>
              <td className="px-4 py-3"><span className="text-slate-500">{ATTRIBUTE_LABEL[duel.attacker.attribute] ?? duel.attacker.attribute}</span> <strong className="text-slate-800">{duel.attacker.value}</strong></td>
              <td className="px-4 py-3"><strong className="block font-medium text-slate-900">{duel.defender.player_name}</strong><span className="text-xs text-slate-500">{POSITION_LABEL[duel.defender.position] ?? duel.defender.position}</span>{duel.defender.condition === 'MAL_CONDICIONADO' && <span className="mt-1 inline-flex items-center gap-1 text-xs text-amber-700"><AlertTriangle size={12} /> Mal condicionado</span>}</td>
              <td className="px-4 py-3"><span className="text-slate-500">Defesa</span> <strong className="text-slate-800">{duel.defender.value}</strong></td>
              <td className="px-4 py-3 text-right">{duel.goal ? <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700"><Goal size={13} /> Gol</span> : <span className="text-slate-500">Sem gol</span>}</td>
            </tr>)}
          </Fragment>;
        })}</tbody>
      </table>
    </div>
  </section>;
}

export default function MatchDetailPage() {
  const { matchId } = useParams();
  const match = useAsyncData(() => getMatch(matchId), [matchId]);
  const data = match.data;
  const resultText = data ? outcomeLabel(data.result) : '';

  return <>
    <PageHeader icon={Swords} title="Detalhes da partida" description="Placar e duelos registrados pela liga." action={<Link to="/matches"><Button variant="secondary" icon={ArrowLeft}>Voltar às partidas</Button></Link>} />
    {match.error && <div role="alert" className="rounded-xl border border-red-200 bg-white p-5 text-sm text-red-700">{match.error} <button onClick={match.reload} className="ml-2 font-semibold underline">Tentar novamente</button></div>}
    {match.loading && !data && <div className="rounded-xl border border-slate-200 bg-white p-8 text-center text-sm text-slate-500">Carregando partida…</div>}
    {data && <>
      <div role="status" className="mb-4 flex items-start gap-3 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-900 shadow-sm"><CheckCircle2 className="mt-0.5 h-5 w-5 shrink-0 text-emerald-600" /><span><strong className="block">Partida jogada</strong>{data.home.club_name} × {data.away.club_name}. Placar e duelos devolvidos pela API.</span></div>
      <section className="mb-5 rounded-xl border border-slate-200 bg-white px-5 py-4 text-center shadow-sm">
        <p className="text-sm text-slate-500">{formatDateTime(data.played_at)}</p>
        <div className="mt-3 grid grid-cols-[1fr_auto_1fr] items-center gap-3 sm:gap-6">
          <div className="min-w-0 text-right"><p className="truncate font-semibold text-slate-900">{data.home.club_name}</p><p className="mt-1 text-xs text-slate-500">Mandante · {data.home.formation}</p></div>
          <div><div className="rounded-xl bg-slate-100 px-5 py-2 text-3xl font-bold tabular-nums text-slate-950">{data.home.goals} <span className="text-slate-400">×</span> {data.away.goals}</div><span className="mt-2 inline-block rounded-full border border-slate-200 px-3 py-1 text-xs font-medium text-slate-600">{resultText}</span></div>
          <div className="min-w-0 text-left"><p className="truncate font-semibold text-slate-900">{data.away.club_name}</p><p className="mt-1 text-xs text-slate-500">Visitante · {data.away.formation}</p></div>
        </div>
      </section>
      <section className="mb-5 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 px-5 py-4"><div><h2 className="font-semibold text-slate-900">Em campo</h2><p className="mt-1 text-sm text-slate-500">Titulares de cada clube e os valores usados nos duelos.</p></div><div className="flex flex-wrap items-center gap-4 text-xs text-slate-500"><span className="inline-flex items-center gap-1.5"><AlertTriangle size={14} className="text-amber-700" /> Mal condicionado</span><span className="inline-flex items-center gap-1.5"><Goal size={14} /> Gol no duelo</span></div></div>
        <div className="p-4"><MatchPitch match={data} /></div>
      </section>
      <DuelTable match={data} />
      <p className="mt-4 text-xs leading-5 text-slate-500">Os valores de ataque e defesa, as condições e os titulares vêm do registro da partida. Jogadores mal condicionados atuam com 15% a menos em cada atributo (RN20).</p>
    </>}
  </>;
}
