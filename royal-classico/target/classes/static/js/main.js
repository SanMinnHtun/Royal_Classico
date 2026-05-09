/**
 * Royal Classico FC — News Slider
 * Auto-rotates every 5s, supports manual navigation.
 */
(function () {
  const slides  = document.querySelectorAll('.slide');
  const dots    = document.querySelectorAll('.slider__dot');
  const prevBtn = document.getElementById('slider-prev');
  const nextBtn = document.getElementById('slider-next');

  if (!slides.length) return;

  let current  = 0;
  let autoPlay = null;

  function goTo(index) {
    slides[current].classList.remove('active');
    dots[current]?.classList.remove('active');
    current = (index + slides.length) % slides.length;
    slides[current].classList.add('active');
    dots[current]?.classList.add('active');
  }

  function startAuto() {
    autoPlay = setInterval(() => goTo(current + 1), 5000);
  }

  function resetAuto() {
    clearInterval(autoPlay);
    startAuto();
  }

  // Wire dots
  dots.forEach((dot, i) => {
    dot.addEventListener('click', () => { goTo(i); resetAuto(); });
  });

  // Wire arrows
  prevBtn?.addEventListener('click', () => { goTo(current - 1); resetAuto(); });
  nextBtn?.addEventListener('click', () => { goTo(current + 1); resetAuto(); });

  // Init
  goTo(0);
  startAuto();
})();

/**
 * Navbar scroll effect
 */
(function () {
  const navbar = document.querySelector('.navbar');
  if (!navbar) return;
  window.addEventListener('scroll', () => {
    navbar.style.background = window.scrollY > 50
      ? 'rgba(0,26,58,0.98)'
      : 'rgba(0,26,58,0.92)';
  }, { passive: true });
})();

/**
 * Countdown timer for next fixture
 */
(function () {
  const countdownEl  = document.getElementById('fixture-countdown');
  if (!countdownEl) return;

  const targetDate = countdownEl.dataset.target;
  if (!targetDate) return;

  const target = new Date(targetDate).getTime();

  function update() {
    const now  = Date.now();
    const diff = target - now;

    if (diff <= 0) {
      countdownEl.textContent = "LIVE / ENDED";
      return;
    }

    const d = Math.floor(diff / 86400000);
    const h = Math.floor((diff % 86400000) / 3600000);
    const m = Math.floor((diff % 3600000)  / 60000);
    const s = Math.floor((diff % 60000)    / 1000);

    countdownEl.innerHTML =
      `<span>${d}<small>d</small></span>` +
      `<span>${h}<small>h</small></span>` +
      `<span>${m}<small>m</small></span>` +
      `<span>${s}<small>s</small></span>`;
  }

  update();
  setInterval(update, 1000);
})();
