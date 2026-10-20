import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { listClubs } from '../services/clubsService';
import { getErrorMessage } from '../services/http';
import { listMatches } from '../services/matchesService';
import { listPlayers } from '../services/playersService';

const LeagueContext = createContext(null);

const INITIAL_SUMMARY = {
  clubs: [],
  totalPlayers: 0,
  freePlayers: 0,
  matchesCount: 0,
  lastMatch: null,
};

export function LeagueProvider({ children }) {
  const [summary, setSummary] = useState(INITIAL_SUMMARY);
  const [loadError, setLoadError] = useState(null);
  const [dataVersion, setDataVersion] = useState(0);

  const refreshSummary = useCallback(async () => {
    try {
      const [clubs, allPlayers, freePlayers, matches] = await Promise.all([
        listClubs(),
        listPlayers({ limit: 1 }),
        listPlayers({ free: true, limit: 1 }),
        listMatches(),
      ]);
      setSummary({
        clubs,
        totalPlayers: allPlayers.meta.total_items,
        freePlayers: freePlayers.meta.total_items,
        matchesCount: matches.length,
        lastMatch: matches[0] ?? null,
      });
      setLoadError(null);
    } catch (error) {
      setLoadError(getErrorMessage(error));
    }
  }, []);

  const refreshAll = useCallback(async () => {
    setDataVersion((version) => version + 1);
    await refreshSummary();
  }, [refreshSummary]);

  useEffect(() => {
    refreshSummary();
  }, [refreshSummary]);

  const value = useMemo(
    () => ({ ...summary, loadError, dataVersion, refreshSummary, refreshAll }),
    [summary, loadError, dataVersion, refreshSummary, refreshAll],
  );

  return <LeagueContext.Provider value={value}>{children}</LeagueContext.Provider>;
}

export function useLeague() {
  const context = useContext(LeagueContext);
  if (!context) {
    throw new Error('useLeague deve ser usado dentro de LeagueProvider');
  }
  return context;
}
