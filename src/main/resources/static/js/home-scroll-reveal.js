(() => {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches || !('IntersectionObserver' in window)) {
    return;
  }

  const revealGroups = [
    ['.home-hero-copy', 0],
    ['.cake-page-hero .hero-inner-wrap', 0],
    ['.section-header, .services-choice-header, .hiw-header, .wcu-header, .section-header-block', 0],
    ['.gallery-tracks-wrapper', 0],
    ['.shape-tabs-wrapper, .storefront-filters', 90],
    ['.services-cards-grid > .service-card', 120],
    ['.hiw-flow-container > .hiw-card', 120],
    ['.wcu-cards-grid > .wcu-card', 120],
    ['.cake-products-grid > .cake-card, .popular-picks-grid > .popular-pick-card', 100],
    ['.footer-container > *, .site-footer > .footer-bottom', 120],
    ['.footer-bottom-container', 0]
  ];
  const targets = new Set();
  const observer = new IntersectionObserver((entries, currentObserver) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add('is-visible');
        currentObserver.unobserve(entry.target);
      }
    });
  }, { threshold: 0.12, rootMargin: '0px 0px -32px 0px' });

  function observeGroup(selector, stagger) {
    document.querySelectorAll(selector).forEach((element) => {
      const siblings = Array.from(element.parentElement.children)
        .filter((sibling) => sibling.matches(selector));
      const delay = siblings.indexOf(element) * stagger;

      element.style.setProperty('--scroll-reveal-delay', `${delay}ms`);
      element.classList.add('scroll-reveal');
      if (!targets.has(element)) {
        targets.add(element);
        observer.observe(element);
      }
    });
  }

  revealGroups.forEach(([selector, stagger]) => {
    observeGroup(selector, stagger);
  });

  if (targets.size === 0) return;

  document.documentElement.classList.add('has-scroll-reveal');

  if ('MutationObserver' in window) {
    const dynamicContentObserver = new MutationObserver(() => {
      targets.forEach((element) => {
        if (!element.isConnected) {
          observer.unobserve(element);
          targets.delete(element);
        }
      });

      observeGroup('.cake-products-grid > .cake-card, .popular-picks-grid > .popular-pick-card', 100);
    });

    document.querySelectorAll('.cake-products-grid, .popular-picks-grid').forEach((grid) => {
      dynamicContentObserver.observe(grid, { childList: true });
    });
  }
})();
