import http from './http';

export async function listPlayers(params) {
  const { data } = await http.get('/players', { params });
  return data;
}

export async function createPlayer(payload) {
  const { data } = await http.post('/players', payload);
  return data;
}

export async function updatePlayer(playerId, payload) {
  const { data } = await http.put(`/players/${playerId}`, payload);
  return data;
}

export async function changePlayerCondition(playerId, condition) {
  const { data } = await http.patch(`/players/${playerId}/condition`, { condition });
  return data;
}

export async function deletePlayer(playerId) {
  await http.delete(`/players/${playerId}`);
}
