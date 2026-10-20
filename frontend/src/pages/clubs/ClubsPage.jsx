import { useState } from 'react';
import { Plus, Shield } from 'lucide-react';
import { Link } from 'react-router-dom';
import Button from '../../components/ui/Button';
import DataTable from '../../components/ui/DataTable';
import { SelectField, TextField } from '../../components/ui/Field';
import Modal, { ModalFooter } from '../../components/ui/Modal';
import PageHeader from '../../components/ui/PageHeader';
import { useToast } from '../../components/ui/ToastProvider';
import useAction from '../../hooks/useAction';
import useAsyncData from '../../hooks/useAsyncData';
import { createClub, listClubs } from '../../services/clubsService';
import { formatMoney } from '../../utils/format';
import { useLeague } from '../../context/LeagueContext';

function NewClubModal({ onClose, onCreated }) {
  const toast = useToast();
  const [form, setForm] = useState({ name: '', acronym: '' });
  const [errors, setErrors] = useState({});
  const action = useAction(createClub);

  const submit = async (event) => {
    event.preventDefault();
    const nextErrors = {};
    if (form.name.trim().length < 3 || form.name.trim().length > 60) nextErrors.name = 'Informe um nome entre 3 e 60 caracteres.';
    if (!/^[a-zA-Z]{3}$/.test(form.acronym.trim())) nextErrors.acronym = 'A sigla deve ter exatamente 3 letras.';
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length) return;
    const result = await action.run({ name: form.name.trim(), acronym: form.acronym.trim().toUpperCase() });
    if (result.ok) {
      toast.success('Clube criado', result.data.name);
      onCreated();
    }
  };

  return (
    <Modal title="Novo clube" description="Cadastre um clube para participar da liga." onClose={onClose} widthClass="max-w-md">
      <form onSubmit={submit}>
        <div className="space-y-4">
          <TextField label="Nome do clube" name="name" value={form.name} onChange={(name, value) => setForm((current) => ({ ...current, [name]: value }))} error={errors.name} maxLength={60} placeholder="Ex.: Águias FC" />
          <TextField label="Sigla" name="acronym" value={form.acronym} onChange={(name, value) => setForm((current) => ({ ...current, [name]: value.toUpperCase() }))} error={errors.acronym} maxLength={3} placeholder="AGU" />
          <p className="text-xs text-slate-500">O clube começa com caixa de R$ 300,00 e sem jogadores.</p>
        </div>
        <ModalFooter>
          <Button variant="secondary" onClick={onClose}>Cancelar</Button>
          <Button type="submit" disabled={action.pending}>{action.pending ? 'Salvando…' : 'Criar clube'}</Button>
        </ModalFooter>
      </form>
    </Modal>
  );
}

export default function ClubsPage() {
  const [showCreate, setShowCreate] = useState(false);
  const [sort, setSort] = useState('name');
  const { refreshSummary } = useLeague();
  const clubs = useAsyncData(() => listClubs(sort === 'name' ? undefined : { sort }), [sort]);
  const columns = [
    { key: 'club', header: 'Clube', render: (club) => <Link to={`/clubs/${club.id}`} className="font-medium text-slate-900 hover:text-brand-700">{club.name}<span className="ml-2 text-xs text-slate-500">{club.acronym}</span></Link> },
    { key: 'cash', header: 'Caixa', align: 'right', render: (club) => `R$ ${formatMoney(club.cash)}` },
    { key: 'squad', header: 'Valor do elenco', align: 'right', render: (club) => `R$ ${formatMoney(club.squad_value)}` },
    { key: 'worth', header: 'Patrimônio', align: 'right', render: (club) => <span className="font-semibold text-slate-900">R$ {formatMoney(club.net_worth)}</span> },
    { key: 'players', header: 'Jogadores', align: 'right', render: (club) => club.player_count ?? '—' },
  ];

  return (
    <>
      <PageHeader icon={Shield} title="Clubes" description="Gerencie os clubes, seus caixas e patrimônios." action={<Button icon={Plus} onClick={() => setShowCreate(true)}>Novo clube</Button>} />
      <section className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
        <div className="flex flex-wrap items-end justify-between gap-3 px-5 py-4">
          <div><h2 className="font-semibold text-slate-900">Clubes da liga</h2><p className="mt-1 text-sm text-slate-500">{clubs.data?.length ?? 0} clubes cadastrados</p></div>
          <div className="w-full sm:w-56"><SelectField label="Ordenar por" name="sort" value={sort} onChange={(_, value) => setSort(value)} options={[{ value: 'name', label: 'Nome' }, { value: 'net_worth_desc', label: 'Patrimônio (maior)' }]} /></div>
        </div>
        <DataTable columns={columns} rows={clubs.data ?? []} getRowKey={(club) => club.id} loading={clubs.loading} error={clubs.error} emptyMessage="Nenhum clube cadastrado ainda." onRetry={clubs.reload} minWidthClass="min-w-[700px]" />
      </section>
      {showCreate && <NewClubModal onClose={() => setShowCreate(false)} onCreated={() => { setShowCreate(false); clubs.reload(); refreshSummary(); }} />}
    </>
  );
}
