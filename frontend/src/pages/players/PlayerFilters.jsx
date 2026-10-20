import { Search } from 'lucide-react';
import { controlClassName } from '../../components/ui/Field';
import { CONDITIONS, POSITIONS } from '../../constants/players';
import RangeFilter from './RangeFilter';

function FilterSelect({ label, value, onChange, options, allLabel, disabled = false }) {
  return (
    <select
      aria-label={label}
      value={value}
      disabled={disabled}
      onChange={(event) => onChange(event.target.value)}
      className={`${controlClassName(false)} min-w-[150px]`}
    >
      <option value="">{allLabel}</option>
      {options.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
  );
}

export default function PlayerFilters({ filters, errors, clubs, onChange }) {
  const clubOptions = clubs.map((club) => ({ value: String(club.id), label: `${club.name} (${club.acronym})` }));

  return (
    <section aria-label="Filtros de jogadores" className="mb-5 rounded-2xl border border-slate-200 bg-white p-4">
      <div className="flex flex-wrap items-start gap-3">
        <div className="relative min-w-[240px] flex-1">
          <Search className="pointer-events-none absolute left-3 top-3 h-4 w-4 text-slate-400" aria-hidden="true" />
          <input
            type="search"
            aria-label="Buscar por nome"
            placeholder="Buscar por nome"
            maxLength={100}
            value={filters.search}
            onChange={(event) => onChange('search', event.target.value)}
            className={`${controlClassName(Boolean(errors.search), 'pl-9 pr-3')} w-full`}
          />
          {errors.search && (
            <p className="mt-1 text-xs text-red-600" role="alert">
              {errors.search}
            </p>
          )}
        </div>
        <FilterSelect
          label="Filtrar por posição"
          value={filters.position}
          onChange={(value) => onChange('position', value)}
          options={POSITIONS}
          allLabel="Todas as posições"
        />
        <FilterSelect
          label="Filtrar por condição"
          value={filters.condition}
          onChange={(value) => onChange('condition', value)}
          options={CONDITIONS}
          allLabel="Todas as condições"
        />
        <FilterSelect
          label="Filtrar por clube"
          value={filters.clubId}
          onChange={(value) => onChange('clubId', value)}
          options={clubOptions}
          allLabel={filters.scope === 'free' ? 'Clube (só livres)' : 'Todos os clubes'}
          disabled={filters.scope === 'free'}
        />
        <RangeFilter
          label="Idade"
          minValue={filters.minAge}
          maxValue={filters.maxAge}
          onMinChange={(value) => onChange('minAge', value)}
          onMaxChange={(value) => onChange('maxAge', value)}
          error={errors.age}
        />
        <RangeFilter
          label="Valor"
          step="0.01"
          minValue={filters.minValue}
          maxValue={filters.maxValue}
          onMinChange={(value) => onChange('minValue', value)}
          onMaxChange={(value) => onChange('maxValue', value)}
          error={errors.value}
        />
      </div>
    </section>
  );
}
