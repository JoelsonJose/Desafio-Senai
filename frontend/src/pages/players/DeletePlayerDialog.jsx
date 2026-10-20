import ConfirmDialog from '../../components/ui/ConfirmDialog';
import { useToast } from '../../components/ui/ToastProvider';
import useAction from '../../hooks/useAction';
import { deletePlayer } from '../../services/playersService';

export default function DeletePlayerDialog({ player, onClose, onDeleted }) {
  const toast = useToast();
  const remove = useAction(() => deletePlayer(player.id));

  const handleConfirm = async () => {
    const result = await remove.run();
    if (result.ok) {
      toast.success('Jogador excluído', player.name);
      onDeleted();
    }
  };

  return (
    <ConfirmDialog
      title={`Excluir ${player.name}?`}
      description="O jogador deixa de aparecer nas listagens. O histórico de transações e de partidas é mantido."
      confirmLabel="Excluir jogador"
      pending={remove.pending}
      onConfirm={handleConfirm}
      onCancel={onClose}
    />
  );
}
