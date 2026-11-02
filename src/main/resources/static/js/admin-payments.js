(() => {
  const app = window.BakeryApp;
  const rows = document.getElementById('paymentRows');
  const filter = document.getElementById('statusFilter');
  const orderSearch = document.getElementById('orderSearch');
  const clearButton = document.getElementById('clearBtn');
  let payments = [];

  async function loadPayments() {
    app.clearError();
    try {
      const params = new URLSearchParams();
      if (filter.value) params.set('status', filter.value);
      const orderId = orderSearch.value.trim();
      const endpoint = orderId
        ? `/api/payments/order/${encodeURIComponent(orderId)}`
        : `/api/payments${params.size ? `?${params}` : ''}`;
      payments = await app.request(endpoint);
      if (!Array.isArray(payments)) throw new Error('The payments response was not a list.');
      clearButton.hidden = !orderId;
      render();
    } catch (error) {
      app.showError(error.message);
    }
  }

  function render() {
    rows.replaceChildren();
    payments.forEach((payment) => {
      if (filter.value && payment.status !== filter.value) return;
      const row = document.createElement('tr');
      const values = [
        `#${payment.id}`,
        window.BakeryPayment.date(payment.createdAt),
        `#${payment.orderId}`,
        payment.paymentType || '—',
        app.money(payment.amount),
        app.money(payment.fee),
        app.money(payment.totalCharged)
      ];
      values.forEach((value, index) => {
        const cell = document.createElement('td');
        cell.textContent = value;
        if (index >= 4) cell.className = 'num';
        row.appendChild(cell);
      });
      const statusCell = document.createElement('td');
      const badge = document.createElement('span');
      window.BakeryPayment.setStatus(badge, payment.status);
      statusCell.appendChild(badge);
      const actionCell = document.createElement('td');
      if (payment.status === 'PAID') {
        const refund = document.createElement('button');
        refund.className = 'btn';
        refund.type = 'button';
        refund.textContent = 'Refund';
        refund.addEventListener('click', () => updatePayment(payment, 'REFUNDED'));
        actionCell.appendChild(refund);
      } else {
        const remove = document.createElement('button');
        remove.className = 'btn';
        remove.type = 'button';
        remove.textContent = 'Delete';
        remove.addEventListener('click', () => deletePayment(payment));
        actionCell.appendChild(remove);
      }
      row.append(statusCell, actionCell);
      rows.appendChild(row);
    });
  }

  async function updatePayment(payment, status) {
    try {
      await app.request(`/api/payments/${encodeURIComponent(payment.id)}/status`, {
        method: 'PUT',
        body: JSON.stringify({ status })
      });
      app.showToast(`Payment #${payment.id} refunded.`);
      await loadPayments();
    } catch (error) {
      app.showError(error.message);
    }
  }

  async function deletePayment(payment) {
    if (!window.confirm(`Delete refunded payment #${payment.id}?`)) return;
    try {
      await app.request(`/api/payments/${encodeURIComponent(payment.id)}`, { method: 'DELETE' });
      app.showToast(`Payment #${payment.id} deleted.`);
      await loadPayments();
    } catch (error) {
      app.showError(error.message);
    }
  }

  document.getElementById('findBtn').addEventListener('click', loadPayments);
  clearButton.addEventListener('click', () => {
    orderSearch.value = '';
    loadPayments();
  });
  filter.addEventListener('change', loadPayments);
  loadPayments();
})();
