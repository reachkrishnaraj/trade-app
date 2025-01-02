import React from 'react';
import { Table, Badge } from 'react-bootstrap';
import { AccTrade } from 'app/shared/model/trade-app-module';

interface CurrentTradesTableProps {
  trades: AccTrade[];
}

const AccTradesTable: React.FC<CurrentTradesTableProps> = ({ trades }) => {
  const getStatusVariant = (status: AccTrade['status']) => {
    switch (status) {
      case 'ACTIVE':
        return 'success';
      case 'PENDING':
        return 'warning';
      default:
        return 'secondary';
    }
  };

  const getDirectionStyle = (direction: AccTrade['direction']) => ({
    color: direction === 'LONG' ? 'green' : 'red',
    fontWeight: 'bold' as const,
  });

  return (
    <Table responsive striped bordered hover>
      <thead>
        <tr>
          <th>Account Name</th>
          <th>Symbol</th>
          <th>Direction</th>
          <th>Status</th>
          <th>Opened</th>
          <th>Closed</th>
        </tr>
      </thead>
      <tbody>
        {trades.map((trade, index) => (
          <tr key={index}>
            <td>{trade.accountName}</td>
            <td>{trade.symbol}</td>
            <td style={getDirectionStyle(trade.direction)}>{trade.direction}</td>
            <td>
              <Badge bg={getStatusVariant(trade.status)}>{trade.status}</Badge>
            </td>
            <td>{trade.openedDatetime}</td>
            <td>{trade.closedDatetime || '-'}</td>
          </tr>
        ))}
        {trades.length === 0 && (
          <tr>
            <td colSpan={6} className="text-center">
              No active trades
            </td>
          </tr>
        )}
      </tbody>
    </Table>
  );
};

export default AccTradesTable;
