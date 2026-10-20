import { useEffect } from 'react';
import { ArrowRight, CheckCircle2, Clock3, History, Shield, ShoppingCart, Swords, Trophy, Users, ChevronRight } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { useLeague } from '../context/LeagueContext';
import BrandMark from '../components/ui/BrandMark';

const FLOW = [
  { number: '01', title: 'Jogadores', text: 'Nascem livres e Apto; o valor de mercado vem da condição, agilidade e idade.', icon: Users, to: '/players' },
  { number: '02', title: 'Clubes', text: 'Começam com 300,00 em caixa e sem jogadores.', icon: Shield, to: '/clubs' },
  { number: '03', title: 'Compra', text: 'Valor + taxa, até 50% do caixa, com contrato de fidelidade.', icon: ShoppingCart, to: '/players' },
  { number: '04', title: 'Venda', text: 'Multa antes do fim da fidelidade e comissão sobre o lucro.', icon: Swords, to: '/clubs' },
  { number: '05', title: 'Histórico', text: 'Toda compra e venda fica registrada, com totais e CSV.', icon: History, to: '/clubs' },
  { number: '06', title: 'Liga', text: 'Escale 5 titulares e jogue partidas que entram na classificação.', icon: Trophy, to: '/matches' },
];

function FieldArtwork() {
  return (
    <svg className="pointer-events-none absolute inset-0 h-full w-full text-slate-200" viewBox="0 0 1400 820" preserveAspectRatio="none" fill="none" aria-hidden="true">
      <rect x="1" y="1" width="1398" height="818" rx="28" stroke="currentColor" strokeWidth="1.5" />
      <path d="M700 1v818M1 204h198v412H1M1399 204h-198v412h198" stroke="currentColor" strokeWidth="1.5" />
      <path d="M1 307h70v206H1M1399 307h-70v206h70" stroke="currentColor" strokeWidth="1.5" />
      <path d="M199 300a120 120 0 0 1 0 220M1201 300a120 120 0 0 0 0 220" stroke="currentColor" strokeWidth="1.5" />
      <circle cx="700" cy="410" r="164" stroke="currentColor" strokeWidth="1.5" />
      <circle cx="700" cy="410" r="3" fill="currentColor" />
      <circle cx="134" cy="410" r="3.5" fill="currentColor" />
      <circle cx="1266" cy="410" r="3.5" fill="currentColor" />
      <path d="M1 362h-18v96H1M1399 362h18v96h-18" stroke="currentColor" strokeWidth="1.5" />
    </svg>
  );
}

