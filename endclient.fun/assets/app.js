// ===== Темы =====
(function(){
  var saved = localStorage.getItem('ecf-theme') || 'red';
  apply(saved);
  function apply(t){
    if(t==='claude'){document.documentElement.setAttribute('data-theme','claude');}
    else{document.documentElement.removeAttribute('data-theme');}
    document.querySelectorAll('.theme-toggle button').forEach(function(b){
      b.classList.toggle('active', b.dataset.theme===t);
    });
    localStorage.setItem('ecf-theme', t);
  }
  document.addEventListener('click', function(e){
    var b = e.target.closest('.theme-toggle button');
    if(b){ apply(b.dataset.theme); }
  });
})();

// ===== Появление при скролле =====
(function(){
  var els = document.querySelectorAll('.reveal');
  if(!('IntersectionObserver' in window)){els.forEach(function(el){el.classList.add('in');});return;}
  var io = new IntersectionObserver(function(entries){
    entries.forEach(function(en){ if(en.isIntersecting){ en.target.classList.add('in'); io.unobserve(en.target); } });
  },{threshold:.14});
  els.forEach(function(el){io.observe(el);});
})();
