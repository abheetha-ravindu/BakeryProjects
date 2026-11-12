(() => {
  const mount = document.getElementById('pageSearchMount');
  if (!mount) return;

  const pages = [
    { title: 'Home', url: '/index.html', keywords: 'bakery shop products cakes' },
    { title: 'Items and Cakes', url: '/item.html', keywords: 'products catalog bakery items' },
    { title: 'Custom Cake Booking', url: '/custom-cake.html', keywords: 'custom cake design booking' },
    { title: 'My Orders', url: '/orders.html', keywords: 'order history order status' },
    { title: 'Make a Payment', url: '/payment.html', keywords: 'checkout pay card cash' },
    { title: 'My Payments', url: '/payments.html', keywords: 'payment history receipts' },
    { title: 'My Invoices', url: '/billing/my-invoices.html', keywords: 'billing invoice receipts' },
    { title: 'Invoice Details', url: '/billing/invoice-detail.html', keywords: 'billing invoice receipt' },
    { title: 'Request a Refund', url: '/billing/refund-request-form.html', keywords: 'refund return payment' },
    { title: 'My Profile', url: '/profileupdate.html', keywords: 'account profile settings' },
    { title: 'Login', url: '/login.html', keywords: 'sign in account' },
    { title: 'Alternate Login', url: '/loging.html', keywords: 'sign in account' },
    { title: 'Create Account', url: '/register.html', keywords: 'register signup customer' },
    { title: 'Sign Up', url: '/singup.html', keywords: 'register create account customer' },
    { title: 'Admin Profile', url: '/adminprofile.html', keywords: 'administrator account profile' },
    { title: 'Admin User List', url: '/adminuserlist.html', keywords: 'administrator customers users' },
    { title: 'Admin Dashboard', url: '/adminpages/adminindex.html', keywords: 'administrator overview reports' },
    { title: 'Admin Orders', url: '/adminpages/admin-orders.html', keywords: 'administrator bakery order management' },
    { title: 'Admin Payments', url: '/adminpages/admin-payments.html', keywords: 'administrator payment management' },
    { title: 'Admin Refunds', url: '/adminpages/admin-refund-management.html', keywords: 'administrator refund management' },
    { title: 'Admin Items', url: '/adminpages/adminitem.html', keywords: 'administrator product catalog' },
    { title: 'Admin Sign Up', url: '/adminpages/adminsingup.html', keywords: 'administrator register account' },
    { title: 'All Invoices', url: '/adminpages/allinvoicess.html', keywords: 'administrator billing receipts' },
    { title: 'Custom Orders', url: '/adminpages/custome_orders.html', keywords: 'administrator custom cake bookings' },
    { title: 'Delivery Count', url: '/adminpages/delivery-count.html', keywords: 'administrator deliveries' },
    { title: 'User Management', url: '/adminpages/userManage.html', keywords: 'administrator customers users' }
  ];

  const style = document.createElement('style');
  style.textContent = `
    .page-search {
      position: fixed;
      right: 20px;
      bottom: 20px;
      z-index: 10000;
      width: min(390px, calc(100vw - 40px));
      color: #30221d;
      font: 14px/1.4 Arial, sans-serif;
    }
    .page-search form {
      display: flex;
      gap: 8px;
      padding: 8px;
      border: 1px solid #eadbce;
      border-radius: 12px;
      background: #fff;
      box-shadow: 0 8px 26px rgba(48, 34, 29, .16);
    }
    .page-search input {
      width: 100%;
      min-width: 0;
      padding: 8px;
      border: 0;
      outline: 0;
      color: inherit;
      font: inherit;
    }
    .page-search form:focus-within {
      outline: 2px solid #79b9a2;
      outline-offset: 2px;
    }
    .page-search button {
      flex: 0 0 auto;
      padding: 0 12px;
      border: 0;
      border-radius: 8px;
      background: linear-gradient(135deg, #315d48, #1f4f43);
      color: #fff;
      font: inherit;
      font-weight: 700;
      cursor: pointer;
    }
    .page-search-results {
      display: none;
      max-height: min(45vh, 360px);
      overflow-y: auto;
      margin: 0 0 8px;
      padding: 6px;
      border: 1px solid #eadbce;
      border-radius: 12px;
      background: #fff;
      box-shadow: 0 8px 26px rgba(48, 34, 29, .16);
      list-style: none;
    }
    .page-search-results[data-open="true"] { display: block; }
    .page-search-results a,
    .page-search-empty {
      display: block;
      padding: 10px 12px;
      border-radius: 8px;
      color: inherit;
      text-decoration: none;
    }
    .page-search-results a:hover,
    .page-search-results a:focus,
    .page-search-results a[aria-selected="true"] {
      outline: none;
      background: rgba(38, 112, 90, 0.12);
      color: #1f4f43;
    }
    .page-search-empty { color: #715f55; }
    .page-search--navbar {
      position: relative;
      right: auto;
      bottom: auto;
      z-index: 35;
      width: 100%;
      color: #d7dcd7;
      font-size: 12px;
    }
    .page-search--navbar form {
      gap: 6px;
      padding: 4px;
      border-color: rgba(255, 255, 255, .09);
      border-radius: 8px;
      background: rgba(255, 255, 255, .03);
      box-shadow: none;
    }
    .page-search--navbar input {
      padding: 6px 8px;
      background: transparent;
      color: #f5f5f3;
    }
    .page-search--navbar input::placeholder { color: #9da69f; }
    .page-search--navbar button {
      padding: 0 11px;
      border-radius: 6px;
    }
    .page-search--navbar .page-search-results {
      position: absolute;
      top: calc(100% + 8px);
      right: 0;
      left: 0;
      z-index: 1;
      margin: 0;
      border-color: rgba(255, 255, 255, .1);
      background: #101712;
      color: #f5f5f3;
      box-shadow: 0 12px 30px rgba(0, 0, 0, .35);
    }
    .page-search--navbar .page-search-results a:hover,
    .page-search--navbar .page-search-results a:focus,
    .page-search--navbar .page-search-results a[aria-selected="true"] {
      background: rgba(38, 112, 90, .28);
      color: #d7efe7;
    }
    .page-search--navbar .page-search-empty { color: #b5b9b5; }
    @media (max-width: 520px) {
      .page-search { right: 12px; bottom: 12px; width: calc(100vw - 24px); }
    }
  `;
  document.head.append(style);

  const container = document.createElement('section');
  container.className = 'page-search page-search--navbar';
  container.setAttribute('aria-label', 'Search website pages');
  container.innerHTML = `
    <ul class="page-search-results" id="pageSearchResults" role="listbox" aria-label="Pages"></ul>
    <form role="search">
      <input id="pageSearchInput" type="search" placeholder="Search all pages..." autocomplete="off"
        aria-label="Search all pages" aria-controls="pageSearchResults" aria-expanded="false">
      <button type="submit">Search</button>
    </form>
  `;
  (mount || document.body).append(container);

  const input = container.querySelector('#pageSearchInput');
  const results = container.querySelector('#pageSearchResults');
  let activeIndex = -1;

  function renderResults() {
    const query = input.value.trim().toLocaleLowerCase();
    const matches = pages.filter((page) =>
      `${page.title} ${page.keywords}`.toLocaleLowerCase().includes(query)
    );
    results.replaceChildren();
    activeIndex = -1;

    if (!matches.length) {
      const empty = document.createElement('li');
      empty.className = 'page-search-empty';
      empty.textContent = 'No matching pages found.';
      results.append(empty);
    } else {
      matches.forEach((page, index) => {
        const item = document.createElement('li');
        const link = document.createElement('a');
        link.href = page.url;
        link.setAttribute('role', 'option');
        link.setAttribute('aria-selected', 'false');
        link.textContent = page.title;
        link.addEventListener('mouseenter', () => setActive(index));
        item.append(link);
        results.append(item);
      });
    }

    results.dataset.open = 'true';
    input.setAttribute('aria-expanded', 'true');
  }

  function closeResults() {
    results.dataset.open = 'false';
    input.setAttribute('aria-expanded', 'false');
    activeIndex = -1;
  }

  function setActive(index) {
    const links = [...results.querySelectorAll('a')];
    if (!links.length) return;
    activeIndex = (index + links.length) % links.length;
    links.forEach((link, linkIndex) => {
      link.setAttribute('aria-selected', String(linkIndex === activeIndex));
    });
    links[activeIndex].scrollIntoView({ block: 'nearest' });
  }

  input.addEventListener('input', renderResults);
  input.addEventListener('focus', renderResults);
  input.addEventListener('keydown', (event) => {
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      setActive(activeIndex + 1);
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      setActive(activeIndex - 1);
    } else if (event.key === 'Enter' && activeIndex >= 0) {
      event.preventDefault();
      results.querySelectorAll('a')[activeIndex]?.click();
    } else if (event.key === 'Escape') {
      closeResults();
    }
  });
  container.querySelector('form').addEventListener('submit', (event) => {
    event.preventDefault();
    const firstResult = results.querySelector('a');
    if (firstResult) {
      window.location.assign(firstResult.href);
    } else {
      renderResults();
    }
  });
  document.addEventListener('click', (event) => {
    if (!container.contains(event.target)) closeResults();
  });
})();
