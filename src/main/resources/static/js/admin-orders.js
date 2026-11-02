(() => {
  const app = window.BakeryApp;
  const rows = document.getElementById('orderRows');
  const filter = document.getElementById('statusFilter');
  let orders = [];

  async function loadOrders() {
    app.clearError();
    try {
      const query = filter.value ? `?status=${encodeURIComponent(filter.value)}` : '';
      orders = await app.request(`/api/orders${query}`);
      if (!Array.isArray(orders)) throw new Error('The orders response was not a list.');
      render();
    } catch (error) {
      app.showError(error.message);
    }
  }

  function render() {
    rows.replaceChildren();
    orders.forEach((order) => {
      const row = document.createElement('tr');
      const values = [
        `#${order.id}`,
        window.BakeryPayment.date(order.createdAt),
        order.customerName || `Customer ${order.userId}`,
        order.itemsSummary || '—',
        app.money(order.totalAmount)
      ];
      values.forEach((value, index) => {
        const cell = document.createElement('td');
        cell.textContent = value;
        if (index === 4) cell.className = 'num';
        row.appendChild(cell);
      });
      const statusCell = document.createElement('td');
      const badge = document.createElement('span');
      window.BakeryPayment.setStatus(badge, order.status);
      statusCell.appendChild(badge);
      const actionCell = document.createElement('td');
      const next = { PENDING: 'PREPARING', PREPARING: 'READY', READY: 'DELIVERED' }[order.status];
      if (next) {
        const button = document.createElement('button');
        button.className = 'btn';
        button.type = 'button';
        button.textContent = `Mark ${window.BakeryPayment.status(next)}`;
        button.addEventListener('click', () => advanceOrder(order, next));
        actionCell.appendChild(button);
      } else {
        actionCell.textContent = 'Complete';
      }
      row.append(statusCell, actionCell);
      rows.appendChild(row);
    });
  }

  async function advanceOrder(order, status) {
    try {
      await app.request(`/api/orders/${encodeURIComponent(order.id)}/status`, {
        method: 'PUT',
        body: JSON.stringify({ status })
      });
      app.showToast(`Order #${order.id} updated.`);
      await loadOrders();
    } catch (error) {
      app.showError(error.message);
    }
  }

  filter.addEventListener('change', loadOrders);
  loadOrders();
})();
