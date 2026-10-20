import { ListOrdered, Swords, Trophy } from 'lucide-react';
import { Link } from 'react-router-dom';
import DataTable from '../../components/ui/DataTable';
import PageHeader from '../../components/ui/PageHeader';
import useAsyncData from '../../hooks/useAsyncData';
import { listMatches, getStandings } from '../../services/matchesService';
import { formatDateTime } from '../../utils/format';

export default function StandingsPage() {
  const standings = useAsyncData(() => getStandings(), []);
  const matches = useAsyncData(() => listMatches(), []);
  const columns = [
    { key: 'position', header: '#', render: (row) => <span className={`flex h-7 w-7 items-center justify-center rounded-lg text-xs font-semibold ${row.position === 1 ? 'bg-brand-50 text-brand-700' : 'bg-slate-100 text-slate-600'}`}>{row.position}</span> },
    { key: 'club', header: 'Clube', render: (row) => <span className="font-medium text-slate-900">{row.club_name}</span> },
    { key: 'played', header: 'J', align: 'center', render: (row) => row.played },
    { key: 'wins', header: 'V', align: 'center', render: (row) => row.wins },
    { key: 'draws', header: 'E', align: 'center', render: (row) => row.draws },
    { key: 'losses', header: 'D', align: 'center', render: (row) => row.losses },
    { key: 'for', header: 'GP', align: 'center', render: (row) => row.goals_for },
    { key: 'against', header: 'GC', align: 'center', render: (row) => row.goals_against },
    { key: 'difference', header: 'SG', align: 'center', render: (row) => row.goal_difference },
    { key: 'points', header: 'Pts', align: 'right', render: (row) => <span className="font-bold text-slate-900">{row.points}</span> },
  ];
  const recentMatches = (matches.data ?? []).slice(0, 5);
  return (
    <>
      <PageHeader icon={ListOrdered} title="Classificação" description="Acompanhe o desempenho dos clubes na liga." />
      <div className="grid grid-cols-1 gap-5 xl:grid-cols-[minmax(0,1fr)_300px]">
        <section className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm"><div className="px-5 py-4"><h2 className="font-semibold text-slate-900">Tabela da liga</h2><p className="mt-1 text-sm text-slate-500">Ordem e critérios definidos pela API.</p></div><DataTable columns={columns} rows={standings.data ?? []} getRowKey={(row) => row.club_id} loading={standings.loading} error={standings.error} emptyMessage="A classificação aparecerá depois das primeiras partidas." onRetry={standings.reload} minWidthClass="min-w-[680px]" /></section>
        <aside className="space-y-5">
          <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm"><div className="flex items-center gap-2"><Trophy className="h-4 w-4 text-brand-600" /><h2 className="font-semibold text-slate-900">Líder da liga</h2></div>{standings.data?.[0] ? <><p className="mt-4 text-lg font-semibold text-slate-900">{standings.data[0].club_name}</p><p className="mt-1 text-sm text-slate-500">{standings.data[0].points} pontos · {standings.data[0].played} jogos</p></> : <p className="mt-3 text-sm text-slate-500">A tabela ainda não tem partidas.</p>}</section>
          <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm"><div className="flex items-center gap-2"><Swords className="h-4 w-4 text-slate-500" /><h2 className="font-semibold text-slate-900">Últimas partidas</h2></div>{matches.error ? <p className="mt-3 text-sm text-red-600">{matches.error}</p> : recentMatches.length ? <ul className="mt-3 divide-y divide-slate-100">{recentMatches.map((match) => <li key={match.id} className="py-3 first:pt-0 last:pb-0"><Link to={`/matches/${match.id}`} className="block hover:text-brand-700"><span className="flex items-center justify-between gap-2 text-sm"><span className="truncate">{match.home.club_name} × {match.away.club_name}</span><strong className="shrink-0">{match.result}</strong></span><span className="mt-1 block text-xs text-slate-500">{formatDateTime(match.played_at)}</span></Link></li>)}</ul> : !matches.loading && <p className="mt-3 text-sm text-slate-500">Nenhuma partida disputada.</p>}</section>
        </aside>
      </div>
    </>
  );
}
