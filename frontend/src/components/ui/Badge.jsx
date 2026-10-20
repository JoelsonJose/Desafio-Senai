import { CONDITIONS, labelOf } from '../../constants/players';

const TONES = {
  neutral: { pill: 'border-slate-200 bg-white text-slate-700', dot: 'bg-slate-400' },
  brand: { pill: 'border-brand-100 bg-brand-50 text-brand-700', dot: 'bg-brand-600' },
  success: { pill: 'border-green-200 bg-green-50 text-green-700', dot: 'bg-green-600' },
  warning: { pill: 'border-amber-200 bg-amber-50 text-amber-700', dot: 'bg-amber-500' },
  danger: { pill: 'border-red-200 bg-red-50 text-red-700', dot: 'bg-red-600' },
};

const CONDITION_TONES = {
  APTO: 'success',
  MAL_CONDICIONADO: 'warning',
  DOENTE: 'danger',
};

export default function Badge({ tone = 'neutral', dot = false, children }) {
  const style = TONES[tone];
  return (
    <span
      className={`inline-flex items-center gap-1.5 whitespace-nowrap rounded-full border px-2.5 py-0.5 text-xs font-medium ${style.pill}`}
    >
      {dot && <span className={`h-1.5 w-1.5 rounded-full ${style.dot}`} aria-hidden="true" />}
      {children}
    </span>
  );
}

export function ConditionBadge({ condition }) {
  return (
    <Badge tone={CONDITION_TONES[condition] ?? 'neutral'} dot>
      {labelOf(CONDITIONS, condition)}
    </Badge>
  );
}
