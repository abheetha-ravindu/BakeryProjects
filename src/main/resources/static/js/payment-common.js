(() => {
  function date(value) {
    if (!value) return '—';
    const parsed = new Date(value);
    return Number.isNaN(parsed.getTime()) ? String(value) : parsed.toLocaleString();
  }

  function status(value) {
    return String(value || 'UNKNOWN').toLowerCase().replaceAll('_', ' ');
  }

  function setStatus(element, value) {
    element.textContent = status(value);
    element.className = 'status';
    element.dataset.status = value || 'UNKNOWN';
  }

  window.BakeryPayment = { date, status, setStatus };
})();
