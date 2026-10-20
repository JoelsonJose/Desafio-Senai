import http from './http';

export async function listClubs(params) {
  const { data } = await http.get('/clubs', { params });
  return data;
}

export async function getClub(clubId) {
  const { data } = await http.get(`/clubs/${clubId}`);
  return data;
}

export async function createClub(payload) {
  const { data } = await http.post('/clubs', payload);
  return data;
}

export async function getClubLineup(clubId) {
  const { data } = await http.get(`/clubs/${clubId}/lineup`);
  return data;
}

export async function saveClubLineup(clubId, payload) {
  const { data } = await http.put(`/clubs/${clubId}/lineup`, payload);
  return data;
}
