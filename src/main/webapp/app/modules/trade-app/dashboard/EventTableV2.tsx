import React, { useState, useMemo } from 'react';
import { createColumnHelper, useReactTable, getSortedRowModel, getCoreRowModel, flexRender } from '@tanstack/react-table';
import { format, isWithinInterval, parseISO, startOfDay, endOfDay } from 'date-fns';
import DatePicker from 'react-datepicker';
import Select from 'react-select';
import { Event } from 'app/shared/model/trade-app-module';
import { Table, Card } from 'react-bootstrap';
import 'react-datepicker/dist/react-datepicker.css';

interface EventTableProps {
  data: Event[];
}

interface SelectOption {
  value: string;
  label: string;
}

const timePresets = [
  { label: '1H', hours: 1 },
  { label: '2H', hours: 2 },
  { label: '4H', hours: 4 },
  { label: '6H', hours: 6 },
  { label: '12H', hours: 12 },
  { label: '24H', hours: 24 },
];

const EventTableV2: React.FC<EventTableProps> = ({ data }) => {
  const columnHelper = createColumnHelper<Event>();

  // Filter states (selected values)
  const [selectedIndicators, setSelectedIndicators] = useState<SelectOption[]>([]);
  const [selectedCategories, setSelectedCategories] = useState<SelectOption[]>([]);
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [endDate, setEndDate] = useState<Date | null>(null);

  // Applied filter states (values used for actual filtering)
  const [appliedFilters, setAppliedFilters] = useState({
    indicators: [] as SelectOption[],
    categories: [] as SelectOption[],
    startDate: null as Date | null,
    endDate: null as Date | null,
  });

  // Extract unique indicators and categories
  const indicators = useMemo(() => {
    const uniqueIndicators = new Set(data.map(event => event.indicatorDisplayName));
    return Array.from(uniqueIndicators).map(name => ({
      value: name,
      label: name,
    }));
  }, [data]);

  const categories = useMemo(() => {
    const uniqueCategories = new Set(data.map(event => event.indicatorSubCategoryDisplayName));
    return Array.from(uniqueCategories).map(name => ({
      value: name,
      label: name,
    }));
  }, [data]);

  const handlePresetClick = (hours: number) => {
    const end = new Date();
    const start = new Date(end.getTime() - hours * 60 * 60 * 1000);
    setStartDate(start);
    setEndDate(end);
  };

  const handleStartDateChange = (date: Date | null) => {
    setStartDate(date);
    if (date && endDate && date > endDate) {
      setEndDate(date);
    }
  };

  const handleEndDateChange = (date: Date | null) => {
    setEndDate(date);
    if (date && startDate && date < startDate) {
      setStartDate(date);
    }
  };

  const resetFilters = () => {
    setSelectedIndicators([]);
    setSelectedCategories([]);
    setStartDate(null);
    setEndDate(null);
    setAppliedFilters({
      indicators: [],
      categories: [],
      startDate: null,
      endDate: null,
    });
  };

  const applyFilters = () => {
    setAppliedFilters({
      indicators: selectedIndicators,
      categories: selectedCategories,
      startDate,
      endDate,
    });
  };

  const filteredData = useMemo(() => {
    return data.filter(event => {
      // Check indicator and category filters
      const indicatorMatch =
        appliedFilters.indicators.length === 0 || appliedFilters.indicators.some(ind => ind.value === event.indicatorDisplayName);
      const categoryMatch =
        appliedFilters.categories.length === 0 ||
        appliedFilters.categories.some(cat => cat.value === event.indicatorSubCategoryDisplayName);

      // Check date range
      let dateMatch = true;
      if (appliedFilters.startDate || appliedFilters.endDate) {
        const eventDate = parseISO(event.created);
        if (appliedFilters.startDate && appliedFilters.endDate) {
          dateMatch = isWithinInterval(eventDate, {
            start: startOfDay(appliedFilters.startDate),
            end: endOfDay(appliedFilters.endDate),
          });
        } else if (appliedFilters.startDate) {
          dateMatch = eventDate >= startOfDay(appliedFilters.startDate);
        } else if (appliedFilters.endDate) {
          dateMatch = eventDate <= endOfDay(appliedFilters.endDate);
        }
      }

      return indicatorMatch && categoryMatch && dateMatch;
    });
  }, [data, appliedFilters]);

  const columns = [
    columnHelper.accessor('indicatorDisplayName', {
      header: () => <span>Indicator Name</span>,
    }),
    columnHelper.accessor('indicatorSubCategoryDisplayName', {
      header: () => <span>Category</span>,
    }),
    columnHelper.accessor('interval', {
      header: () => <span>Interval</span>,
    }),
    columnHelper.accessor('direction', {
      header: () => <span>Direction</span>,
    }),
    columnHelper.accessor('tradeSignalProcessStatus', {
      header: () => <span>Trade Status</span>,
    }),
    columnHelper.accessor('price', {
      header: () => <span>Price</span>,
    }),
    columnHelper.accessor('datetime', {
      header: () => <span>Event Time</span>,
      cell: info => format(new Date(info.getValue()), 'PPpp'),
    }),
    columnHelper.accessor('created', {
      header: () => <span>Created Time</span>,
      cell: info => format(new Date(info.getValue()), 'PPpp'),
    }),
    columnHelper.accessor('rawAlertMsg', {
      header: () => <span>Raw Message</span>,
      cell: info => (
        <span style={{ maxWidth: '300px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{info.getValue()}</span>
      ),
    }),
  ];

  const table = useReactTable({
    data: filteredData,
    columns,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    initialState: {
      sorting: [{ id: 'datetime', desc: false }],
    },
  });

  const getRowStyle = (direction: string | undefined) => {
    if (direction === 'BULL') {
      return { backgroundColor: '#d4edda' };
    }
    if (direction === 'BEAR') {
      return { backgroundColor: '#f8d7da' };
    }
    return { backgroundColor: 'white' };
  };

  return (
    <>
      <Card className="mb-3">
        <Card.Body>
          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Filter by Indicators</label>
              <Select
                isMulti
                options={indicators}
                value={selectedIndicators}
                onChange={newValue => setSelectedIndicators(newValue as SelectOption[])}
                placeholder="Select indicators..."
                className="basic-multi-select"
                classNamePrefix="select"
              />
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Filter by Categories</label>
              <Select
                isMulti
                options={categories}
                value={selectedCategories}
                onChange={newValue => setSelectedCategories(newValue as SelectOption[])}
                placeholder="Select categories..."
                className="basic-multi-select"
                classNamePrefix="select"
              />
            </div>
          </div>
          <div className="row">
            <div className="col-md-4 mb-3">
              <label className="form-label">From Date</label>
              <div>
                <DatePicker
                  selected={startDate}
                  onChange={handleStartDateChange}
                  selectsStart
                  startDate={startDate}
                  endDate={endDate}
                  maxDate={endDate || new Date()}
                  className="form-control"
                  placeholderText="Select start date"
                  showTimeSelect
                  dateFormat="MMMM d, yyyy h:mm aa"
                />
              </div>
            </div>
            <div className="col-md-4 mb-3">
              <label className="form-label">To Date</label>
              <div>
                <DatePicker
                  selected={endDate}
                  onChange={handleEndDateChange}
                  selectsEnd
                  startDate={startDate}
                  endDate={endDate}
                  minDate={startDate}
                  maxDate={new Date()}
                  className="form-control"
                  placeholderText="Select end date"
                  showTimeSelect
                  dateFormat="MMMM d, yyyy h:mm aa"
                />
              </div>
            </div>
            <div className="col-md-4 mb-3">
              <label className="form-label">&nbsp;</label>
              <div className="d-flex gap-2">
                <button className="btn btn-primary" onClick={applyFilters}>
                  Apply Filters
                </button>
                <button className="btn btn-secondary" onClick={resetFilters}>
                  Reset
                </button>
              </div>
            </div>
          </div>
          <div className="row">
            <div className="col-12 mb-3">
              <label className="form-label">Quick Ranges</label>
              <div className="d-flex gap-2">
                {timePresets.map(preset => (
                  <button key={preset.label} className="btn btn-outline-secondary btn-sm" onClick={() => handlePresetClick(preset.hours)}>
                    Last {preset.label}
                  </button>
                ))}
                <button
                  className="btn btn-outline-secondary btn-sm"
                  onClick={() => {
                    setStartDate(null);
                    setEndDate(null);
                  }}
                >
                  Clear Range
                </button>
              </div>
            </div>
          </div>
          <div className="row">
            <div className="col">
              <small className="text-muted">
                Showing {filteredData.length} of {data.length} events
              </small>
            </div>
          </div>
        </Card.Body>
      </Card>

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
          {filteredData.length === 0 && (
            <tr>
              <td colSpan={columns.length} className="text-center">
                No events found
              </td>
            </tr>
          )}
        </tbody>
      </Table>
    </>
  );
};

export default EventTableV2;
