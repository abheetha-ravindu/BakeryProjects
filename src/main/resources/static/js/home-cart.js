(() => {
  const CART_KEY = 'sweetbloom_cart';
  const overlay = document.getElementById('cartDrawerOverlay');
  const body = document.getElementById('cartDrawerBody');
  const total = document.getElementById('cartDrawerTotal');
  const checkoutLink = document.getElementById('cartCheckoutLink');
  const badge = document.querySelector('#navCartBtn .cart-badge');
  let cartItems = [];

  checkoutLink?.addEventListener('click', event => {
    if (checkoutLink.getAttribute('aria-disabled') === 'true') event.preventDefault();
  });

  function loadCart() {
    try {
      const stored = localStorage.getItem(CART_KEY);
      const parsed = stored ? JSON.parse(stored) : [];
      if (!Array.isArray(parsed) || parsed.some(item => !item || typeof item !== 'object')) {
        throw new Error('Saved cart data is invalid.');
      }
      cartItems = parsed;
      renderCart();
    } catch (error) {
      console.error('Unable to load the saved cart.', error);
      cartItems = [];
      if (body) body.textContent = 'Your saved basket could not be loaded. Please open the shop page to review your order.';
      if (checkoutLink) checkoutLink.setAttribute('aria-disabled', 'true');
    }
  }

  function saveCart() {
    try {
      localStorage.setItem(CART_KEY, JSON.stringify(cartItems));
      renderCart();
    } catch (error) {
      console.error('Unable to save the cart.', error);
      if (body) body.textContent = 'Your basket could not be saved. Check your browser storage settings and try again.';
    }
  }

  function renderCart() {
    const itemCount = cartItems.reduce((sum, item) => {
      const quantity = Number(item.qty);
      return sum + (Number.isFinite(quantity) && quantity > 0 ? quantity : 0);
    }, 0);
    if (badge) {
      badge.textContent = String(itemCount);
      badge.style.display = itemCount > 0 ? 'flex' : 'none';
    }
    if (!body || !total || !checkoutLink) return;

    if (cartItems.length === 0) {
      body.innerHTML = '<div class="cart-empty-message"><span aria-hidden="true">🎂</span><h2>Your basket is empty</h2><p>Browse our bakery items and add something sweet.</p></div>';
      total.textContent = 'Rs. 0';
      checkoutLink.setAttribute('aria-disabled', 'true');
      return;
    }

    checkoutLink.setAttribute('aria-disabled', 'false');
    let subtotal = 0;
    body.innerHTML = cartItems.map((item, index) => {
      const rawQuantity = Number(item.qty);
      const quantity = Number.isFinite(rawQuantity) ? Math.max(1, Math.floor(rawQuantity)) : 1;
      const rawPrice = Number(item.price);
      const price = Number.isFinite(rawPrice) ? Math.max(0, rawPrice) : 0;
      const itemTotal = quantity * price;
      subtotal += itemTotal;
      return `<article class="cart-item-card">
        <div class="cart-item-details">
          <div class="cart-item-title">${escapeHtml(item.name || 'Bakery item')}</div>
          <div class="cart-item-unit-price">Rs. ${price.toLocaleString()} each</div>
          <div class="cart-item-controls" aria-label="Quantity controls for ${escapeHtml(item.name || 'item')}">
            <button type="button" onclick="updateHomeCartItem(${index}, -1)" aria-label="Decrease quantity">−</button>
            <span>${quantity}</span>
            <button type="button" onclick="updateHomeCartItem(${index}, 1)" aria-label="Increase quantity">+</button>
            <button type="button" onclick="removeHomeCartItem(${index})" aria-label="Remove item">Remove</button>
          </div>
        </div>
        <div class="cart-item-total">Rs. ${itemTotal.toLocaleString()}</div>
      </article>`;
    }).join('');
    total.textContent = `Rs. ${subtotal.toLocaleString()}`;
  }

  function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, character => ({
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#39;'
    })[character]);
  }

  window.toggleHomeCartDrawer = event => {
    if (event) event.preventDefault();
    if (!overlay) return;
    if (!overlay.classList.contains('open')) loadCart();
    overlay.classList.toggle('open');
    const isOpen = overlay.classList.contains('open');
    overlay.setAttribute('aria-hidden', String(!isOpen));
    document.body.style.overflow = isOpen ? 'hidden' : '';
    if (isOpen) overlay.querySelector('.cart-close-btn')?.focus();
    else document.getElementById('navCartBtn')?.focus();
  };

  window.handleHomeCartOverlayClick = event => {
    if (event.target === overlay) window.toggleHomeCartDrawer();
  };

  window.updateHomeCartItem = (index, change) => {
    const item = cartItems[index];
    if (!item) return;
    const rawQuantity = Number(item.qty);
    item.qty = (Number.isFinite(rawQuantity) ? Math.max(1, Math.floor(rawQuantity)) : 1) + change;
    if (item.qty <= 0) cartItems.splice(index, 1);
    saveCart();
  };

  window.removeHomeCartItem = index => {
    if (!cartItems[index]) return;
    cartItems.splice(index, 1);
    saveCart();
  };

  window.clearHomeCart = () => {
    cartItems = [];
    saveCart();
  };

  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && overlay?.classList.contains('open')) {
      window.toggleHomeCartDrawer();
    }
  });

  window.addEventListener('storage', event => {
    if (event.key === CART_KEY) loadCart();
  });

  loadCart();
})();
