import { Navigate, Route, Routes } from 'react-router-dom';
import AppShell from './components/layout/AppShell';
import HomePage from './pages/HomePage';
import ClubsPage from './pages/clubs/ClubsPage';
import ClubDetailPage from './pages/clubs/ClubDetailPage';
import MatchesPage from './pages/league/MatchesPage';
import MatchDetailPage from './pages/league/MatchDetailPage';
import StandingsPage from './pages/league/StandingsPage';
import PlayersPage from './pages/players/PlayersPage';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route element={<AppShell />}>
        <Route path="players" element={<PlayersPage />} />
        <Route path="clubs" element={<ClubsPage />} />
        <Route path="clubs/:clubId" element={<ClubDetailPage />} />
        <Route path="matches" element={<MatchesPage />} />
        <Route path="matches/:matchId" element={<MatchDetailPage />} />
        <Route path="standings" element={<StandingsPage />} />
        <Route path="*" element={<Navigate to="/players" replace />} />
      </Route>
    </Routes>
  );
}
