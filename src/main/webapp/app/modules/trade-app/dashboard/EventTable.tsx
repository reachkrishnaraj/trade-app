import React, { useState } from 'react';
import { Table } from 'react-bootstrap';
import { format } from 'date-fns';
import { Event } from 'app/shared/model/trade-app-module';

interface EventTableProps {
  data: Event[];
}

const EventTable: React.FC<EventTableProps> = ({ data }) => {
  // State for sorting
  const [sortConfig, setSortConfig] = useState<{ key: string; direction: 'asc' | 'desc' | null }>({
    key: 'datetime',
    direction: null,
  });

  // Function to format date into a human-readable string
  const getHumanReadableDate = (isoString: string): string => {
    return format(new Date(isoString), 'PPpp'); // Formats to "Jan 1, 2025, 12:45:54 PM"
  };

  // Function to handle sorting
  const handleSort = (key: string) => {
    let direction: 'asc' | 'desc' | null = 'asc';
    if (sortConfig.key === key && sortConfig.direction === 'asc') {
      direction = 'desc';
    }
    setSortConfig({ key, direction });
  };

  // Apply sorting to the data
  const sortedData = React.useMemo(() => {
    if (!sortConfig.key || sortConfig.direction === null) {
      return data; // No sorting applied
    }
    return [...data].sort((a, b) => {
      const aValue = a[sortConfig.key as keyof Event] as string;
      const bValue = b[sortConfig.key as keyof Event] as string;

      if (sortConfig.direction === 'asc') {
        return new Date(aValue).getTime() - new Date(bValue).getTime();
      } else {
        return new Date(bValue).getTime() - new Date(aValue).getTime();
      }
    });
  }, [data, sortConfig]);

  return (
    <Table responsive striped bordered hover>
      <thead>
        <tr>
          <th>Indicator Name</th>
          <th>Category</th>
          <th>Interval</th>
          <th>Direction</th>
          <th>Price</th>
          <th>Symbol</th>
          <th onClick={() => handleSort('datetime')} style={{ cursor: 'pointer' }}>
            Event Time {sortConfig.key === 'datetime' && (sortConfig.direction === 'asc' ? '▲' : '▼')}
          </th>
          <th>Relative Time</th>
          <th>Raw Message</th>
        </tr>
      </thead>
      <tbody>
        {sortedData.map((event, index) => (
          <tr key={index}>
            <td>{event.indicator}</td>
            <td>{event.category}</td>
            <td>{event.interval}</td>
            <td>{event.direction}</td>
            <td>{event.price}</td>
            <td>{event.symbol}</td>
            <td>{getHumanReadableDate(event.datetime)}</td>
            <td>{event.sinceCreatedStr}</td>
            <td style={{ maxWidth: '300px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{event.rawMsg}</td>
          </tr>
        ))}
        {data.length === 0 && (
          <tr>
            <td colSpan={9} className="text-center">
              No events found
            </td>
          </tr>
        )}
      </tbody>
    </Table>
  );
};

export default EventTable;
