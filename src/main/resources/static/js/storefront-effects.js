(() => {
  const selectors = [
    '.home-hero',
    '.cake-page-hero',
    '.gallery-section',
    '.services-choice-section',
    '.how-it-works-section',
    '.why-choose-us-section',
    'body.verdant-storefront > section',
    'body.verdant-storefront > main > section',
    '.site-footer'
  ].join(',');

  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches || !('IntersectionObserver' in window)) {
    return;
  }

  const observer = new IntersectionObserver((entries, currentObserver) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add('is-visible');
        currentObserver.unobserve(entry.target);
      }
    });
  }, { threshold: 0.12, rootMargin: '0px 0px -32px 0px' });

  document.querySelectorAll(selectors).forEach((element) => {
    element.classList.add('verdant-reveal');
    observer.observe(element);
  });
})();
