import SummaryModal from '../../components/ui/SummaryModal';
import { formatDateTime, formatMoney } from '../../utils/format';

export default function PurchaseResultModal({ result, onClose }) {
  const rows = [
    { label: 'Jogador', value: result.player_name },
    { label: 'Valor de mercado', value: formatMoney(result.value) },
    { label: 'Taxa de compra', value: formatMoney(result.fee) },
    { label: 'Custo total (valor + taxa)', value: formatMoney(-result.cash_change), emphasis: true },
    { label: 'Fim da fidelidade', value: formatDateTime(result.contract.loyalty_until) },
    { label: 'Novo caixa do clube', value: formatMoney(result.club_cash), emphasis: true },
  ];

  return (
    <SummaryModal
      title="Compra realizada"
      description="O jogador entrou no elenco com contrato de fidelidade ativo."
      rows={rows}
      onClose={onClose}
    />
  );
}
