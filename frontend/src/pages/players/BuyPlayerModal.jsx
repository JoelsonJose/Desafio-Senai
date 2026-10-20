import { ShoppingCart } from 'lucide-react';
import Avatar from '../../components/ui/Avatar';
import Button from '../../components/ui/Button';
import { TextField, SelectField } from '../../components/ui/Field';
import Modal, { ModalFooter } from '../../components/ui/Modal';
import { POSITIONS, labelOf } from '../../constants/players';
import useAction from '../../hooks/useAction';
import useForm from '../../hooks/useForm';
import { buyPlayer } from '../../services/marketService';
import { formatMoney, toApiDateTime } from '../../utils/format';

function validatePurchase(values) {
  const errors = {};
  if (!values.clubId) {
    errors.clubId = 'Escolha o clube comprador.';
  }
  if (!values.date) {
    errors.date = 'Informe a data.';
  }
  if (!values.time) {
    errors.time = 'Informe a hora.';
  }
  return errors;
}

export default function BuyPlayerModal({ player, clubs, onClose, onBought }) {
  const buy = useAction((payload) => buyPlayer(player.id, payload));
  const form = useForm({
    initialValues: { clubId: '', date: '', time: '' },
    validate: validatePurchase,
    onSubmit: async (values) => {
      const result = await buy.run({
        club_id: Number(values.clubId),
        loyalty_until: toApiDateTime(values.date, values.time),
      });
      if (result.ok) {
        onBought(result.data);
      }
    },
  });
  const { values, errors, setValue } = form;

  const clubOptions = clubs.map((club) => ({
    value: String(club.id),
    label: `${club.name} (${club.acronym}) · caixa ${formatMoney(club.cash)}`,
  }));
  const selectedClub = clubs.find((club) => String(club.id) === values.clubId);
  const clubHint = selectedClub
    ? `Caixa atual: ${formatMoney(selectedClub.cash)}. O custo total (valor + taxa) pode ir até 50% do caixa.`
    : 'O custo total (valor + taxa) pode ir até 50% do caixa do clube.';

  return (
    <Modal title="Comprar jogador" description="Escolha o clube comprador e até quando vai a fidelidade do contrato." onClose={onClose}>
      <form onSubmit={form.handleSubmit} noValidate className="space-y-4">
        <div className="flex items-center justify-between gap-4 rounded-xl border border-slate-200 bg-slate-50 p-4">
          <div className="flex items-center gap-3">
            <Avatar name={player.name} />
            <div>
              <p className="font-semibold text-slate-900">{player.name}</p>
              <p className="text-xs text-slate-500">
                {player.position} · {labelOf(POSITIONS, player.position)} · {player.age} anos
              </p>
            </div>
          </div>
          <div className="text-right">
            <p className="text-xs text-slate-500">Valor de mercado</p>
            <p className="text-lg font-semibold text-slate-900">{formatMoney(player.market_value)}</p>
          </div>
        </div>

        <SelectField
          label="Clube comprador"
          name="clubId"
          value={values.clubId}
          onChange={setValue}
          options={clubOptions}
          placeholder="Selecione o clube"
          error={errors.clubId}
          hint={clubHint}
        />

        <div>
          <div className="grid grid-cols-[1fr_9rem] gap-3">
            <TextField label="Fim da fidelidade" name="date" type="date" value={values.date} onChange={setValue} error={errors.date} />
            <TextField label="Hora" name="time" type="time" value={values.time} onChange={setValue} error={errors.time} />
          </div>
          <p className="mt-1 text-xs text-slate-500">
            Depois do momento da compra e até 1.095 dias. Horário de Brasília; enviado com o fuso (-03:00).
          </p>
        </div>

        <p className="rounded-xl border border-slate-200 bg-slate-50 p-3 text-xs text-slate-600">
          A taxa é de 5% do valor de mercado, só quando ele passa de 30,00. Taxa, custo e novo caixa são calculados ao
          confirmar.
        </p>

        <ModalFooter>
          <Button variant="secondary" onClick={onClose} disabled={buy.pending}>
            Cancelar
          </Button>
          <Button type="submit" icon={ShoppingCart} disabled={buy.pending}>
            Confirmar compra
          </Button>
        </ModalFooter>
      </form>
    </Modal>
  );
}
