export const POSITIONS = [
  { value: 'GOL', label: 'Goleiro' },
  { value: 'ZAG', label: 'Zagueiro' },
  { value: 'LAT', label: 'Lateral' },
  { value: 'MEI', label: 'Meio-campo' },
  { value: 'ATA', label: 'Atacante' },
];

export const CONDITIONS = [
  { value: 'APTO', label: 'Apto' },
  { value: 'MAL_CONDICIONADO', label: 'Mal condicionado' },
  { value: 'DOENTE', label: 'Doente' },
];

export const ATTRIBUTES = [
  { key: 'pace', label: 'Velocidade' },
  { key: 'shooting', label: 'Finalização' },
  { key: 'passing', label: 'Passe' },
  { key: 'dribbling', label: 'Drible' },
  { key: 'defending', label: 'Defesa' },
  { key: 'physical', label: 'Físico' },
];

export const PLAYER_SORT_OPTIONS = [
  { value: 'name', label: 'Nome' },
  { value: 'value_desc', label: 'Valor de mercado (maior)' },
  { value: 'value_asc', label: 'Valor de mercado (menor)' },
  { value: 'age_desc', label: 'Idade (maior)' },
  { value: 'age_asc', label: 'Idade (menor)' },
  { value: 'position', label: 'Posição' },
];

export function labelOf(options, value) {
  return options.find((option) => option.value === value)?.label ?? value;
}
