import Button from '../../components/ui/Button';
import { SelectField } from '../../components/ui/Field';
import Modal, { ModalFooter } from '../../components/ui/Modal';
import { useToast } from '../../components/ui/ToastProvider';
import { CONDITIONS } from '../../constants/players';
import useAction from '../../hooks/useAction';
import useForm from '../../hooks/useForm';
import { changePlayerCondition } from '../../services/playersService';

function validateCondition(values) {
  return values.condition ? {} : { condition: 'Escolha a condição física.' };
}

export default function PlayerConditionModal({ player, onClose, onSaved }) {
  const toast = useToast();
  const save = useAction((condition) => changePlayerCondition(player.id, condition));
  const form = useForm({
    initialValues: { condition: player.condition },
    validate: validateCondition,
    onSubmit: async (values) => {
      const result = await save.run(values.condition);
      if (result.ok) {
        toast.success('Condição atualizada', result.data.name);
        onSaved();
      }
    },
  });

  const options = CONDITIONS.map((option) => ({
    ...option,
    disabled: player.condition === 'DOENTE' && option.value === 'APTO',
  }));
  const warnsAboutLineup = form.values.condition === 'DOENTE' && Boolean(player.club_id);

  let hint;
  if (player.condition === 'DOENTE') {
    hint = 'Um jogador doente passa por mal condicionado antes de voltar a apto.';
  } else if (warnsAboutLineup) {
    hint = 'Se for titular, ele sai da escalação e o clube só joga depois de escalar de novo.';
  }

  return (
    <Modal
      title={`Condição de ${player.name}`}
      description="O valor de mercado é recalculado pela API com a nova condição."
      onClose={onClose}
      widthClass="max-w-md"
    >
      <form onSubmit={form.handleSubmit} noValidate>
        <SelectField
          label="Condição física"
          name="condition"
          value={form.values.condition}
          onChange={form.setValue}
          options={options}
          error={form.errors.condition}
          hint={hint}
        />
        <ModalFooter>
          <Button variant="secondary" onClick={onClose} disabled={save.pending}>
            Cancelar
          </Button>
          <Button type="submit" disabled={save.pending}>
            Salvar condição
          </Button>
        </ModalFooter>
      </form>
    </Modal>
  );
}
