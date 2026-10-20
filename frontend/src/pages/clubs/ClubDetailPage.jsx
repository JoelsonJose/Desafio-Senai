import { useEffect, useState } from 'react';
import { ArrowDownToLine, Users, Shield, Wallet, TrendingUp, ClipboardList, UsersRound, CircleDollarSign, AlertTriangle, CheckCircle2, LockKeyhole } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';
import Button from '../../components/ui/Button';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import DataTable from '../../components/ui/DataTable';
import { SelectField } from '../../components/ui/Field';
import Modal from '../../components/ui/Modal';
import PageHeader from '../../components/ui/PageHeader';
import { useToast } from '../../components/ui/ToastProvider';
import useAction from '../../hooks/useAction';
import useAsyncData from '../../hooks/useAsyncData';
import { getClub, getClubLineup, saveClubLineup } from '../../services/clubsService';
import { exportClubTransactions, listClubTransactions, sellPlayer } from '../../services/marketService';
import { formatDateTime, formatMoney, formatSignedMoney } from '../../utils/format';
import { useLeague } from '../../context/LeagueContext';

const TABS = [
  { id: 'squad', label: 'Elenco', icon: UsersRound },
  { id: 'lineup', label: 'Escalação', icon: Shield },
  { id: 'history', label: 'Histórico', icon: ClipboardList },
];

function MetricCard({ icon: Icon, label, value }) {
  return <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm"><div className="flex items-center gap-2 text-xs text-slate-500"><Icon className="h-4 w-4" />{label}</div><p className="mt-3 text-lg font-semibold text-slate-900">R$ {formatMoney(value)}</p></div>;
}

function SaleResult({ result, onClose }) {
  return <Modal title="Venda concluída" description={result.player_name} onClose={onClose} widthClass="max-w-md"><dl className="divide-y divide-slate-100 rounded-xl border border-slate-200">{[
    ['Valor de compra', `R$ ${formatMoney(result.purchase_value)}`], ['Valor de venda', `R$ ${formatMoney(result.value)}`], ['Dias restantes', result.remaining_days], ['Multa', `R$ ${formatMoney(result.penalty)} (${result.penalty_rate}%)`], ['Lucro', `R$ ${formatMoney(result.profit)}`], ['Comissão', `R$ ${formatMoney(result.fee)}`], ['Crédito no caixa', `R$ ${formatMoney(result.cash_change)}`], ['Novo caixa', `R$ ${formatMoney(result.club_cash)}`],
  ].map(([label, value]) => <div key={label} className="flex justify-between gap-3 px-4 py-3 text-sm"><dt className="text-slate-600">{label}</dt><dd className="font-medium text-slate-900">{value}</dd></div>)}</dl><div className="mt-5 flex justify-end"><Button onClick={onClose}>Fechar</Button></div></Modal>;
}

const FORMATION_SLOTS = {
  '3-1': ['GOL', 'DEF', 'DEF', 'DEF', 'ATA'],
  '2-2': ['GOL', 'DEF', 'DEF', 'ATA', 'ATA'],
  '2-1-1': ['GOL', 'DEF', 'DEF', 'MEI', 'ATA'],
  '1-2-1': ['GOL', 'DEF', 'MEI', 'MEI', 'ATA'],
};

const FORMATION_POSITIONS = {
  '3-1': [[50, 88], [22, 68], [50, 68], [78, 68], [50, 25]],
  '2-2': [[50, 88], [32, 68], [68, 68], [32, 32], [68, 32]],
  '2-1-1': [[50, 88], [32, 68], [68, 68], [50, 47], [50, 25]],
  '1-2-1': [[50, 88], [50, 68], [32, 47], [68, 47], [50, 25]],
};

const POSITION_LABEL = { GOL: 'Goleiro', DEF: 'Defesa', MEI: 'Meio', ATA: 'Ataque' };
const ATTRIBUTE_LABEL = { pace: 'Velocidade', shooting: 'Finalização', passing: 'Passe', dribbling: 'Drible', defending: 'Defesa', physical: 'Físico' };

function initials(name = '') {
  return name.split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0]).join('').toUpperCase();
}

