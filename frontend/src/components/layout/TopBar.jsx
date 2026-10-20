import { Fragment } from 'react';
import { useLocation } from 'react-router-dom';
import { ChevronRight, RefreshCw } from 'lucide-react';
import { useLeague } from '../../context/LeagueContext';
import useAction from '../../hooks/useAction';
import { IconButton } from '../ui/Button';

function buildBreadcrumb(pathname, clubs) {
  const [section, id] = pathname.split('/').filter(Boolean);
  switch (section) {
    case 'players':
      return ['Mercado', 'Jogadores'];
    case 'clubs': {
      if (!id) {
        return ['Mercado', 'Clubes'];
      }
      const club = clubs.find((item) => String(item.id) === id);
      return ['Mercado', 'Clubes', club?.name ?? 'Clube'];
    }
    case 'matches':
      return id ? ['Liga', 'Partidas', 'Partida'] : ['Liga', 'Partidas'];
    case 'standings':
      return ['Liga', 'Classificação'];
    default:
      return [];
  }
}

export default function TopBar() {
  const { pathname } = useLocation();
  const { clubs, refreshAll } = useLeague();
  const refresh = useAction(refreshAll);
  const trail = buildBreadcrumb(pathname, clubs);

  return (
    <header className="flex h-14 items-center justify-between border-b border-slate-200 bg-white px-8">
      <nav aria-label="Localização" className="flex items-center gap-2 text-sm">
        {trail.map((item, index) => (
          <Fragment key={`${index}-${item}`}>
            {index > 0 && <ChevronRight className="h-4 w-4 text-slate-400" aria-hidden="true" />}
            <span className={index === trail.length - 1 ? 'font-medium text-slate-900' : 'text-slate-500'}>{item}</span>
          </Fragment>
        ))}
      </nav>
      <IconButton icon={RefreshCw} label="Atualizar dados" onClick={() => refresh.run()} disabled={refresh.pending} />
    </header>
  );
}
