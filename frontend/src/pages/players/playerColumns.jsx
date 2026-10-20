import { Link } from 'react-router-dom';
import { HeartPulse, Pencil, ShoppingCart, Trash2 } from 'lucide-react';
import Avatar from '../../components/ui/Avatar';
import Badge, { ConditionBadge } from '../../components/ui/Badge';
import Button, { IconButton } from '../../components/ui/Button';
import { POSITIONS, labelOf } from '../../constants/players';
import { formatMoney } from '../../utils/format';

function AgilityMeter({ value }) {
  let barColor = 'bg-slate-500';
  if (value <= 30) {
    barColor = 'bg-red-500';
  } else if (value >= 80) {
    barColor = 'bg-green-600';
  }

  return (
    <div className="flex items-center gap-2">
      <span className="w-7 text-slate-700">{value}</span>
      <span className="h-1.5 w-16 overflow-hidden rounded-full bg-slate-200" aria-hidden="true">
        <span className={`block h-full rounded-full ${barColor}`} style={{ width: `${value}%` }} />
      </span>
    </div>
  );
}

function ClubCell({ player, clubsById }) {
  if (!player.club_id) {
    return <Badge>Livre</Badge>;
  }
  const club = clubsById.get(player.club_id);
  return (
    <div className="flex items-center gap-2">
      <Badge>{club?.acronym ?? '…'}</Badge>
      <Link to={`/clubs/${player.club_id}`} className="text-slate-900 underline underline-offset-2">
        {club?.name ?? 'Clube'}
      </Link>
    </div>
  );
}

export default function buildPlayerColumns({ clubsById, onBuy, onEdit, onChangeCondition, onDelete }) {
  return [
    {
      key: 'player',
      header: 'Jogador',
      render: (player) => (
        <div className="flex items-center gap-3">
          <Avatar name={player.name} />
          <div>
            <p className="font-semibold text-slate-900">{player.name}</p>
            <p className="text-xs text-slate-500">{labelOf(POSITIONS, player.position)}</p>
          </div>
        </div>
      ),
    },
    { key: 'position', header: 'Posição', render: (player) => <Badge>{player.position}</Badge> },
    {
      key: 'overall',
      header: 'Overall',
      align: 'right',
      render: (player) => <span className="font-semibold text-slate-900">{player.overall}</span>,
    },
    { key: 'agility', header: 'Agilidade', render: (player) => <AgilityMeter value={player.agility} /> },
    { key: 'age', header: 'Idade', align: 'right', render: (player) => player.age },
    { key: 'condition', header: 'Condição', render: (player) => <ConditionBadge condition={player.condition} /> },
    {
      key: 'baseValue',
      header: 'Valor base',
      align: 'right',
      render: (player) => <span className="text-slate-500">{formatMoney(player.base_value)}</span>,
    },
    {
      key: 'marketValue',
      header: 'Valor de mercado',
      align: 'right',
      render: (player) => <span className="font-semibold text-slate-900">{formatMoney(player.market_value)}</span>,
    },
    { key: 'club', header: 'Clube', render: (player) => <ClubCell player={player} clubsById={clubsById} /> },
    {
      key: 'actions',
      header: <span className="sr-only">Ações</span>,
      align: 'right',
      render: (player) => (
        <div className="flex items-center justify-end gap-1">
          {!player.club_id && (
            <Button variant="secondary" size="sm" icon={ShoppingCart} onClick={() => onBuy(player)}>
              Comprar
            </Button>
          )}
          <IconButton icon={Pencil} label={`Editar ${player.name}`} onClick={() => onEdit(player)} />
          <IconButton
            icon={HeartPulse}
            label={`Alterar condição de ${player.name}`}
            onClick={() => onChangeCondition(player)}
          />
          {!player.club_id && (
            <IconButton icon={Trash2} tone="danger" label={`Excluir ${player.name}`} onClick={() => onDelete(player)} />
          )}
        </div>
      ),
    },
  ];
}