function LineupPitch({ formation, players }) {
  const points = FORMATION_POSITIONS[formation] ?? FORMATION_POSITIONS['3-1'];
  return <div className="lineup-pitch" aria-label={`Prévia da escalação na formação ${formation}`}>
    <svg className="pitch-lines" viewBox="0 0 300 400" preserveAspectRatio="none" aria-hidden="true">
      <rect x="8" y="8" width="284" height="384" rx="8" />
      <path d="M8 200h284M65 8v70h170V8M110 8v26h80V8M65 392v-70h170v70M110 392v-26h80v26" />
      <circle cx="150" cy="200" r="42" /><circle cx="150" cy="200" r="2" className="pitch-dot" />
      <circle cx="150" cy="56" r="2" className="pitch-dot" /><circle cx="150" cy="344" r="2" className="pitch-dot" />
      <path d="M104 78a48 48 0 0 0 92 0M104 322a48 48 0 0 1 92 0" />
      <path d="M126 8v-5h48v5M126 392v5h48v-5" />
    </svg>
    <span className="pitch-direction">ATAQUE</span>
    {points.map(([left, top], index) => {
      const player = players[index];
      return <div key={`slot-${index}`} className="lineup-marker" style={{ left: `${left}%`, top: `${top}%` }} title={player ? `${player.name} · ${player.position}` : POSITION_LABEL[FORMATION_SLOTS[formation]?.[index]]}>
        <span className={`lineup-avatar ${player?.condition === 'MAL_CONDICIONADO' ? 'lineup-avatar-warn' : ''}`}>{player ? initials(player.name) : index === 0 ? 'GOL' : '—'}</span>
        {player?.condition === 'MAL_CONDICIONADO' && <span className="lineup-condition-icon" title="Mal condicionado"><AlertTriangle size={12} /></span>}
        <span className="lineup-player-label">{player?.name ?? POSITION_LABEL[FORMATION_SLOTS[formation]?.[index]]}</span>
      </div>;
    })}
  </div>;
}

function FormationCard({ value, selected, onClick }) {
  const slots = FORMATION_SLOTS[value];
  return <button type="button" aria-pressed={selected} onClick={onClick} className={`formation-card ${selected ? 'formation-card-selected' : ''}`}>
    <span className="formation-mini-pitch" aria-hidden="true">{slots.map((position, index) => <i key={`${position}-${index}`} className={position === 'GOL' ? 'keeper' : ''} />)}</span>
    <span className="min-w-0"><strong className="block text-sm">{value}</strong><span className="mt-1 block text-xs leading-4 text-slate-500">{value === '3-1' ? 'Defesa 3, ataque 1' : value === '2-2' ? 'Defesa 2, ataque 2' : value === '2-1-1' ? 'Defesa 2, meio 1, ataque 1' : 'Defesa 1, meio 2, ataque 1'}</span></span>
  </button>;
}

