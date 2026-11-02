(() => {
  const USER_ID_KEY = 'sweetbloom_user_id';
  const banner = document.getElementById('demoBanner');
  const toast = document.getElementById('toast');
  let toastTimer;

  function getUserId() {
    try {
      const currentUser = JSON.parse(localStorage.getItem('sweetbloom_user') || 'null');
      if (currentUser?.id) {
        localStorage.setItem(USER_ID_KEY, String(currentUser.id));
        return String(currentUser.id);
      }
    } catch (error) {
      console.error('Unable to read the signed-in bakery account.', error);
    }
    let userId = localStorage.getItem(USER_ID_KEY);
    if (!userId) {
      userId = String(Date.now());
      localStorage.setItem(USER_ID_KEY, userId);
    }
    return userId;
  }

  async function request(url, options = {}) {
    const response = await fetch(url, {
      ...options,
      headers: {
        Accept: 'application/json',
        ...(options.body ? { 'Content-Type': 'application/json' } : {}),
        ...options.headers
      }
    });

    if (!response.ok) {
      let detail = `Request failed (${response.status}).`;
      try {
        const body = await response.json();
        detail = body.error || body.message || Object.values(body).join(' ') || detail;
      } catch {
        const body = await response.text();
        if (body) detail = body;
      }
      throw new Error(detail);
    }

    if (response.status === 204) return null;
    const contentType = response.headers.get('content-type') || '';
    return contentType.includes('application/json') ? response.json() : null;
  }

  function showError(message) {
    if (!banner) return;
    banner.textContent = message;
    banner.hidden = false;
  }

  function clearError() {
    if (banner) banner.hidden = true;
  }

  function money(value) {
    const amount = Number(value);
    return `Rs. ${Number.isFinite(amount) ? amount.toLocaleString(undefined, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }) : '0.00'}`;
  }

  function showToast(message) {
    if (!toast) return;
    toast.textContent = message;
    toast.classList.add('show');
    window.clearTimeout(toastTimer);
    toastTimer = window.setTimeout(() => toast.classList.remove('show'), 2800);
  }

  window.BakeryApp = { getUserId, request, showError, clearError, money, showToast };
})();
