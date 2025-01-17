import React, { useState, useMemo } from 'react';
import { createColumnHelper, useReactTable, getSortedRowModel, getCoreRowModel, flexRender, PaginationState } from '@tanstack/react-table';
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

const ITEMS_PER_PAGE = 50;

const candleTypes = [
  { value: 'CLASSIC', label: 'Classic' },
  { value: 'HEIKIN_ASHI', label: 'Heiken Ashi' },
  { value: 'RENKO_8B', label: 'Renko 8B' },
  { value: 'RENKO_5B', label: 'Renko 5B' },
  { value: 'RENKO_4B', label: 'Renko 4B' },
  { value: 'RENKO_2B', label: 'Renko 2B' },
  { value: 'RENKO_1B', label: 'Renko 1B' },
];

const EventTableV2: React.FC<EventTableProps> = ({ data }) => {
  const columnHelper = createColumnHelper<Event>();

  // Filter states (selected values)
  const [selectedIndicators, setSelectedIndicators] = useState<SelectOption[]>([]);
  const [selectedCategories, setSelectedCategories] = useState<SelectOption[]>([]);
  const [selectedCandleTypes, setSelectedCandleTypes] = useState<SelectOption[]>([]);
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [endDate, setEndDate] = useState<Date | null>(null);
  const [currentPage, setCurrentPage] = useState(0);

  // Applied filter states (values used for actual filtering)
  const [appliedFilters, setAppliedFilters] = useState({
    indicators: [] as SelectOption[],
    categories: [] as SelectOption[],
    candleTypes: [] as SelectOption[],
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
    setSelectedCandleTypes([]);
    setStartDate(null);
    setEndDate(null);
    setCurrentPage(0);
    setAppliedFilters({
      indicators: [],
      categories: [],
      candleTypes: [],
      startDate: null,
      endDate: null,
    });
  };

  const applyFilters = () => {
    setCurrentPage(0);
    setAppliedFilters({
      indicators: selectedIndicators,
      categories: selectedCategories,
      candleTypes: selectedCandleTypes,
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
      const candleTypeMatch =
        appliedFilters.candleTypes.length === 0 || appliedFilters.candleTypes.some(type => type.value === event.candleType);

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

      return indicatorMatch && categoryMatch && candleTypeMatch && dateMatch;
    });
  }, [data, appliedFilters, startDate, endDate]);

  const paginatedData = useMemo(() => {
    const startIndex = currentPage * ITEMS_PER_PAGE;
    return filteredData.slice(startIndex, startIndex + ITEMS_PER_PAGE);
  }, [filteredData, currentPage]);

  const pageCount = Math.ceil(filteredData.length / ITEMS_PER_PAGE);

  const columns = [
    columnHelper.accessor('indicatorDisplayName', {
      header: () => <span>Indicator Name</span>,
    }),
    columnHelper.accessor('indicatorSubCategoryDisplayName', {
      header: () => <span>Category</span>,
    }),
    columnHelper.accessor('candleType', {
      header: () => <span>Candle Type</span>,
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
    data: paginatedData,
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

  const renderPagination = () => {
    const pages = [];
    const maxVisiblePages = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(pageCount - 1, startPage + maxVisiblePages - 1);

    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(
        <button
          key={i}
          className={`btn btn-sm ${currentPage === i ? 'btn-primary' : 'btn-outline-primary'}`}
          onClick={() => setCurrentPage(i)}
        >
          {i + 1}
        </button>,
      );
    }

    return (
      <div className="d-flex justify-content-center gap-2 mt-3">
        <button
          className="btn btn-sm btn-outline-primary"
          onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
          disabled={currentPage === 0}
        >
          Previous
        </button>
        {startPage > 0 && (
          <>
            <button className="btn btn-sm btn-outline-primary" onClick={() => setCurrentPage(0)}>
              1
            </button>
            {startPage > 1 && <span className="align-self-center">...</span>}
          </>
        )}
        {pages}
        {endPage < pageCount - 1 && (
          <>
            {endPage < pageCount - 2 && <span className="align-self-center">...</span>}
            <button className="btn btn-sm btn-outline-primary" onClick={() => setCurrentPage(pageCount - 1)}>
              {pageCount}
            </button>
          </>
        )}
        <button
          className="btn btn-sm btn-outline-primary"
          onClick={() => setCurrentPage(p => Math.min(pageCount - 1, p + 1))}
          disabled={currentPage === pageCount - 1}
        >
          Next
        </button>
      </div>
    );
  };

  return (
    <>
      <Card className="mb-3">
        <Card.Body>
          <div className="row">
            <div className="col-md-4 mb-3">
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
            <div className="col-md-4 mb-3">
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
            <div className="col-md-4 mb-3">
              <label className="form-label">Filter by Candle Type</label>
              <Select
                isMulti
                options={candleTypes}
                value={selectedCandleTypes}
                onChange={newValue => setSelectedCandleTypes(newValue as SelectOption[])}
                placeholder="Select candle types..."
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
                Showing {paginatedData.length} of {filteredData.length} events (page {currentPage + 1} of {pageCount})
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
          {paginatedData.length === 0 && (
            <tr>
              <td colSpan={columns.length} className="text-center">
                No events found
              </td>
            </tr>
          )}
        </tbody>
      </Table>
      {renderPagination()}
    </>
  );
};

export default EventTableV2;
