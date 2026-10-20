import { AlertCircle } from 'lucide-react';
import Button from './Button';

const ALIGNMENT = {
  left: 'text-left',
  right: 'text-right',
  center: 'text-center',
};

function StatusRow({ colSpan, children }) {
  return (
    <tr>
      <td colSpan={colSpan} className="px-6 py-12 text-center text-sm text-slate-500">
        {children}
      </td>
    </tr>
  );
}

export default function DataTable({
  columns,
  rows,
  getRowKey,
  loading,
  error,
  emptyMessage,
  onRetry,
  minWidthClass = 'min-w-[960px]',
}) {
  const hasRows = rows.length > 0;
  let body;

  if (hasRows) {
    body = rows.map((row) => (
      <tr key={getRowKey(row)} className="border-b border-slate-100 last:border-0 hover:bg-slate-50/60">
        {columns.map((column) => (
          <td key={column.key} className={`px-4 py-3 align-middle ${ALIGNMENT[column.align ?? 'left']}`}>
            {column.render(row)}
          </td>
        ))}
      </tr>
    ));
  } else if (error) {
    body = (
      <StatusRow colSpan={columns.length}>
        <div className="flex flex-col items-center gap-3 text-red-700">
          <AlertCircle className="h-6 w-6" aria-hidden="true" />
          <p>{error}</p>
          {onRetry && (
            <Button variant="secondary" size="sm" onClick={onRetry}>
              Tentar novamente
            </Button>
          )}
        </div>
      </StatusRow>
    );
  } else if (loading) {
    body = <StatusRow colSpan={columns.length}>Carregando...</StatusRow>;
  } else {
    body = <StatusRow colSpan={columns.length}>{emptyMessage}</StatusRow>;
  }

  return (
    <div className="overflow-x-auto" aria-busy={loading}>
      <table className={`w-full ${minWidthClass} border-collapse text-sm`}>
        <thead>
          <tr className="border-y border-slate-200 bg-slate-50 text-xs font-medium text-slate-500">
            {columns.map((column) => (
              <th key={column.key} scope="col" className={`px-4 py-3 ${ALIGNMENT[column.align ?? 'left']}`}>
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className={loading && hasRows ? 'opacity-60' : ''}>{body}</tbody>
      </table>
    </div>
  );
}