function ClubLineup({ clubId, players }) {
  const toast = useToast();
  const lineup = useAsyncData(() => getClubLineup(clubId), [clubId]);
  const [formation, setFormation] = useState('3-1');
  const [playerIds, setPlayerIds] = useState(['', '', '', '', '']);
  const action = useAction(saveClubLineup);
  useEffect(() => {
    if (lineup.data?.players) {
      setFormation(lineup.data.formation ?? '3-1');
      setPlayerIds(Array.from({ length: 5 }, (_, index) => String(lineup.data.players[index]?.id ?? '')));
    }
  }, [lineup.data]);
  const eligible = players.filter((player) => player.condition !== 'DOENTE');
  const goalkeepers = eligible.filter((player) => player.position === 'GOL');
  const fieldPlayers = eligible.filter((player) => player.position !== 'GOL');
  const slots = FORMATION_SLOTS[formation];
  const selectedPlayers = playerIds.map((id) => players.find((player) => String(player.id) === id) ?? null);
  const usedIds = new Set(playerIds.filter(Boolean));
  const malConditioned = selectedPlayers.filter((player) => player?.condition === 'MAL_CONDICIONADO');
  const onSave = async () => {
    if (playerIds.some((id) => !id) || new Set(playerIds).size !== 5) {
      toast.error('Selecione cinco jogadores diferentes, incluindo um goleiro.');
      return;
    }
    const result = await action.run(clubId, { formation, player_ids: playerIds.map(Number) });
    if (result.ok) {
      toast.success('Escalação salva');
      lineup.reload();
    }
  };
  const optionsFor = (index) => {
    const role = slots[index];
    const candidates = role === 'GOL' ? goalkeepers : role === 'DEF' ? fieldPlayers.filter((player) => ['ZAG', 'LAT'].includes(player.position)) : eligible.filter((player) => player.position === role);
    return candidates.filter((player) => String(player.id) === playerIds[index] || !usedIds.has(String(player.id))).map((player) => ({ value: String(player.id), label: `${player.name} · ${player.position} · OVR ${player.overall}` }));
  };
  const handleFormation = (next) => {
    if (next === formation) return;
    setFormation(next);
    setPlayerIds(['', '', '', '', '']);
  };
  const getUsedAttribute = (player) => {
    if (!player) return '—';
    const attribute = player.position === 'GOL' ? 'defending' : player.position === 'ATA' ? 'shooting' : player.position === 'MEI' ? 'passing' : 'dribbling';
    return `${ATTRIBUTE_LABEL[attribute]} ${player[attribute]}`;
  };
  const reserves = players.filter((player) => !usedIds.has(String(player.id)));

  return <>
    <div className="lineup-layout">
      <section className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 px-5 py-4">
          <div><h2 className="font-semibold text-slate-900">Titulares</h2><p className="mt-1 text-sm text-slate-500">1 goleiro e 4 jogadores de linha, na formação escolhida.</p></div>
          {lineup.data?.complete && playerIds.every(Boolean) && <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-medium text-emerald-700"><CheckCircle2 size={14} /> Completa</span>}
          {lineup.error && <span className="rounded-full bg-amber-50 px-2.5 py-1 text-xs font-medium text-amber-800">Escalação incompleta</span>}
        </div>
        <div className="border-b border-slate-200 px-5 py-4"><h3 className="text-sm font-semibold text-slate-800">Formação</h3><p className="mt-1 text-xs text-slate-500">Os números contam os jogadores de linha de trás para frente. O goleiro fica fora da conta.</p>
          <div className="mt-3 grid grid-cols-2 gap-2 xl:grid-cols-4">{Object.keys(FORMATION_SLOTS).map((item) => <FormationCard key={item} value={item} selected={formation === item} onClick={() => handleFormation(item)} />)}</div>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full min-w-[680px] text-sm">
            <thead><tr className="bg-slate-50 text-left text-xs text-slate-500"><th className="px-4 py-3 font-medium">Vaga</th><th className="px-4 py-3 font-medium">Titular</th><th className="px-4 py-3 font-medium">Condição</th><th className="px-4 py-3 text-right font-medium">Overall</th><th className="px-4 py-3 font-medium">Atributo usado</th></tr></thead>
            <tbody>{slots.map((role, index) => {
              const player = selectedPlayers[index];
              return <tr key={`lineup-slot-${index}`} className="border-t border-slate-100">
                <td className="px-4 py-3"><span className="font-medium text-slate-800">{POSITION_LABEL[role]}</span><span className="mt-0.5 block text-xs text-slate-500">{role === 'DEF' ? 'ZAG ou LAT' : role}</span></td>
                <td className="px-3 py-2"><SelectField label="" name={`lineup-${index}`} value={playerIds[index]} onChange={(_, value) => setPlayerIds((current) => current.map((item, i) => i === index ? value : item))} options={optionsFor(index)} placeholder="Selecione um jogador" aria-label={`Titular: ${POSITION_LABEL[role]}`} /></td>
                <td className="px-4 py-3">{player ? <span className={`condition-pill ${player.condition === 'MAL_CONDICIONADO' ? 'condition-pill-warn' : 'condition-pill-ok'}`}><i />{player.condition === 'MAL_CONDICIONADO' ? 'Mal condicionado' : 'Apto'}</span> : <span className="text-slate-400">—</span>}</td>
                <td className="px-4 py-3 text-right font-medium text-slate-700">{player?.overall ?? '—'}</td>
                <td className="px-4 py-3 text-slate-600">{getUsedAttribute(player)}</td>
              </tr>;
            })}</tbody>
          </table>
        </div>
        {malConditioned.length > 0 && <div className="mx-5 mt-4 flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800"><AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" /><span>{malConditioned.map((player) => player.name).join(' e ')} {malConditioned.length === 1 ? 'está' : 'estão'} mal condicionado{malConditioned.length === 1 ? '' : 's'}. Pode{malConditioned.length === 1 ? '' : 'm'} jogar, mas cada atributo vale 15% a menos durante a partida.</span></div>}
        <div className="flex flex-wrap items-center justify-between gap-4 border-t border-slate-200 px-5 py-4"><p className="max-w-lg text-xs leading-5 text-slate-500">A escalação vale até ser trocada. Titular vendido ou que fica doente sai dela; o clube só volta a jogar depois de escalar de novo.</p><div className="flex gap-2"><Button variant="secondary" onClick={() => { setFormation(lineup.data?.formation ?? '3-1'); setPlayerIds(Array.from({ length: 5 }, (_, index) => String(lineup.data?.players?.[index]?.id ?? ''))); }}>Cancelar</Button><Button onClick={onSave} disabled={action.pending || eligible.length < 5 || playerIds.some((id) => !id) || new Set(playerIds).size !== 5}>{action.pending ? 'Salvando…' : 'Salvar escalação'}</Button></div></div>
      </section>

      <aside className="space-y-4">
        <section className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
          <div className="border-b border-slate-200 px-4 py-3"><h2 className="font-semibold text-slate-900">Em campo</h2><p className="mt-1 text-xs text-slate-500">Prévia da formação {formation}, do goleiro ao ataque.</p></div>
          <div className="p-3"><LineupPitch formation={formation} players={selectedPlayers} /></div>
          <p className="flex items-start gap-2 border-t border-slate-200 bg-slate-50 px-4 py-3 text-xs leading-5 text-slate-500"><Shield size={15} className="mt-0.5 shrink-0" /> Os três jogadores de linha mais avançados atacam; o goleiro e os dois mais recuados defendem.</p>
        </section>
        <section className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
          <div className="border-b border-slate-200 px-4 py-3"><h2 className="font-semibold text-slate-900">Reservas</h2><p className="mt-1 text-xs text-slate-500">Jogadores do elenco fora da escalação.</p></div>
          {reserves.length ? <ul className="divide-y divide-slate-100">{reserves.map((player) => <li key={player.id} className="flex items-center justify-between gap-3 px-4 py-3"><span className="flex min-w-0 items-center gap-3"><span className="lineup-reserve-avatar">{initials(player.name)}</span><span className="min-w-0"><strong className="block truncate text-sm font-medium text-slate-800">{player.name}</strong><span className="text-xs text-slate-500">{player.position === 'GOL' ? 'Goleiro' : player.position === 'MEI' ? 'Meio-campo' : player.position === 'ATA' ? 'Atacante' : player.position === 'ZAG' ? 'Zagueiro' : 'Lateral'}</span></span></span><span className={`condition-pill ${player.condition === 'DOENTE' ? 'condition-pill-sick' : player.condition === 'MAL_CONDICIONADO' ? 'condition-pill-warn' : 'condition-pill-ok'}`}><i />{player.condition === 'DOENTE' ? 'Doente' : player.condition === 'MAL_CONDICIONADO' ? 'Mal cond.' : 'Apto'}</span></li>)}</ul> : <p className="px-4 py-5 text-sm text-slate-500">Todos os jogadores elegíveis estão entre os titulares.</p>}
          {reserves.some((player) => player.condition === 'DOENTE') && <div className="flex items-center gap-2 border-t border-slate-100 bg-slate-50 px-4 py-3 text-xs text-slate-500"><LockKeyhole size={14} /> Jogador doente não pode ser escalado.</div>}
        </section>
      </aside>
    </div>
    <p className="mt-5 text-xs leading-5 text-slate-500">Escalação na formação {formation}, com os titulares na ordem do elenco. Salvar troca a formação e os 5 titulares de uma vez; titular doente, fora do elenco ou posições que não fecham a formação são recusados (RN19).</p>
  </>;
}

export default function ClubDetailPage() {
  const { clubId } = useParams();
  const toast = useToast();
  const { refreshAll } = useLeague();
  const [tab, setTab] = useState('squad');
  const [transactionTypes, setTransactionTypes] = useState([]);
  const [playerFilter, setPlayerFilter] = useState('');
  const [sellTarget, setSellTarget] = useState(null);
  const [saleResult, setSaleResult] = useState(null);
  const club = useAsyncData(() => getClub(clubId), [clubId]);
  const allTransactions = useAsyncData(() => listClubTransactions(clubId), [clubId]);
  const query = { ...(transactionTypes.length ? { type: transactionTypes.join(',') } : {}), ...(playerFilter ? { player_id: Number(playerFilter) } : {}) };
  const transactions = useAsyncData(() => listClubTransactions(clubId, query), [clubId, transactionTypes.join(','), playerFilter]);
  const sellAction = useAction(sellPlayer);
  const players = club.data?.players ?? [];
  const toggleType = (type) => setTransactionTypes((current) => current.includes(type) ? current.filter((item) => item !== type) : [...current, type]);
  const confirmSale = async () => {
    if (!sellTarget) return;
    const result = await sellAction.run(sellTarget.id);
    if (result.ok) {
      setSellTarget(null);
      setSaleResult(result.data);
      toast.success('Venda realizada', result.data.player_name);
      club.reload(); transactions.reload(); allTransactions.reload(); refreshAll();
    }
  };
  const exportCsv = async () => {
    try {
      const blob = await exportClubTransactions(clubId, query);
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url; link.download = `historico-clube-${clubId}.csv`; link.click(); URL.revokeObjectURL(url);
    } catch (error) {
      const message = error?.response?.data?.message ?? 'Não foi possível exportar o histórico.';
      toast.error(message);
    }
  };
  const rosterColumns = [
    { key: 'player', header: 'Jogador', render: (player) => <div><p className="font-medium text-slate-900">{player.name}</p><p className="text-xs text-slate-500">{player.position} · OVR {player.overall}</p></div> },
    { key: 'condition', header: 'Condição', render: (player) => player.condition },
    { key: 'market', header: 'Valor de mercado', align: 'right', render: (player) => `R$ ${formatMoney(player.market_value)}` },
    { key: 'contract', header: 'Fim da fidelidade', render: (player) => player.contract?.loyalty_until ? formatDateTime(player.contract.loyalty_until) : '—' },
    { key: 'days', header: 'Dias restantes', align: 'right', render: (player) => player.contract?.remaining_days ?? '—' },
    { key: 'penalty', header: 'Multa', align: 'right', render: (player) => player.contract ? `${player.contract.penalty_rate}%` : '—' },
    { key: 'actions', header: '', align: 'right', render: (player) => <Button size="sm" variant="secondary" onClick={() => setSellTarget(player)}>Vender</Button> },
  ];
  const transactionColumns = [
    { key: 'date', header: 'Data e hora', render: (item) => formatDateTime(item.created_at) },
    { key: 'type', header: 'Tipo', render: (item) => item.type === 'COMPRA' ? 'Compra' : 'Venda' },
    { key: 'player', header: 'Jogador', render: (item) => item.player_name },
    { key: 'value', header: 'Valor', align: 'right', render: (item) => `R$ ${formatMoney(item.value)}` },
    { key: 'fee', header: 'Taxa / comissão', align: 'right', render: (item) => `R$ ${formatMoney(item.fee)}` },
    { key: 'penalty', header: 'Multa', align: 'right', render: (item) => `R$ ${formatMoney(item.penalty)}` },
    { key: 'profit', header: 'Lucro', align: 'right', render: (item) => `R$ ${formatMoney(item.profit)}` },
    { key: 'cash', header: 'Variação do caixa', align: 'right', render: (item) => <span className={item.cash_change < 0 ? 'text-red-600' : 'text-green-700'}>{formatSignedMoney(item.cash_change)}</span> },
  ];

  return <>
    <PageHeader icon={Shield} title={club.data?.name ?? 'Clube'} description={club.data ? `${club.data.acronym} · ${players.length} jogadores no elenco` : 'Resumo, elenco, escalação e histórico do clube.'} action={<Link to="/players"><Button icon={Users}>Comprar jogadores</Button></Link>} />
    {club.error ? <section className="rounded-xl border border-red-200 bg-white p-6 text-sm text-red-700">{club.error} <button className="ml-2 font-semibold underline" onClick={club.reload}>Tentar novamente</button></section> : <>
      <div className="mb-5 grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4"><MetricCard icon={Wallet} label="Caixa" value={club.data?.cash} /><MetricCard icon={TrendingUp} label="Valor do elenco" value={club.data?.squad_value} /><MetricCard icon={CircleDollarSign} label="Patrimônio" value={club.data?.net_worth} /><div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm"><div className="flex items-center gap-2 text-xs text-slate-500"><Users className="h-4 w-4" />Elenco</div><p className="mt-3 text-lg font-semibold text-slate-900">{players.length} <span className="text-sm font-normal text-slate-500">de 12 jogadores</span></p></div></div>
      <div className="mb-4 flex gap-1 border-b border-slate-200">{TABS.map(({ id, label, icon: Icon }) => <button key={id} type="button" role="tab" aria-selected={tab === id} onClick={() => setTab(id)} className={`inline-flex items-center gap-2 border-b-2 px-4 py-3 text-sm ${tab === id ? 'border-brand-600 font-semibold text-brand-700' : 'border-transparent text-slate-500 hover:text-slate-800'}`}><Icon className="h-4 w-4" />{label}{id === 'squad' && <span className="text-xs">{players.length}</span>}</button>)}</div>
      {tab === 'squad' && <section className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm"><div className="px-5 py-4"><h2 className="font-semibold text-slate-900">Elenco</h2><p className="mt-1 text-sm text-slate-500">Valores e informações de contrato carregados da API.</p></div><DataTable columns={rosterColumns} rows={players} getRowKey={(player) => player.id} loading={club.loading} error={null} emptyMessage="Este clube ainda não tem jogadores." minWidthClass="min-w-[950px]" /></section>}
      {tab === 'lineup' && <ClubLineup clubId={Number(clubId)} players={players} />}
      {tab === 'history' && <section className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm"><div className="flex flex-wrap items-center justify-between gap-4 px-5 py-4"><div><h2 className="font-semibold text-slate-900">Histórico de transações</h2><p className="mt-1 text-sm text-slate-500">Compras e vendas registradas para este clube.</p></div><Button variant="secondary" icon={ArrowDownToLine} onClick={exportCsv}>Exportar CSV</Button></div><div className="flex flex-wrap items-end gap-3 border-t border-slate-100 px-5 py-4"><div className="flex gap-2"><button type="button" onClick={() => toggleType('COMPRA')} className={`rounded-lg border px-3 py-2 text-sm ${transactionTypes.includes('COMPRA') ? 'border-brand-200 bg-brand-50 text-brand-700' : 'border-slate-200 text-slate-600'}`}>Compras</button><button type="button" onClick={() => toggleType('VENDA')} className={`rounded-lg border px-3 py-2 text-sm ${transactionTypes.includes('VENDA') ? 'border-brand-200 bg-brand-50 text-brand-700' : 'border-slate-200 text-slate-600'}`}>Vendas</button></div><div className="w-full sm:w-64"><SelectField label="Jogador" name="playerFilter" value={playerFilter} onChange={(_, value) => setPlayerFilter(value)} options={[...new Map((allTransactions.data?.data ?? []).map((item) => [item.player_id, { value: String(item.player_id), label: item.player_name }])).values()]} placeholder="Todos os jogadores" /></div>{(transactionTypes.length > 0 || playerFilter) && <button type="button" className="pb-2 text-sm text-brand-700 hover:underline" onClick={() => { setTransactionTypes([]); setPlayerFilter(''); }}>Limpar filtros</button>}</div><DataTable columns={transactionColumns} rows={transactions.data?.data ?? []} getRowKey={(item) => item.id} loading={transactions.loading} error={transactions.error} emptyMessage="Nenhuma transação encontrada." onRetry={transactions.reload} minWidthClass="min-w-[1050px]" />
        <div className="grid grid-cols-2 gap-px border-t border-slate-200 bg-slate-200 sm:grid-cols-5">{[['Total pago', transactions.data?.summary?.total_spent], ['Total recebido', transactions.data?.summary?.total_received], ['Taxas e comissões', transactions.data?.summary?.total_fees], ['Multas', transactions.data?.summary?.total_penalties], ['Lucro total', transactions.data?.summary?.total_profit]].map(([label, value]) => <div key={label} className="bg-white p-4"><p className="text-xs text-slate-500">{label}</p><p className="mt-1 font-semibold text-slate-900">R$ {formatMoney(value)}</p></div>)}</div></section>}
    </>}
    {sellTarget && <ConfirmDialog title={`Vender ${sellTarget.name}?`} description="A API calculará multa, comissão e crédito de caixa com base no contrato atual." confirmLabel={sellAction.pending ? 'Vendendo…' : 'Confirmar venda'} pending={sellAction.pending} onConfirm={confirmSale} onCancel={() => setSellTarget(null)} />}
    {saleResult && <SaleResult result={saleResult} onClose={() => setSaleResult(null)} />}
  </>;
}
