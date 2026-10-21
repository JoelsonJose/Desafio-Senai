import { NavLink } from 'react-router-dom';
import { useState } from 'react';
import { Clock, ListOrdered, Menu, Plus, Shield, Swords, Trophy, Users, X } from 'lucide-react';
import { useLeague } from '../../context/LeagueContext';
import BrandMark from '../ui/BrandMark';

function SidebarLink({ to, icon: Icon, label, count }) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        `flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors ${
          isActive
            ? 'border border-slate-200 bg-white font-semibold text-slate-900 shadow-sm'
            : 'border border-transparent text-slate-600 hover:bg-slate-100'
        }`
      }
    >
      {({ isActive }) => (
        <>
          <Icon className={`h-4 w-4 ${isActive ? 'text-brand-600' : 'text-slate-500'}`} aria-hidden="true" />
          <span>{label}</span>
          {count !== undefined && <span className="ml-auto text-xs text-slate-500">{count}</span>}
        </>
      )}
    </NavLink>
  );
}

function SectionTitle({ children }) {
  return <p className="mb-2 px-3 text-xs font-medium text-slate-500">{children}</p>;
}

function formatLastMatch(match) {
  if (!match) {
    return 'Nenhuma partida jogada';
  }
  return `${match.home.club_name} ${match.home.goals} × ${match.away.goals} ${match.away.club_name}`;
}

export default function Sidebar() {
  const { clubs, totalPlayers, matchesCount, lastMatch, loadError } = useLeague();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <aside className="z-20 w-full shrink-0 border-b border-slate-200 bg-slate-50 md:sticky md:top-0 md:flex md:h-screen md:w-64 md:flex-col md:border-b-0 md:border-r">
      <div className="flex items-center justify-between px-4 py-3 md:justify-start md:gap-3 md:px-5 md:py-5">
        <div className="flex items-center gap-3">
          <BrandMark size={36} className="rounded-xl" />
          <div>
            <p className="font-semibold leading-tight text-slate-900">Passe</p>
            <p className="text-xs text-slate-500">Gestão da liga</p>
          </div>
        </div>
        <button
          type="button"
          className="inline-flex h-10 w-10 items-center justify-center rounded-lg border border-slate-200 bg-white text-slate-700 md:hidden"
          aria-label={mobileMenuOpen ? 'Fechar menu' : 'Abrir menu'}
          aria-expanded={mobileMenuOpen}
          aria-controls="main-navigation"
          onClick={() => setMobileMenuOpen((open) => !open)}
        >
          {mobileMenuOpen ? <X className="h-5 w-5" aria-hidden="true" /> : <Menu className="h-5 w-5" aria-hidden="true" />}
        </button>
      </div>

      <nav id="main-navigation" className={`${mobileMenuOpen ? 'block' : 'hidden'} space-y-6 overflow-y-auto px-3 pb-4 md:flex md:flex-1 md:flex-col`} aria-label="Menu principal">
        <div>
          <SectionTitle>Mercado</SectionTitle>
          <SidebarLink to="/players" icon={Users} label="Jogadores" count={totalPlayers} />
          <SidebarLink to="/clubs" icon={Shield} label="Clubes" count={clubs.length} />
        </div>

        <div>
          <SectionTitle>Liga</SectionTitle>
          <SidebarLink to="/matches" icon={Swords} label="Partidas" count={matchesCount} />
          <SidebarLink to="/standings" icon={ListOrdered} label="Classificação" />
        </div>

        <div>
          <div className="mb-2 flex items-center justify-between px-3">
            <p className="text-xs font-medium text-slate-500">Clubes</p>
            <NavLink to="/clubs" aria-label="Ver clubes e criar novo clube" className="text-slate-500 hover:text-slate-900">
              <Plus className="h-4 w-4" aria-hidden="true" />
            </NavLink>
          </div>
          {clubs.map((club) => (
            <NavLink
              key={club.id}
              to={`/clubs/${club.id}`}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-1.5 text-sm ${
                  isActive ? 'bg-white font-semibold text-slate-900 shadow-sm' : 'text-slate-600 hover:bg-slate-100'
                }`
              }
            >
              <span className="flex h-6 w-6 items-center justify-center rounded-md bg-slate-200 text-xs font-semibold text-slate-700">
                {club.name[0]}
              </span>
              {club.name}
            </NavLink>
          ))}
        </div>
      </nav>

      <div className="m-3 hidden space-y-3 rounded-2xl border border-slate-200 bg-white p-3 md:block">
        <div className="flex items-start gap-3">
          <Trophy className="mt-0.5 h-4 w-4 text-slate-500" aria-hidden="true" />
          <div>
            <p className="text-sm font-semibold text-slate-900">Liga Fictícia 2026</p>
            <p className="text-xs text-slate-500">
              {clubs.length} clubes · {totalPlayers} jogadores
            </p>
          </div>
        </div>
        <div className="flex items-start gap-3">
          <Swords className="mt-0.5 h-4 w-4 text-slate-500" aria-hidden="true" />
          <div>
            <p className="text-sm font-semibold text-slate-900">Última partida</p>
            <p className="text-xs text-slate-500">{formatLastMatch(lastMatch)}</p>
          </div>
        </div>
        {loadError && <p className="text-xs text-red-600">{loadError}</p>}
        <p className="flex items-center gap-2 border-t border-slate-100 pt-3 text-xs text-slate-500">
          <Clock className="h-3.5 w-3.5" aria-hidden="true" />
          Horário de Brasília (UTC−3)
        </p>
      </div>
    </aside>
  );
}
