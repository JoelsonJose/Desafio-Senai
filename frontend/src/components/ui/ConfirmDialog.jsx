import Button from './Button';
import Modal, { ModalFooter } from './Modal';

export default function ConfirmDialog({
  title,
  description,
  confirmLabel,
  confirmVariant = 'danger',
  pending = false,
  onConfirm,
  onCancel,
  children,
}) {
  return (
    <Modal title={title} description={description} onClose={onCancel} widthClass="max-w-md">
      {children}
      <ModalFooter>
        <Button variant="secondary" onClick={onCancel} disabled={pending}>
          Cancelar
        </Button>
        <Button variant={confirmVariant} onClick={onConfirm} disabled={pending}>
          {confirmLabel}
        </Button>
      </ModalFooter>
    </Modal>
  );
}
