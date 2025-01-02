import React from 'react';
import { createColumnHelper, useReactTable, getSortedRowModel, getCoreRowModel, flexRender } from '@tanstack/react-table';
import { format } from 'date-fns';
import { Event } from 'app/shared/model/trade-app-module';
import { Table } from 'react-bootstrap';

interface EventTableProps {
  data: Event[];
}

const EventTableV2: React.FC<EventTableProps> = ({ data }) => {
  const columnHelper = createColumnHelper<Event>();

  const columns = [
    columnHelper.accessor('indicator', {
      header: () => <span>Indicator Name</span>,
    }),
    columnHelper.accessor('category', {
      header: () => <span>Category</span>,
    }),
    columnHelper.accessor('interval', {
      header: () => <span>Interval</span>,
    }),
    columnHelper.accessor('direction', {
      header: () => <span>Direction</span>,
    }),
    columnHelper.accessor('tradeAction', {
      header: () => <span>Trade Action</span>,
    }),
    columnHelper.accessor('price', {
      header: () => <span>Price</span>,
    }),
    columnHelper.accessor('datetime', {
      header: () => <span>Event Time</span>,
      cell: info => format(new Date(info.getValue()), 'PPpp'),
    }),
    columnHelper.accessor('sinceCreatedStr', {
      header: () => <span>Relative Time</span>,
    }),
    columnHelper.accessor('rawMsg', {
      header: () => <span>Raw Message</span>,
      cell: info => (
        <span style={{ maxWidth: '300px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{info.getValue()}</span>
      ),
    }),
  ];

  const table = useReactTable({
    data,
    columns,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    initialState: {
      sorting: [{ id: 'datetime', desc: false }], // Default sorting
    },
  });

  const getRowStyle = (direction: string | undefined) => {
    if (direction === 'BULL') {
      return { backgroundColor: '#d4edda' }; // Light green for bullish
    }
    if (direction === 'BEAR') {
      return { backgroundColor: '#f8d7da' }; // Light red for bearish
    }
    return { backgroundColor: 'white' }; // Default color
  };

  return (
    <Table responsive striped bordered hover>
      <thead>
        {table.getHeaderGroups().map(headerGroup => (
          <tr key={headerGroup.id}>
            {headerGroup.headers.map(header => (
              <th key={header.id} onClick={header.column.getToggleSortingHandler()} style={{ cursor: 'pointer' }}>
                {header.isPlaceholder ? null : flexRender(header.column.columnDef.header, header.getContext())}
                <span>{header.column.getIsSorted() ? (header.column.getIsSorted() === 'desc' ? ' 🔽' : ' 🔼') : ''}</span>
              </th>
            ))}
          </tr>
        ))}
      </thead>
      <tbody>
        {table.getRowModel().rows.map(row => (
          <tr key={row.id} style={getRowStyle(row.original.direction)}>
            {row.getVisibleCells().map(cell => (
              <td key={cell.id}>{flexRender(cell.column.columnDef.cell, cell.getContext())}</td>
            ))}
          </tr>
        ))}
        {data.length === 0 && (
          <tr>
            <td colSpan={columns.length} className="text-center">
              No events found
            </td>
          </tr>
        )}
      </tbody>
    </Table>
  );
};

export default EventTableV2;
