export const PAGE_SIZE = 10;

export const INITIAL_FILTERS = {
  search: '',
  position: '',
  condition: '',
  scope: 'all',
  clubId: '',
  minAge: '',
  maxAge: '',
  minValue: '',
  maxValue: '',
  sort: 'name',
  page: 1,
};

const INTEGER_PATTERN = /^\d+$/;
const DECIMAL_PATTERN = /^\d+(\.\d{1,2})?$/;

function parseRange(rawMin, rawMax, { pattern, lower, upper, formatError }) {
  const minText = rawMin.trim();
  const maxText = rawMax.trim();
  const provided = [minText, maxText].filter((text) => text !== '');
  const valid = provided.every(
    (text) => pattern.test(text) && Number(text) >= lower && Number(text) <= upper,
  );
  if (!valid) {
    return { error: formatError };
  }
  if (minText !== '' && maxText !== '' && Number(minText) > Number(maxText)) {
    return { error: 'O mínimo não pode ser maior que o máximo.' };
  }
  return { min: minText, max: maxText };
}

function applyRange(range, minKey, maxKey, errorKey, params, errors) {
  if (range.error) {
    errors[errorKey] = range.error;
    return;
  }
  if (range.min !== '') {
    params[minKey] = Number(range.min);
  }
  if (range.max !== '') {
    params[maxKey] = Number(range.max);
  }
}

export function buildPlayerQuery(filters) {
  const params = { page: filters.page, limit: PAGE_SIZE, sort: filters.sort };
  const errors = {};

  const search = filters.search.trim().replace(/\s+/g, ' ');
  if (search.length === 1) {
    errors.search = 'Digite ao menos 2 caracteres para buscar.';
  } else if (search.length >= 2) {
    params.search = search;
  }

  if (filters.position) {
    params.position = filters.position;
  }
  if (filters.condition) {
    params.condition = filters.condition;
  }
  if (filters.scope === 'free') {
    params.free = true;
  }
  if (filters.scope === 'club') {
    params.free = false;
  }
  if (filters.clubId && filters.scope !== 'free') {
    params.club_id = Number(filters.clubId);
  }

  const ageRange = parseRange(filters.minAge, filters.maxAge, {
    pattern: INTEGER_PATTERN,
    lower: 16,
    upper: 45,
    formatError: 'Use idades inteiras de 16 a 45.',
  });
  applyRange(ageRange, 'min_age', 'max_age', 'age', params, errors);

  const valueRange = parseRange(filters.minValue, filters.maxValue, {
    pattern: DECIMAL_PATTERN,
    lower: 0,
    upper: 550,
    formatError: 'Use valores de 0 a 550, com até 2 casas decimais.',
  });
  applyRange(valueRange, 'min_value', 'max_value', 'value', params, errors);

  return { params, errors };
}
