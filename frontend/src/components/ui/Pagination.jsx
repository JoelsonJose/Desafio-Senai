function buildPageList(current, total) {
  if (total <= 7) {
    return Array.from({ length: total }, (_, index) => index + 1);
  }
  const candidates = [1, current - 1, current, current + 1, total];
  const sorted = [...new Set(candidates)].filter((page) => page >= 1 && page <= total).sort((a, b) => a - b);
  const items = [];
  sorted.forEach((page, index) => {
    if (index > 0 && page - sorted[index - 1] > 1) {
      items.push('gap');
    }
    items.push(page);
  });
  return items;
}

function describeRange({ page, limit, total_items: totalItems }, itemLabel) {
  if (totalItems === 0) {
    return 'Nenhum resultado';
  }
  const first = (page - 1) * limit + 1;
  if (first > totalItems) {
    return `Nenhum item nesta página (${totalItems} ${itemLabel} no total)`;
  }
  const last = Math.min(page * limit, totalItems);
  return `Mostrando ${first}–${last} de ${totalItems} ${itemLabel}`;
}

const NAV_BUTTON = 'rounded-lg px-3 py-1.5 text-sm text-slate-700 hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-40';

export default function Pagination({ meta, itemLabel, onPageChange }) {
  const { page, total_pages: totalPages } = meta;
  const pages = buildPageList(page, totalPages);

  return (
    <div className="flex flex-wrap items-center justify-between gap-3 border-t border-slate-200 px-6 py-4">
      <p className="text-sm text-slate-600">{describeRange(meta, itemLabel)}</p>
      <nav aria-label="Paginação" className="flex items-center gap-1">
        <button type="button" className={NAV_BUTTON} disabled={page <= 1} onClick={() => onPageChange(page - 1)}>
          Anterior
        </button>
        {pages.map((item, index) =>
          item === 'gap' ? (
            <span key={`gap-${index}`} className="px-1 text-slate-400">
              …
            </span>
          ) : (
            <button
              key={item}
              type="button"
              aria-current={item === page ? 'page' : undefined}
              onClick={() => onPageChange(item)}
              className={`h-9 min-w-9 rounded-lg border px-2 text-sm ${
                item === page ? 'border-slate-300 bg-white font-semibold shadow-sm' : 'border-transparent text-slate-600 hover:bg-slate-100'
              }`}
            >
              {item}
            </button>
          ),
        )}
        <button
          type="button"
          className={NAV_BUTTON}
          disabled={page >= totalPages}
          onClick={() => onPageChange(page + 1)}
        >
          Próxima
        </button>
      </nav>
    </div>
  );
}
