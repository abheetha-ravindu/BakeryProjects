(() => {
  const statusColors = {
    PENDING: '#d99b37',
    PREPARING: '#8759aa',
    READY: '#458c62',
    DELIVERED: '#5482b9',
    CANCELLED: '#c95b64',
    CONFIRMED: '#438e9b',
    COMPLETED: '#63a77a',
    APPROVED: '#63a77a',
    REJECTED: '#c95b64'
  };
  const statusOrder = ['PENDING', 'PREPARING', 'READY', 'DELIVERED', 'CANCELLED', 'CONFIRMED', 'COMPLETED', 'APPROVED', 'REJECTED'];

  const byId = (id) => document.getElementById(id);
  const setText = (id, value) => {
    byId(id).textContent = value;
  };
  const arrayFrom = (value, label) => {
    if (!Array.isArray(value)) throw new Error(`${label} response was not a list.`);
    return value;
  };
  const fetchList = async (url, label) => {
    const response = await fetch(url, { headers: { Accept: 'application/json' } });
    if (!response.ok) {
      throw new Error(`${label} could not be loaded (HTTP ${response.status}).`);
    }
    return arrayFrom(await response.json(), label);
  };
  const normalizeStatus = (status) => String(status || 'UNKNOWN').trim().toUpperCase();
  const countStatuses = (records) => records.reduce((counts, record) => {
    const status = normalizeStatus(record.status);
    counts[status] = (counts[status] || 0) + 1;
    return counts;
  }, {});
  const formatMoney = (value) => {
    const amount = Number(value);
    if (!Number.isFinite(amount)) return 'Rs. 0.00';
    return `Rs. ${amount.toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };
  const formatDate = (value) => {
    const date = value ? new Date(value) : null;
    return date && !Number.isNaN(date.getTime())
      ? date.toLocaleDateString(undefined, { day: 'numeric', month: 'short', year: 'numeric' })
      : '—';
  };

  function showLoadErrors(errors) {
    if (!errors.length) return;
    const errorBox = byId('loadError');
    errorBox.textContent = `Some dashboard information could not be loaded: ${errors.join(' ')}`;
    errorBox.hidden = false;
  }

  function renderOrderChart(orders) {
    const svg = byId('ordersChart');
    const now = new Date();
    const days = Array.from({ length: 7 }, (_, index) => {
      const date = new Date(now);
      date.setHours(0, 0, 0, 0);
      date.setDate(date.getDate() - (6 - index));
      return date;
    });
    const counts = Array(7).fill(0);
    const positions = new Map(days.map((date, index) => [
      `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`,
      index
    ]));

    orders.forEach((order) => {
      const date = order.createdAt ? new Date(order.createdAt) : null;
      if (!date || Number.isNaN(date.getTime())) return;
      const index = positions.get(`${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`);
      if (index !== undefined) counts[index] += 1;
    });

    const width = 640;
    const height = 250;
    const left = 42;
    const right = 18;
    const top = 20;
    const bottom = 36;
    const chartWidth = width - left - right;
    const chartHeight = height - top - bottom;
    const maxCount = Math.max(4, Math.ceil(Math.max(...counts) / 4) * 4);
    const points = counts.map((count, index) => ({
      x: left + (chartWidth * index) / (counts.length - 1),
      y: top + chartHeight - (count / maxCount) * chartHeight,
      count
    }));
    const path = points.map((point, index) => `${index ? 'L' : 'M'} ${point.x} ${point.y}`).join(' ');
    const areaPath = `${path} L ${points[points.length - 1].x} ${top + chartHeight} L ${points[0].x} ${top + chartHeight} Z`;
    const gridLines = Array.from({ length: 5 }, (_, index) => {
      const value = Math.round((maxCount * index) / 4);
      const y = top + chartHeight - (chartHeight * index) / 4;
      return `<line class="chart-grid-line" x1="${left}" y1="${y}" x2="${width - right}" y2="${y}"></line>
        <text class="chart-axis-label" x="${left - 10}" y="${y + 4}" text-anchor="end">${value}</text>`;
    }).join('');
    const labels = days.map((date, index) => {
      const x = points[index].x;
      const label = date.toLocaleDateString(undefined, { weekday: 'short' });
      return `<text class="chart-axis-label" x="${x}" y="${height - 10}" text-anchor="middle">${label}</text>`;
    }).join('');
    const dots = points.map((point, index) => `<circle class="chart-dot" cx="${point.x}" cy="${point.y}" r="4">
        <title>${days[index].toLocaleDateString()}: ${point.count} orders</title>
      </circle>`).join('');

    svg.innerHTML = `<defs><linearGradient id="ordersAreaFill" x1="0" x2="0" y1="0" y2="1">
        <stop offset="0%" stop-color="#c92c3d" stop-opacity=".2"></stop>
        <stop offset="100%" stop-color="#c92c3d" stop-opacity="0"></stop>
      </linearGradient></defs>
      ${gridLines}
      <path class="chart-area" d="${areaPath}"></path>
      <path class="chart-line" d="${path}"></path>
      ${dots}
      ${labels}`;
    svg.setAttribute('aria-label', `Order activity for the last seven days: ${counts.join(', ')} orders from oldest to newest.`);
  }

  function renderStatusChart(orders) {
    const counts = countStatuses(orders);
    const entries = statusOrder
      .filter((status) => counts[status])
      .map((status) => [status, counts[status]]);
    Object.keys(counts)
      .filter((status) => !statusOrder.includes(status))
      .sort()
      .forEach((status) => entries.push([status, counts[status]]));

    const total = orders.length;
    setText('pieTotal', String(total));
    const pie = byId('orderStatusPie');
    const legend = byId('orderStatusLegend');
    if (!total) {
      pie.style.background = 'conic-gradient(#eadbce 0deg 360deg)';
      pie.setAttribute('aria-label', 'No bakery orders yet.');
      legend.replaceChildren();
      const empty = document.createElement('li');
      empty.className = 'legend-item';
      empty.textContent = 'No orders to show';
      legend.appendChild(empty);
      return;
    }

    let offset = 0;
    const segments = entries.map(([status, count], index) => {
      const end = offset + (count / total) * 360;
      const color = statusColors[status] || ['#d99b37', '#8759aa', '#458c62', '#5482b9'][index % 4];
      const segment = `${color} ${offset}deg ${end}deg`;
      offset = end;
      return segment;
    });
    pie.style.background = `conic-gradient(${segments.join(', ')})`;
    pie.setAttribute('aria-label', `Order status breakdown: ${entries.map(([status, count]) => `${status.toLowerCase()} ${count}`).join(', ')}.`);
    legend.replaceChildren();

    entries.forEach(([status, count], index) => {
      const item = document.createElement('li');
      item.className = 'legend-item';
      const swatch = document.createElement('span');
      swatch.className = 'legend-swatch';
      swatch.style.backgroundColor = statusColors[status] || ['#d99b37', '#8759aa', '#458c62', '#5482b9'][index % 4];
      const label = document.createElement('span');
      label.textContent = status.toLowerCase().replace(/^\w/, (letter) => letter.toUpperCase());
      const value = document.createElement('strong');
      value.className = 'legend-count';
      value.textContent = String(count);
      item.append(swatch, label, value);
      legend.appendChild(item);
    });
  }

  function renderRecentOrders(orders) {
    const rows = byId('recentOrderRows');
    rows.replaceChildren();
    const recent = [...orders]
      .sort((first, second) => new Date(second.createdAt || 0) - new Date(first.createdAt || 0))
      .slice(0, 5);
    byId('recentOrdersEmpty').hidden = recent.length > 0;
    recent.forEach((order) => {
      const row = document.createElement('tr');
      const values = [
        `#${order.id ?? '—'}`,
        formatDate(order.createdAt),
        order.customerName || (order.userId ? `Customer #${order.userId}` : '—'),
        formatMoney(order.totalAmount)
      ];
      values.forEach((value) => {
        const cell = document.createElement('td');
        cell.textContent = value;
        row.appendChild(cell);
      });
      const statusCell = document.createElement('td');
      const badge = document.createElement('span');
      const status = normalizeStatus(order.status);
      badge.className = 'status-tag';
      badge.dataset.status = status;
      badge.textContent = status.toLowerCase().replace(/^\w/, (letter) => letter.toUpperCase());
      statusCell.appendChild(badge);
      row.appendChild(statusCell);
      rows.appendChild(row);
    });
  }

  async function loadDashboard() {
    byId('todayLabel').textContent = new Date().toLocaleDateString(undefined, {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
    const requests = [
      ['orders', '/api/orders', 'Orders'],
      ['payments', '/api/payments', 'Payments'],
      ['bookings', '/api/cakeBookings', 'Custom bookings'],
      ['refunds', '/api/refund', 'Refund requests'],
      ['items', '/api/items', 'Catalog items'],
      ['customers', '/api/admin/users', 'Customers']
    ];
    const results = await Promise.all(requests.map(async ([key, url, label]) => {
      try {
        return [key, await fetchList(url, label), null];
      } catch (error) {
        return [key, null, error instanceof Error ? error.message : `${label} could not be loaded.`];
      }
    }));
    const data = {};
    const errors = [];
    results.forEach(([key, value, error]) => {
      if (error) errors.push(error);
      else data[key] = value;
    });
    showLoadErrors(errors);

    if (data.orders) {
      const counts = countStatuses(data.orders);
      setText('orderTotal', String(data.orders.length));
      setText('pendingOrders', `${counts.PENDING || 0} pending orders`);
      renderOrderChart(data.orders);
      renderStatusChart(data.orders);
      renderRecentOrders(data.orders);
    }
    if (data.payments) {
      const paid = data.payments
        .filter((payment) => normalizeStatus(payment.status) === 'PAID')
        .reduce((sum, payment) => sum + Number(payment.totalCharged ?? payment.amount ?? 0), 0);
      setText('paidRevenue', formatMoney(paid));
    }
    if (data.bookings) {
      const counts = countStatuses(data.bookings);
      setText('bookingTotal', String(data.bookings.length));
      setText('pendingBookings', `${counts.PENDING || 0} pending bookings`);
    }
    if (data.refunds) {
      const counts = countStatuses(data.refunds);
      setText('refundTotal', String(data.refunds.length));
      setText('pendingRefunds', `${counts.PENDING || 0} awaiting review`);
    }
    if (data.items) setText('itemTotal', String(data.items.length));
    if (data.customers) setText('customerTotal', String(data.customers.length));
  }

  loadDashboard();
})();
