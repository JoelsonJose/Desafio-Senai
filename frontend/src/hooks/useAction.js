import { useCallback, useRef, useState } from 'react';
import { useToast } from '../components/ui/ToastProvider';
import { getErrorMessage } from '../services/http';

export default function useAction(action) {
  const toast = useToast();
  const [pending, setPending] = useState(false);
  const inFlight = useRef(false);

  const run = useCallback(
    async (...args) => {
      if (inFlight.current) {
        return { ok: false };
      }
      inFlight.current = true;
      setPending(true);
      try {
        const data = await action(...args);
        return { ok: true, data };
      } catch (error) {
        toast.error(getErrorMessage(error));
        return { ok: false };
      } finally {
        inFlight.current = false;
        setPending(false);
      }
    },
    [action, toast],
  );

  return { run, pending };
}
