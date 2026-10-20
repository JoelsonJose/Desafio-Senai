const PALETTE = [
  'bg-blue-100 text-blue-700',
  'bg-pink-100 text-pink-700',
  'bg-cyan-100 text-cyan-700',
  'bg-violet-100 text-violet-700',
  'bg-emerald-100 text-emerald-700',
  'bg-slate-200 text-slate-700',
];

function getInitials(name) {
  const words = name.split(' ').filter(Boolean);
  if (words.length === 0) {
    return '?';
  }
  const first = words[0][0];
  const last = words.length > 1 ? words[words.length - 1][0] : '';
  return `${first}${last}`.toUpperCase();
}

function pickPalette(name) {
  let hash = 0;
  for (const char of name) {
    hash = (hash * 31 + char.codePointAt(0)) >>> 0;
  }
  return PALETTE[hash % PALETTE.length];
}

export default function Avatar({ name }) {
  return (
    <span
      className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-full text-xs font-semibold ${pickPalette(name)}`}
      aria-hidden="true"
    >
      {getInitials(name)}
    </span>
  );
}
