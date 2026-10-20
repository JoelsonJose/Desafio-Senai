import Button from '../../components/ui/Button';
import { SelectField, TextField } from '../../components/ui/Field';
import Modal, { ModalFooter } from '../../components/ui/Modal';
import { useToast } from '../../components/ui/ToastProvider';
import { ATTRIBUTES, POSITIONS } from '../../constants/players';
import useAction from '../../hooks/useAction';
import useForm from '../../hooks/useForm';
import { createPlayer, updatePlayer } from '../../services/playersService';
import { normalizeText } from '../../utils/text';

const INTEGER_PATTERN = /^\d+$/;

function validateInteger(errors, values, key, min, max, subject) {
  const text = String(values[key]).trim();
  if (text === '') {
    errors[key] = 'Campo obrigatório.';
  } else if (!INTEGER_PATTERN.test(text) || Number(text) < min || Number(text) > max) {
    errors[key] = `${subject} deve ser um número inteiro de ${min} a ${max}.`;
  }
}

function validatePlayer(values, isEdit) {
  const errors = {};
  const name = normalizeText(values.name);
  if (!name) {
    errors.name = 'Informe o nome do jogador.';
  } else if (name.length < 3 || name.length > 100) {
    errors.name = 'O nome deve ter de 3 a 100 caracteres.';
  }
  if (!values.position) {
    errors.position = 'Escolha a posição.';
  }
  validateInteger(errors, values, 'age', 16, 45, 'A idade');
  validateInteger(errors, values, 'agility', 1, 100, 'A agilidade');
  if (!isEdit) {
    ATTRIBUTES.forEach((attribute) => {
      validateInteger(errors, values, attribute.key, 1, 99, `O atributo ${attribute.label.toLowerCase()}`);
    });
    validateInteger(errors, values, 'potential', 1, 99, 'O potencial');
  }
  return errors;
}

function buildInitialValues(player) {
  if (player) {
    return {
      name: player.name,
      position: player.position,
      age: String(player.age),
      agility: String(player.agility),
    };
  }
  const blankAttributes = Object.fromEntries(ATTRIBUTES.map((attribute) => [attribute.key, '']));
  return { name: '', position: '', age: '', agility: '', ...blankAttributes, potential: '' };
}

function buildPayload(values, isEdit) {
  const payload = {
    name: normalizeText(values.name),
    position: values.position,
    age: Number(values.age),
    agility: Number(values.agility),
  };
  if (!isEdit) {
    ATTRIBUTES.forEach((attribute) => {
      payload[attribute.key] = Number(values[attribute.key]);
    });
    payload.potential = Number(values.potential);
  }
  return payload;
}

export default function PlayerFormModal({ player, onClose, onSaved }) {
  const isEdit = Boolean(player);
  const toast = useToast();
  const save = useAction((payload) => (isEdit ? updatePlayer(player.id, payload) : createPlayer(payload)));
  const form = useForm({
    initialValues: buildInitialValues(player),
    validate: (values) => validatePlayer(values, isEdit),
    onSubmit: async (values) => {
      const result = await save.run(buildPayload(values, isEdit));
      if (result.ok) {
        toast.success(isEdit ? 'Jogador atualizado' : 'Jogador cadastrado', result.data.name);
        onSaved();
      }
    },
  });
  const { values, errors, setValue } = form;
  const hasClub = Boolean(player?.club_id);

  return (
    <Modal
      title={isEdit ? 'Editar jogador' : 'Novo jogador'}
      description={
        isEdit
          ? 'Altere nome, posição, agilidade e idade. Os indicadores não mudam depois do cadastro.'
          : 'O valor base e o overall são calculados pela API depois de salvar.'
      }
      onClose={onClose}
    >
      <form onSubmit={form.handleSubmit} noValidate className="space-y-4">
        <TextField label="Nome" name="name" value={values.name} onChange={setValue} error={errors.name} maxLength={100} />
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <SelectField
            label="Posição"
            name="position"
            value={values.position}
            onChange={setValue}
            options={POSITIONS}
            placeholder="Selecione"
            error={errors.position}
            disabled={hasClub}
            hint={hasClub ? 'Jogador com clube não muda de posição.' : undefined}
          />
          <TextField
            label="Idade"
            name="age"
            type="number"
            value={values.age}
            onChange={setValue}
            error={errors.age}
            hint={isEdit ? `Não pode ser menor que ${player.age}.` : '16 a 45 anos.'}
            min={isEdit ? player.age : 16}
            max={45}
          />
          <TextField
            label="Agilidade"
            name="agility"
            type="number"
            value={values.agility}
            onChange={setValue}
            error={errors.agility}
            hint="1 a 100."
            min={1}
            max={100}
          />
        </div>

        {!isEdit && (
          <>
            <div className="grid grid-cols-2 gap-4 border-t border-slate-100 pt-4 sm:grid-cols-3">
              {ATTRIBUTES.map((attribute) => (
                <TextField
                  key={attribute.key}
                  label={attribute.label}
                  name={attribute.key}
                  type="number"
                  value={values[attribute.key]}
                  onChange={setValue}
                  error={errors[attribute.key]}
                  min={1}
                  max={99}
                />
              ))}
            </div>
            <TextField
              label="Potencial"
              name="potential"
              type="number"
              value={values.potential}
              onChange={setValue}
              error={errors.potential}
              hint="1 a 99, nunca menor que o maior atributo."
              min={1}
              max={99}
            />
          </>
        )}

        <ModalFooter>
          <Button variant="secondary" onClick={onClose} disabled={save.pending}>
            Cancelar
          </Button>
          <Button type="submit" disabled={save.pending}>
            {isEdit ? 'Salvar alterações' : 'Salvar jogador'}
          </Button>
        </ModalFooter>
      </form>
    </Modal>
  );
}
