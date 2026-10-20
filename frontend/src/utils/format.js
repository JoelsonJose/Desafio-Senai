const moneyFormatter = new Intl.NumberFormat('pt-BR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const dateTimeFormatter = new Intl.DateTimeFormat('pt-BR', {
  timeZone: 'America/Sao_Paulo',
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
  hour12: false,
});

export function formatMoney(value) {
  if (value === null || value === undefined) {
    return '–';
  }
  return moneyFormatter.format(value);
}

export function formatSignedMoney(value) {
  const formatted = moneyFormatter.format(Math.abs(value));
  if (value > 0) {
    return `+${formatted}`;
  }
  if (value < 0) {
    return `−${formatted}`;
  }
  return formatted;
}

export function formatDateTime(isoString) {
  return dateTimeFormatter.format(new Date(isoString));
}

export function toApiDateTime(date, time) {
  return `${date}T${time}-03:00`;
}
