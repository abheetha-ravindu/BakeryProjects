(() => {
  const app = window.BakeryApp;
  const list = document.getElementById('paymentList');

  async function loadPayments() {
    list.replaceChildren();
    app.clearError();
    try {
      const payments = await app.request(`/api/payments/user/${encodeURIComponent(app.getUserId())}`);
      if (!Array.isArray(payments)) throw new Error('The payments response was not a list.');
      if (!payments.length) {
        const empty = document.createElement('p');
        empty.className = 'muted';
        empty.textContent = 'No payments have been recorded yet.';
        list.appendChild(empty);
        return;
      }
      payments.forEach((payment) => {
        const card = document.createElement('article');
        card.className = 'order-card';
        const header = document.createElement('div');
        header.className = 'order-card-header';
        const title = document.createElement('h2');
        title.textContent = `Payment #${payment.id} · Order #${payment.orderId}`;
        const badge = document.createElement('span');
        window.BakeryPayment.setStatus(badge, payment.status);
        header.append(title, badge);
        card.appendChild(header);
        const details = document.createElement('p');
        details.textContent = `${payment.paymentType || 'Payment'} · ${window.BakeryPayment.date(payment.createdAt)}`;
        const amount = document.createElement('p');
        amount.className = 'muted';
        amount.textContent = `Amount ${app.money(payment.amount)} · Fee ${app.money(payment.fee)} · Total ${app.money(payment.totalCharged)}`;
        card.append(details, amount);
        if (payment.receiptNote) {
          const receipt = document.createElement('p');
          receipt.className = 'muted';
          receipt.textContent = payment.receiptNote;
          card.appendChild(receipt);
        }
        list.appendChild(card);
      });
    } catch (error) {
      app.showError(error.message);
    }
  }

  loadPayments();
})();
