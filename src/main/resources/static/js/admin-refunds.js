(() => {
  const app = window.BakeryApp;
  const rows = document.getElementById('refundTableBody');
  const emptyState = document.getElementById('emptyState');
  const statusFilter = document.getElementById('statusFilter');
  const refreshButton = document.getElementById('refreshButton');
  let refunds = [];

  function render() {
    rows.replaceChildren();
    const visibleRefunds = refunds.filter((refund) =>
      !statusFilter.value || refund.status === statusFilter.value
    );

    visibleRefunds.forEach((refund) => {
      const row = document.createElement('tr');
      const values = [
        `#${refund.id}`,
        `#${refund.orderId}`,
        window.BakeryPayment.date(refund.requestDate),
        refund.reason || '—'
      ];

      values.forEach((value, index) => {
        const cell = document.createElement('td');
        cell.textContent = value;
        if (index === 3) cell.className = 'reason-cell';
        row.appendChild(cell);
      });

      const statusCell = document.createElement('td');
      const badge = document.createElement('span');
      badge.className = 'status-badge';
      badge.dataset.status = refund.status || 'UNKNOWN';
      badge.textContent = String(refund.status || 'UNKNOWN').toLowerCase();
      statusCell.appendChild(badge);

      const actionCell = document.createElement('td');
      if (refund.status === 'PENDING') {
        const actionGroup = document.createElement('div');
        actionGroup.className = 'action-group';
        [
          { status: 'APPROVED', label: 'Approve', className: 'approve-button' },
          { status: 'REJECTED', label: 'Reject', className: 'reject-button' }
        ].forEach(({ status, label, className }) => {
          const button = document.createElement('button');
          button.type = 'button';
          button.className = `action-button ${className}`;
          button.textContent = label;
          button.addEventListener('click', () => processRefund(refund, status, button));
          actionGroup.appendChild(button);
        });
        actionCell.appendChild(actionGroup);
      } else {
        actionCell.textContent = 'Processed';
      }

      row.append(statusCell, actionCell);
      rows.appendChild(row);
    });

    emptyState.hidden = visibleRefunds.length > 0;
  }

  async function loadRefunds() {
    app.clearError();
    refreshButton.disabled = true;
    try {
      const result = await app.request('/api/refund');
      if (!Array.isArray(result)) {
        throw new Error('The refund requests response was not a list.');
      }
      refunds = result;
      render();
    } catch (error) {
      app.showError(error.message);
    } finally {
      refreshButton.disabled = false;
    }
  }

  async function processRefund(refund, status, button) {
    const action = status.toLowerCase();
    if (!window.confirm(`${action[0].toUpperCase()}${action.slice(1)} refund request #${refund.id}?`)) {
      return;
    }

    const buttons = button.closest('.action-group').querySelectorAll('button');
    buttons.forEach((actionButton) => {
      actionButton.disabled = true;
    });

    try {
      await app.request(`/api/refund/${encodeURIComponent(refund.id)}?${new URLSearchParams({ status })}`, {
        method: 'PUT'
      });
      app.showToast(`Refund request #${refund.id} ${action}.`);
      await loadRefunds();
    } catch (error) {
      app.showError(error.message);
      buttons.forEach((actionButton) => {
        actionButton.disabled = false;
      });
    }
  }

  statusFilter.addEventListener('change', render);
  refreshButton.addEventListener('click', loadRefunds);
  loadRefunds();
})();
