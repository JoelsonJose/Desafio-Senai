import { useMemo, useState } from 'react';
import { Plus, Users } from 'lucide-react';
import Button from '../../components/ui/Button';
import DataTable from '../../components/ui/DataTable';
import { controlClassName } from '../../components/ui/Field';
import PageHeader from '../../components/ui/PageHeader';
import Pagination from '../../components/ui/Pagination';
import { useToast } from '../../components/ui/ToastProvider';
import { PLAYER_SORT_OPTIONS } from '../../constants/players';
import { useLeague } from '../../context/LeagueContext';
import useAsyncData from '../../hooks/useAsyncData';
import useDebouncedValue from '../../hooks/useDebouncedValue';
import { listPlayers } from '../../services/playersService';
import BuyPlayerModal from './BuyPlayerModal';
import DeletePlayerDialog from './DeletePlayerDialog';
import PlayerConditionModal from './PlayerConditionModal';
import PlayerFilters from './PlayerFilters';
import PlayerFormModal from './PlayerFormModal';
import PurchaseResultModal from './PurchaseResultModal';
import buildPlayerColumns from './playerColumns';
import { INITIAL_FILTERS, buildPlayerQuery } from './playerQuery';

const SCOPE_TABS = [
  { value: 'all', label: 'Todos' },
  { value: 'free', label: 'Livres' },
  { value: 'club', label: 'Em clubes' },
];

function ScopeTabs({ scope, counts, onChange }) {
  return (
    <div role="tablist" aria-label="Jogadores por situação" className="inline-flex rounded-lg bg-slate-100 p-1">
      {SCOPE_TABS.map((tab) => (
        <button
          key={tab.value}
          type="button"
          role="tab"
          aria-selected={scope === tab.value}
          onClick={() => onChange(tab.value)}
          className={`rounded-md px-3 py-1.5 text-sm ${
            scope === tab.value ? 'bg-white font-semibold text-slate-900 shadow-sm' : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          {tab.label} <span className="ml-1 text-slate-500">{counts[tab.value]}</span>
        </button>
      ))}
    </div>
  );
}

export default function PlayersPage() {
  const { clubs, totalPlayers, freePlayers, dataVersion, refreshSummary } = useLeague();
  const toast = useToast();
  const [filters, setFilters] = useState(INITIAL_FILTERS);
  const [modal, setModal] = useState(null);

  const debouncedFilters = useDebouncedValue(filters, 300);
  const { params, errors } = useMemo(() => buildPlayerQuery(debouncedFilters), [debouncedFilters]);
  const paramsKey = JSON.stringify(params);
  const players = useAsyncData(() => listPlayers(params), [paramsKey, dataVersion]);

  const clubsById = useMemo(() => new Map(clubs.map((club) => [club.id, club])), [clubs]);
  const rows = players.data?.data ?? [];
  const meta = players.data?.meta;

  const updateFilter = (name, value) => setFilters((previous) => ({ ...previous, [name]: value, page: 1 }));
  const updateScope = (scope) =>
    setFilters((previous) => ({
      ...previous,
      scope,
      clubId: scope === 'free' ? '' : previous.clubId,
      page: 1,
    }));
  const changePage = (page) => setFilters((previous) => ({ ...previous, page }));

  const closeModal = () => setModal(null);
  const handleDataChanged = () => {
    players.reload();
    refreshSummary();
  };
  const handleSaved = () => {
    closeModal();
    handleDataChanged();
  };
  const handleDeleted = () => {
    closeModal();
    if (rows.length === 1 && filters.page > 1) {
      changePage(filters.page - 1);
    }
    handleDataChanged();
  };
  const handleBought = (result) => {
    toast.success('Compra realizada', result.player_name);
    setModal({ type: 'purchase', result });
    handleDataChanged();
  };

  const columns = buildPlayerColumns({
    clubsById,
    onBuy: (player) => setModal({ type: 'buy', player }),
    onEdit: (player) => setModal({ type: 'edit', player }),
    onChangeCondition: (player) => setModal({ type: 'condition', player }),
    onDelete: (player) => setModal({ type: 'delete', player }),
  });

  const renderModal = () => {
    if (!modal) {
      return null;
    }
    switch (modal.type) {
      case 'create':
        return <PlayerFormModal onClose={closeModal} onSaved={handleSaved} />;
      case 'edit':
        return <PlayerFormModal player={modal.player} onClose={closeModal} onSaved={handleSaved} />;
      case 'condition':
        return <PlayerConditionModal player={modal.player} onClose={closeModal} onSaved={handleSaved} />;
      case 'delete':
        return <DeletePlayerDialog player={modal.player} onClose={closeModal} onDeleted={handleDeleted} />;
      case 'buy':
        return <BuyPlayerModal player={modal.player} clubs={clubs} onClose={closeModal} onBought={handleBought} />;
      case 'purchase':
        return <PurchaseResultModal result={modal.result} onClose={closeModal} />;
      default:
        return null;
    }
  };

  return (
    <>
      <PageHeader
        icon={Users}
        title="Jogadores"
        description="Cadastre jogadores, acompanhe os indicadores e compre os que estão livres."
        action={
          <Button icon={Plus} onClick={() => setModal({ type: 'create' })}>
            Novo jogador
          </Button>
        }
      />

      <PlayerFilters filters={filters} errors={errors} clubs={clubs} onChange={updateFilter} />

      <section className="rounded-2xl border border-slate-200 bg-white">
        <div className="flex flex-wrap items-center justify-between gap-3 p-4">
          <ScopeTabs
            scope={filters.scope}
            counts={{ all: totalPlayers, free: freePlayers, club: totalPlayers - freePlayers }}
            onChange={updateScope}
          />
          <label className="flex items-center gap-2 text-sm text-slate-600">
            Ordenar:
            <select
              value={filters.sort}
              onChange={(event) => updateFilter('sort', event.target.value)}
              className={controlClassName(false)}
            >
              {PLAYER_SORT_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>
        </div>

        <DataTable
          columns={columns}
          rows={rows}
          getRowKey={(player) => player.id}
          loading={players.loading}
          error={players.error}
          emptyMessage="Nenhum jogador encontrado com esses filtros."
          onRetry={players.reload}
        />

        {meta && <Pagination meta={meta} itemLabel="jogadores" onPageChange={changePage} />}
      </section>

      <p className="mt-4 text-xs text-slate-500">
        Overall e valor de mercado são calculados a cada consulta. A busca ignora maiúsculas, minúsculas e acentos.
      </p>

      {renderModal()}
    </>
  );
}
