import http from './http';

export async function buyPlayer(playerId, payload) {
  const { data } = await http.post(`/players/${playerId}/buy`, payload);
  return data;
}

export async function sellPlayer(playerId) {
  const { data } = await http.post(`/players/${playerId}/sell`);
  return data;
}

export async function listClubTransactions(clubId, params) {
  const { data } = await http.get(`/clubs/${clubId}/transactions`, { params });
  return data;
}

export async function exportClubTransactions(clubId, params) {
  const { data } = await http.get(`/clubs/${clubId}/transactions/export`, { params, responseType: 'blob' });
  return data;
}
