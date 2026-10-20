import http from './http';

export async function listMatches(params) {
  const { data } = await http.get('/matches', { params });
  return data;
}

export async function getMatch(matchId) {
  const { data } = await http.get(`/matches/${matchId}`);
  return data;
}

export async function playMatch(payload) {
  const { data } = await http.post('/matches', payload);
  return data;
}

export async function getStandings() {
  const { data } = await http.get('/standings');
  return data;
}
