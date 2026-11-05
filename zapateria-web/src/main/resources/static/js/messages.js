document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('[data-flash]').forEach(function(el) {
        var delay = parseInt(el.getAttribute('data-delay'), 10) || 3000;

        setTimeout(function() {
            el.classList.add('opacity-0');
            setTimeout(function() {
                el.remove();
            }, 500);
        }, delay);
    })
})