export default function HomePage() {
  const navigate = useNavigate();
  const { clubs, totalPlayers, freePlayers, matchesCount, loadError } = useLeague();

  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Enter' && !['BUTTON', 'A', 'INPUT', 'SELECT', 'TEXTAREA'].includes(document.activeElement?.tagName)) {
        navigate('/players');
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [navigate]);

  return (
    <main className="flex min-h-screen items-center justify-center bg-white px-4 py-8 sm:px-8 sm:py-10 lg:px-12 lg:py-[8vh]">
      <section className="relative mx-auto flex min-h-[78vh] w-full max-w-[1666px] flex-col justify-between overflow-visible rounded-[30px] px-2 pb-7 pt-8 sm:px-5 sm:pt-10 lg:px-6 lg:pb-9 lg:pt-12">
        <FieldArtwork />
        <div className="relative z-10 mx-auto flex w-full flex-col items-center bg-white/95 px-3 pb-5 pt-1 text-center sm:px-8 lg:pt-5 xl:w-[78%] xl:max-w-[1200px]">
          <div className="flex items-center gap-6">
            <BrandMark />
            <span className="text-4xl font-bold tracking-tight text-slate-950 sm:text-5xl">Passe</span>
          </div>
          <div className="mt-6 inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-3 py-1.5 text-sm shadow-sm">
            <span className="h-2.5 w-2.5 rounded-full bg-emerald-600" />
            <span className="font-semibold text-slate-900">Liga Fictícia 2026</span>
            <span className="text-slate-400">·</span>
            <span className="text-slate-500">Gestão da liga</span>
          </div>
          <h1 className="mt-7 max-w-5xl text-3xl font-bold tracking-tight text-slate-950 sm:text-4xl lg:text-[42px]">
            Gestão da liga, do mercado à classificação
          </h1>
          <p className="mt-4 max-w-3xl text-base leading-7 text-slate-500 sm:text-lg">
            Cadastre jogadores, monte os clubes e negocie com contrato de fidelidade. Depois, escale os titulares e jogue partidas com placar calculado pelos atributos.
          </p>
          <div className="mt-7 flex flex-wrap items-center justify-center gap-3">
            <Link to="/players" className="inline-flex h-14 min-w-[278px] items-center justify-center gap-3 rounded-xl bg-brand-600 px-8 text-base font-semibold text-white shadow-sm transition hover:bg-brand-700">
              Iniciar <ArrowRight className="h-5 w-5" aria-hidden="true" />
            </Link>
            <span className="text-sm text-slate-500">ou tecle <kbd className="ml-1 rounded border border-slate-200 bg-slate-50 px-2 py-1 font-sans text-xs text-slate-600">Enter</kbd></span>
          </div>
          <div className="mt-6 flex flex-wrap items-center justify-center gap-x-5 gap-y-2 text-sm text-slate-500">
            <span className="inline-flex items-center gap-2"><Users className="h-4 w-4" /> <b className="text-slate-800">{loadError ? '—' : totalPlayers}</b> jogadores</span>
            <span className="hidden text-slate-300 sm:inline">·</span>
            <span className="inline-flex items-center gap-2"><Users className="h-4 w-4" /> <b className="text-slate-800">{loadError ? '—' : freePlayers}</b> livres</span>
            <span className="hidden text-slate-300 sm:inline">·</span>
            <span className="inline-flex items-center gap-2"><Shield className="h-4 w-4" /> <b className="text-slate-800">{loadError ? '—' : clubs.length}</b> clubes</span>
            <span className="hidden text-slate-300 sm:inline">·</span>
            <span className="inline-flex items-center gap-2"><Swords className="h-4 w-4" /> <b className="text-slate-800">{loadError ? '—' : matchesCount}</b> partidas</span>
            <span className="hidden text-slate-300 sm:inline">·</span>
            <span className="inline-flex items-center gap-2"><CheckCircle2 className="h-4 w-4" /> Não precisa de login</span>
          </div>
        </div>

        <nav aria-label="Etapas do sistema" className="home-flow relative z-10 mt-8 grid grid-cols-1 gap-5">
          {FLOW.map(({ number, title, text, icon: Icon, to }, index) => (
            <div key={number} className="relative min-w-0">
              <Link to={to} className="group block min-h-[174px] rounded-2xl border border-slate-200 bg-white p-4 text-left shadow-sm transition hover:border-brand-100 hover:shadow-md sm:min-h-[188px]">
                <div className="flex items-center gap-3">
                  <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-50 text-brand-600"><Icon className="h-5 w-5" aria-hidden="true" /></span>
                  <span className="font-mono text-sm text-slate-500">{number}</span>
                </div>
                <h2 className="mt-3 font-semibold text-slate-900">{title}</h2>
                <p className="mt-1 text-sm leading-5 text-slate-500">{text}</p>
              </Link>
              {index < FLOW.length - 1 && <ChevronRight style={{ right: -22 }} className="pointer-events-none absolute top-1/2 z-20 hidden h-4 w-4 -translate-y-1/2 text-slate-500 lg:block" aria-hidden="true" />}
            </div>
          ))}
        </nav>
        <footer className="relative z-10 mt-5 flex items-center justify-center gap-2 bg-white/90 text-center text-sm text-slate-500">
          <Clock3 className="h-4 w-4" aria-hidden="true" /> Liga Fictícia 2026 · Horário de Brasília (UTC−3)
        </footer>
      </section>
    </main>
  );
}
