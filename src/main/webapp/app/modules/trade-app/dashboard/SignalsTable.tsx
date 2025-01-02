import React from 'react';
import { Table, Badge } from 'react-bootstrap';
import { TradeSignal } from 'app/shared/model/trade-app-module';

interface SignalsTableProps {
  signals: TradeSignal[];
}

const SignalsTable: React.FC<SignalsTableProps> = ({ signals }) => {
  const getSignalBadge = (signal: string) => {
    const signalLower = signal.toLowerCase();
    let variant = 'secondary';

    if (signalLower.includes('buy') || signalLower.includes('long')) {
      variant = 'success';
    } else if (signalLower.includes('sell') || signalLower.includes('short')) {
      variant = 'danger';
    }

    return <Badge bg={variant}>{signal}</Badge>;
  };

  return (
    <Table responsive striped bordered hover>
      <thead>
        <tr>
          <th>Strategy Name</th>
          <th>Signal</th>
          <th>DateTime</th>
          <th>Symbol</th>
        </tr>
      </thead>
      <tbody>
        {signals.map((signal, index) => (
          <tr key={index}>
            <td>{signal.strategy}</td>
            <td>{getSignalBadge(signal.signal)}</td>
            <td>{signal.createdTs}</td>
            <td>{signal.symbol}</td>
          </tr>
        ))}
        {signals.length === 0 && (
          <tr>
            <td colSpan={4} className="text-center">
              No signals found
            </td>
          </tr>
        )}
      </tbody>
    </Table>
  );
};

export default SignalsTable;
