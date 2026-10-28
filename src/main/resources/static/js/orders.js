(() => {
  const app = window.BakeryApp;
  const list = document.getElementById('orderList');
  const confirm = document.getElementById('confirm');
  const confirmText = document.getElementById('confirmText');

  const addText = (parent, tag, className, value) => {
    const element = document.createElement(tag);
    if (className) element.className = className;
    element.textContent = value || '—';
    parent.appendChild(element);
    return element;
  };

  async function loadOrders() {
    list.replaceChildren();
    app.clearError();
    try {
      const orders = await app.request(`/api/orders/user/${encodeURIComponent(app.getUserId())}`);
      if (!Array.isArray(orders)) throw new Error('The orders response was not a list.');
      if (!orders.length) {
        addText(list, 'p', 'muted', 'You do not have any orders yet.');
        return;
      }
      orders.forEach(renderOrder);
    } catch (error) {
      app.showError(error.message);
    }
  }

  function renderOrder(order) {
    const card = document.createElement('article');
    card.className = 'order-card';

    const header = document.createElement('div');
    header.className = 'order-card-header';
    const title = addText(header, 'h2', '', `Order #${order.id}`);
    title.setAttribute('aria-label', `Order number ${order.id}`);
    const badge = document.createElement('span');
    window.BakeryPayment.setStatus(badge, order.status);
    header.appendChild(badge);
    card.appendChild(header);

    addText(card, 'p', '', order.itemsSummary);
    addText(card, 'p', 'muted', `Placed ${window.BakeryPayment.date(order.createdAt)} · Total ${app.money(order.totalAmount)}`);
    if (order.customerName) addText(card, 'p', 'muted', `For ${order.customerName}`);
    if (order.note) addText(card, 'p', 'muted', `Note: ${order.note}`);

    const actions = document.createElement('div');
    actions.className = 'order-actions';
    if (order.status === 'PENDING') {
      const noteButton = document.createElement('button');
      noteButton.className = 'btn';
      noteButton.type = 'button';
      noteButton.textContent = 'Edit note';
      noteButton.addEventListener('click', () => editNote(order));
      actions.appendChild(noteButton);

      const cancelButton = document.createElement('button');
      cancelButton.className = 'btn';
      cancelButton.type = 'button';
      cancelButton.textContent = 'Cancel order';
      cancelButton.addEventListener('click', () => cancelOrder(order));
      actions.appendChild(cancelButton);
    }
    if (order.status === 'PENDING' || order.status === 'PREPARING' || order.status === 'READY') {
      const payLink = document.createElement('a');
      payLink.className = 'btn btn-primary';
      payLink.href = `payment.html?orderId=${encodeURIComponent(order.id)}`;
      payLink.textContent = 'Pay for order';
      actions.appendChild(payLink);
    }
    card.appendChild(actions);
    list.appendChild(card);
  }

  async function editNote(order) {
    const note = window.prompt('Update the note for this order (up to 200 characters):', order.note || '');
    if (note === null) return;
    try {
      await app.request(`/api/orders/${encodeURIComponent(order.id)}`, {
        method: 'PUT',
        body: JSON.stringify({ note })
      });
      app.showToast('Order note updated.');
      await loadOrders();
    } catch (error) {
      app.showError(error.message);
    }
  }

  async function cancelOrder(order) {
    if (!window.confirm(`Cancel order #${order.id}?`)) return;
    try {
      await app.request(`/api/orders/${encodeURIComponent(order.id)}`, { method: 'DELETE' });
      app.showToast('Order cancelled.');
      await loadOrders();
    } catch (error) {
      app.showError(error.message);
    }
  }

  const orderId = new URLSearchParams(window.location.search).get('orderId');
  if (orderId) {
    confirm.hidden = false;
    confirmText.textContent = `Your order reference is #${orderId}.`;
  }
  loadOrders();
})();
