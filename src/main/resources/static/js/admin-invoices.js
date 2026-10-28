(() => {
  const rows = document.getElementById('invoiceRows');
  const message = document.getElementById('invoiceMessage');
  const search = document.getElementById('invoiceSearch');
  const statusFilter = document.getElementById('paymentStatusFilter');
  const invoiceCount = document.getElementById('invoiceCount');
  const invoiceTotal = document.getElementById('invoiceTotal');
  let invoices = [];

  function formatMoney(value) {
    const amount = Number(value);
    return `Rs. ${Number.isFinite(amount) ? amount.toLocaleString(undefined, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }) : '0.00'}`;
  }

  function formatDate(value) {
    if (!value) return '—';
    const date = new Date(value);
    return Number.isNaN(date.getTime())
      ? String(value)
      : new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(date);
  }

  function normalize(value) {
    return String(value ?? '').toLocaleLowerCase();
  }

  function addCell(row, value, className) {
    const cell = document.createElement('td');
    if (className) cell.className = className;
    cell.textContent = value || '—';
    row.appendChild(cell);
    return cell;
  }

  function render() {
    const query = normalize(search.value.trim());
    const status = statusFilter.value;
    const filtered = invoices.filter((invoice) => {
      const matchesStatus = !status || invoice.paymentStatus === status;
      const searchable = [
        invoice.invoiceNumber,
        invoice.id,
        invoice.customerName,
        invoice.customerEmail,
        invoice.userId,
        invoice.paymentId,
        invoice.orderId,
        invoice.paymentType
      ].map(normalize).join(' ');
      return matchesStatus && searchable.includes(query);
    });

    rows.replaceChildren();
    filtered.forEach((invoice) => {
      const row = document.createElement('tr');

      const invoiceCell = addCell(row, invoice.invoiceNumber || `Invoice #${invoice.id}`, 'invoice-primary');
      const invoiceSubtext = document.createElement('span');
      invoiceSubtext.className = 'invoice-subtext';
      invoiceSubtext.textContent = `ID #${invoice.id ?? '—'}`;
      invoiceCell.appendChild(invoiceSubtext);

      const customerCell = addCell(row, invoice.customerName || `Customer #${invoice.userId ?? '—'}`, 'invoice-primary');
      if (invoice.customerEmail) {
        const email = document.createElement('span');
        email.className = 'invoice-subtext';
        email.textContent = invoice.customerEmail;
        customerCell.appendChild(email);
      }

      addCell(row, invoice.paymentId == null ? '—' : `#${invoice.paymentId}`);
      addCell(row, invoice.orderId == null ? '—' : `#${invoice.orderId}`);
      addCell(row, String(invoice.paymentType || '—').replace(/[_-]+/g, ' '));
      addCell(row, formatMoney(invoice.amount), 'invoice-amount');
      addCell(row, formatDate(invoice.invoiceDate));

      const statusCell = addCell(row, invoice.paymentStatus || '—');
      if (invoice.paymentStatus) {
        const badge = document.createElement('span');
        badge.className = 'invoice-status';
        badge.dataset.status = invoice.paymentStatus;
        badge.textContent = String(invoice.paymentStatus).replace(/[_-]+/g, ' ');
        statusCell.replaceChildren(badge);
      }
    });

    invoiceCount.textContent = String(filtered.length);
    const total = filtered.reduce((sum, invoice) => {
      const amount = Number(invoice.amount);
      return sum + (Number.isFinite(amount) ? amount : 0);
    }, 0);
    invoiceTotal.textContent = formatMoney(total);

    if (filtered.length === 0) {
      message.textContent = invoices.length
        ? 'No invoices match your search and status filters.'
        : 'No invoices have been generated yet.';
      message.classList.remove('error');
    } else {
      message.textContent = `Showing ${filtered.length} of ${invoices.length} invoices.`;
      message.classList.remove('error');
    }
  }

  async function loadInvoices() {
    message.textContent = 'Loading invoices…';
    message.classList.remove('error');
    rows.replaceChildren();
    try {
      const response = await fetch('/api/admin/invoices', {
        headers: { Accept: 'application/json' },
        credentials: 'same-origin'
      });
      if (response.status === 401 || response.status === 403) {
        const returnUrl = window.location.pathname + window.location.search;
        window.location.assign(`/loging.html?returnUrl=${encodeURIComponent(returnUrl)}`);
        return;
      }
      if (!response.ok) throw new Error(`Could not load invoices (error ${response.status}).`);

      const result = await response.json();
      if (!Array.isArray(result)) throw new Error('The invoices response was invalid.');
      invoices = result;
      render();
    } catch (error) {
      invoices = [];
      invoiceCount.textContent = '—';
      invoiceTotal.textContent = '—';
      message.textContent = error instanceof TypeError
        ? 'Unable to connect to the bakery service. Check the server and try again.'
        : error.message || 'Unable to load invoices.';
      message.classList.add('error');
    }
  }

  search.addEventListener('input', render);
  statusFilter.addEventListener('change', render);
  document.getElementById('refreshInvoices').addEventListener('click', loadInvoices);
  loadInvoices();
})();
