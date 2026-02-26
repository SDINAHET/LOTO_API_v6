(function () {
  const year = new Date().getFullYear();
  const y = document.getElementById("y");
  const yf = document.getElementById("yf");
  if (y) y.textContent = year;
  if (yf) yf.textContent = year;

  // Reveal on scroll (IntersectionObserver)
  const io = new IntersectionObserver((entries) => {
    for (const e of entries) {
      if (!e.isIntersecting) continue;
      e.target.classList.add("is-visible");

      // Animate skill bars when visible
      const bars = e.target.querySelectorAll('[data-animate="bars"]');
      bars.forEach(b => b.classList.add("is-on"));

      io.unobserve(e.target);
    }
  }, { threshold: 0.12 });

  document.querySelectorAll(".reveal").forEach(el => io.observe(el));
})();

