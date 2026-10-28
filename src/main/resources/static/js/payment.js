(() => {
  const app = window.BakeryApp;
  const choices = document.getElementById('orderChoices');
  const cardFields = document.getElementById('cardFields');
  const payButton = document.getElementById('payBtn');
  const methodInputs = [...document.querySelectorAll('input[name="method"]')];
  const amountElement = document.getElementById('sumAmount');
  const feeElement = document.getElementById('sumFee');
  const totalElement = document.getElementById('sumTotal');
  const receipt = document.getElementById('receipt');
  const receiptText = document.getElementById('receiptText');
  let orders = [];
  let selectedOrderId = null;

  function selectedOrder() {
    return orders.find((order) => String(order.id) === String(selectedOrderId));
  }

  function updateSummary() {
    const order = selectedOrder();
    const amount = Number(order?.totalAmount) || 0;
    const method = methodInputs.find((input) => input.checked)?.value;
    const fee = method === 'CARD'
      ? Math.round(amount * 0.02 * 100) / 100
      : 100;
    amountElement.textContent = app.money(amount);
    feeElement.textContent = app.money(order ? fee : 0);
    totalElement.textContent = app.money(order ? amount + fee : 0);
    cardFields.hidden = method !== 'CARD';
    payButton.disabled = !order;
  }

  function renderOrderChoices() {
    choices.replaceChildren();
    const selectedId = new URLSearchParams(window.location.search).get('orderId');
    const unpaid = orders.filter((order) =>
      !order.hasPaidPayment && order.status !== 'DELIVERED');
    if (!unpaid.length) {
      const empty = document.createElement('p');
      empty.className = 'muted';
      empty.textContent = 'There are no unpaid orders available.';
      choices.appendChild(empty);
      selectedOrderId = null;
      updateSummary();
      return;
    }
    const preferredId = unpaid.some((order) => String(order.id) === String(selectedId))
      ? selectedId
      : selectedOrderId;
    const orderToSelectId = unpaid.some((order) => String(order.id) === String(preferredId))
      ? preferredId
      : String(unpaid[0].id);
    unpaid.forEach((order) => {
      const label = document.createElement('label');
      label.className = 'choice';
      const input = document.createElement('input');
      input.type = 'radio';
      input.name = 'selectedOrder';
      input.value = order.id;
      input.checked = String(order.id) === String(orderToSelectId);
      input.addEventListener('change', () => {
        selectedOrderId = input.value;
        updateSummary();
      });
      const body = document.createElement('span');
      body.className = 'choice-body';
      const title = document.createElement('span');
      title.className = 'choice-title';
      title.textContent = `Order #${order.id} · ${app.money(order.totalAmount)}`;
      const summary = document.createElement('span');
      summary.className = 'muted';
      summary.textContent = order.itemsSummary || order.note || 'Bakery order';
      body.append(title, summary);
      label.append(input, body);
      choices.appendChild(label);
      if (input.checked) selectedOrderId = input.value;
    });
    updateSummary();
  }

  async function loadOrders() {
    app.clearError();
    try {
      const [customerOrders, payments] = await Promise.all([
        app.request(`/api/orders/user/${encodeURIComponent(app.getUserId())}`),
        app.request(`/api/payments/user/${encodeURIComponent(app.getUserId())}`)
      ]);
      if (!Array.isArray(customerOrders) || !Array.isArray(payments)) {
        throw new Error('Unable to read your orders and payments.');
      }
      const paidOrderIds = new Set(
        payments.filter((payment) => payment.status === 'PAID').map((payment) => String(payment.orderId))
      );
      orders = customerOrders.map((order) => ({
        ...order,
        hasPaidPayment: paidOrderIds.has(String(order.id))
      }));
      renderOrderChoices();
    } catch (error) {
      app.showError(error.message);
    }
  }

  async function submitPayment() {
    const order = selectedOrder();
    if (!order) return;
    const paymentType = methodInputs.find((input) => input.checked)?.value;
    const payload = { orderId: order.id, paymentType };
    if (paymentType === 'CARD') {
      payload.cardHolderName = document.getElementById('holder').value.trim();
      payload.cardNumber = document.getElementById('cardNumber').value.replace(/\s/g, '');
      if (!payload.cardHolderName || !/^\d{16}$/.test(payload.cardNumber)) {
        app.showError('Enter the cardholder name and a valid 16-digit card number.');
        return;
      }
    }

    payButton.disabled = true;
    app.clearError();
    try {
      const payment = await app.request('/api/payments', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      receipt.hidden = false;
      receiptText.textContent = `Payment #${payment.id} recorded for order #${payment.orderId}. ${payment.receiptNote || ''} Your invoice is available in My invoices.`;
      orders = orders.filter((item) => String(item.id) !== String(payment.orderId));
      selectedOrderId = null;
      renderOrderChoices();
      app.clearError();
      app.showToast('Payment recorded.');
    } catch (error) {
      app.showError(error.message);
    } finally {
      updateSummary();
    }
  }

  methodInputs.forEach((input) => input.addEventListener('change', updateSummary));
  payButton.addEventListener('click', submitPayment);
  loadOrders();
})();
