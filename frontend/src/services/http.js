import axios from 'axios';

const apiOrigin = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

const http = axios.create({
  baseURL: `${apiOrigin}/api/v1`,
  headers: { 'Content-Type': 'application/json' },
});

export function getErrorMessage(error) {
  const apiMessage = error?.response?.data?.message;
  if (typeof apiMessage === 'string' && apiMessage.trim()) {
    return apiMessage;
  }
  if (error?.request && !error.response) {
    return 'Não foi possível se comunicar com a API. Verifique se o backend está no ar.';
  }
  return 'Ocorreu um erro inesperado. Tente novamente.';
}

export default http;
