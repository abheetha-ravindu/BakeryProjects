(() => {
  const trackIds = ['galleryRow1', 'galleryRow2'];
  const imageFallback = '/images/3-orader%20bakery%20items%20or%20custom%20cake%20booking/base.png';

  function createCard(item) {
    const card = document.createElement('article');
    card.className = 'gallery-card';

    const imageWrap = document.createElement('div');
    imageWrap.className = 'gallery-card-img-wrap';

    const image = document.createElement('img');
    image.alt = String(item.name || 'Bakery item');
    image.loading = 'lazy';
    const imageUrl = typeof item.imageUrl === 'string'
      && item.imageUrl.startsWith('/')
      && !item.imageUrl.startsWith('//')
      ? item.imageUrl
      : imageFallback;
    image.src = imageUrl;
    image.addEventListener('error', () => {
      if (image.src.endsWith(imageFallback)) return;
      image.src = imageFallback;
    }, { once: true });
    imageWrap.append(image);

    const body = document.createElement('div');
    body.className = 'gallery-card-body';

    const tag = document.createElement('span');
    tag.className = 'gallery-card-tag';
    tag.textContent = item.category || 'Bakery favorite';

    const title = document.createElement('h3');
    title.className = 'gallery-card-title';
    title.textContent = item.name || 'Bakery item';

    body.append(tag, title);
    card.append(imageWrap, body);
    return card;
  }

  function showMessage(track, message) {
    const status = document.createElement('p');
    status.className = 'gallery-load-state';
    status.textContent = message;
    track.replaceChildren(status);
  }

  async function loadGallery() {
    const tracks = trackIds.map((id) => document.getElementById(id));
    if (tracks.some((track) => !track)) return;
    const wrapper = tracks[0].closest('.gallery-tracks-wrapper');
    tracks.forEach((track) => showMessage(track, 'Loading bakery favorites...'));

    try {
      const response = await fetch('/api/items', {
        headers: { Accept: 'application/json' },
        cache: 'no-store'
      });
      if (!response.ok) {
        throw new Error(`Item request failed with status ${response.status}`);
      }

      const items = await response.json();
      if (!Array.isArray(items)) {
        throw new Error('The item request returned an invalid response.');
      }

      const availableItems = items.filter((item) => item && item.id && item.name);
      if (availableItems.length === 0) {
        tracks.forEach((track) => showMessage(track, 'No bakery items are available right now.'));
        if (wrapper) wrapper.hidden = false;
        return;
      }

      tracks.forEach((track) => {
        const group = document.createElement('div');
        group.className = 'gallery-track-group';
        availableItems.forEach((item) => group.append(createCard(item)));

        const repeatedGroup = group.cloneNode(true);
        repeatedGroup.setAttribute('aria-hidden', 'true');
        track.replaceChildren(group, repeatedGroup);
      });
      if (wrapper) wrapper.hidden = false;
    } catch (error) {
      console.error('Unable to load bakery gallery items.', error);
      tracks.forEach((track) => showMessage(track, 'Bakery favorites could not be loaded. Please try again later.'));
      if (wrapper) wrapper.hidden = false;
    }
  }

  loadGallery();
})();
