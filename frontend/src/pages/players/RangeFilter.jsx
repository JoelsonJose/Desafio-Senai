export default function RangeFilter({ label, minValue, maxValue, onMinChange, onMaxChange, error, step = '1' }) {
  const borderClass = error ? 'border-red-500' : 'border-slate-300';

  return (
    <div>
      <div className={`flex h-10 items-center gap-2 rounded-lg border bg-white px-3 text-sm ${borderClass}`}>
        <span className="text-slate-500">{label}</span>
        <input
          type="number"
          inputMode="decimal"
          step={step}
          aria-label={`${label}, mínimo`}
          placeholder="mín"
          value={minValue}
          onChange={(event) => onMinChange(event.target.value)}
          className="w-16 bg-transparent text-right outline-none"
        />
        <span className="text-slate-400">–</span>
        <input
          type="number"
          inputMode="decimal"
          step={step}
          aria-label={`${label}, máximo`}
          placeholder="máx"
          value={maxValue}
          onChange={(event) => onMaxChange(event.target.value)}
          className="w-16 bg-transparent text-right outline-none"
        />
      </div>
      {error && (
        <p className="mt-1 max-w-[16rem] text-xs text-red-600" role="alert">
          {error}
        </p>
      )}
    </div>
  );
}
