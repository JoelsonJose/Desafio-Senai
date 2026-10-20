import Button from './Button';
import Modal, { ModalFooter } from './Modal';

export default function SummaryModal({ title, description, rows, onClose }) {
  return (
    <Modal title={title} description={description} onClose={onClose} widthClass="max-w-md">
      <dl className="divide-y divide-slate-100 rounded-xl border border-slate-200">
        {rows.map((row) => (
          <div key={row.label} className="flex items-center justify-between gap-4 px-4 py-3 text-sm">
            <dt className="text-slate-600">{row.label}</dt>
            <dd className={row.emphasis ? 'font-semibold text-slate-900' : 'text-slate-900'}>{row.value}</dd>
          </div>
        ))}
      </dl>
      <ModalFooter>
        <Button onClick={onClose}>Fechar</Button>
      </ModalFooter>
    </Modal>
  );
}
