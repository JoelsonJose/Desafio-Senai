import { useState } from 'react';
import { Swords } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../../components/ui/Button';
import DataTable from '../../components/ui/DataTable';
import { SelectField } from '../../components/ui/Field';
import Modal, { ModalFooter } from '../../components/ui/Modal';
import PageHeader from '../../components/ui/PageHeader';
import { useToast } from '../../components/ui/ToastProvider';
import useAction from '../../hooks/useAction';
import useAsyncData from '../../hooks/useAsyncData';
import { listClubs } from '../../services/clubsService';
import { listMatches, playMatch } from '../../services/matchesService';
import { formatDateTime } from '../../utils/format';
import { useLeague } from '../../context/LeagueContext';

function MatchModal({ clubs, onClose, onPlayed }) {
  const toast = useToast();
  const [homeClubId, setHomeClubId] = useState('');
  const [awayClubId, setAwayClubId] = useState('');
  const [errors, setErrors] = useState({});
  const action = useAction(playMatch);
  const options = clubs.map((club) => ({ value: String(club.id), label: `${club.name} (${club.acronym})` }));
  const submit = async (event) => {
    event.preventDefault();
    const nextErrors = {};
    if (!homeClubId) nextErrors.home = 'Escolha o clube mandante.';
    if (!awayClubId) nextErrors.away = 'Escolha o clube visitante.';
    if (homeClubId && homeClubId === awayClubId) nextErrors.away = 'Mandante e visitante devem ser diferentes.';
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length) return;
    const result = await action.run({ home_club_id: Number(homeClubId), away_club_id: Number(awayClubId) });
    if (result.ok) {
      toast.success('Partida realizada', `${result.data.home.club_name} ${result.data.result} ${result.data.away.club_name}`);
      onPlayed(result.data);
    }
  };
  return (
    <Modal title="Nova partida" description="Escolha os clubes. Ambos precisam ter escalação completa." onClose={onClose} widthClass="max-w-md">
      <form onSubmit={submit} className="space-y-4">
        <SelectField label="Mandante" name="home" value={homeClubId} onChange={(_, value) => setHomeClubId(value)} options={options} placeholder="Selecione o mandante" error={errors.home} />
        <SelectField label="Visitante" name="away" value={awayClubId} onChange={(_, value) => setAwayClubId(value)} options={options} placeholder="Selecione o visitante" error={errors.away} />
        <ModalFooter><Button variant="secondary" onClick={onClose}>Cancelar</Button><Button type="submit" disabled={action.pending}>{action.pending ? 'Jogando…' : 'Jogar partida'}</Button></ModalFooter>
      </form>
    </Modal>
  );
}

export default function MatchesPage() {
  const navigate = useNavigate();
  const { refreshSummary } = useLeague();
  const [showNewMatch, setShowNewMatch] = useState(false);
  const [clubFilter, setClubFilter] = useState('');
  const clubs = useAsyncData(() => listClubs(), []);
  const matches = useAsyncData(() => listMatches(clubFilter ? { club_id: Number(clubFilter) } : undefined), [clubFilter]);
  const rows = matches.data ?? [];
  const columns = [
    { key: 'date', header: 'Data e hora', render: (match) => formatDateTime(match.played_at) },
    { key: 'home', header: 'Mandante', render: (match) => match.home.club_name },
    { key: 'result', header: 'Placar', align: 'center', render: (match) => <span className="font-semibold">{match.result}</span> },
    { key: 'away', header: 'Visitante', render: (match) => match.away.club_name },
    { key: 'details', header: '', align: 'right', render: (match) => <Link className="font-medium text-brand-700 hover:underline" to={`/matches/${match.id}`}>Ver duelos</Link> },
  ];
  return (
    <>
      <PageHeader icon={Swords} title="Partidas" description="Jogue partidas e acompanhe os resultados da liga." action={<Button icon={Swords} onClick={() => setShowNewMatch(true)}>Nova partida</Button>} />
      {rows[0] && <section className="mb-5 rounded-xl border border-slate-200 bg-white p-5 shadow-sm"><div className="flex flex-wrap items-center justify-between gap-4"><div><p className="text-xs font-medium uppercase tracking-wide text-slate-500">Última partida</p><p className="mt-2 text-lg font-semibold text-slate-900">{rows[0].home.club_name} <span className="mx-2 rounded-lg bg-slate-100 px-3 py-1">{rows[0].result}</span> {rows[0].away.club_name}</p><p className="mt-1 text-sm text-slate-500">{formatDateTime(rows[0].played_at)}</p></div><Link to={`/matches/${rows[0].id}`}><Button variant="secondary">Ver duelos</Button></Link></div></section>}
      <section className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
        <div className="flex flex-wrap items-end justify-between gap-3 px-5 py-4"><div><h2 className="font-semibold text-slate-900">Histórico de partidas</h2><p className="mt-1 text-sm text-slate-500">Mais recentes primeiro</p></div><div className="w-full sm:w-64"><SelectField label="Filtrar por clube" name="clubFilter" value={clubFilter} onChange={(_, value) => setClubFilter(value)} options={(clubs.data ?? []).map((club) => ({ value: String(club.id), label: club.name }))} placeholder="Todos os clubes" /></div></div>
        <DataTable columns={columns} rows={rows} getRowKey={(match) => match.id} loading={matches.loading} error={matches.error} emptyMessage="Nenhuma partida jogada ainda." onRetry={matches.reload} minWidthClass="min-w-[700px]" />
      </section>
      {showNewMatch && <MatchModal clubs={clubs.data ?? []} onClose={() => setShowNewMatch(false)} onPlayed={(match) => { setShowNewMatch(false); refreshSummary(); navigate(`/matches/${match.id}`); }} />}
    </>
  );
}
