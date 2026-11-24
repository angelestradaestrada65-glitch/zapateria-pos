document.addEventListener('DOMContentLoaded', function() {
    var canvas = document.getElementById('salesChart');
    if (!canvas) {
        return;
    }

    var labels = canvas.getAttribute('data-labels').split(',');
    var sold = canvas.getAttribute('data-sold').split(',').map(Number);
    var collected = canvas.getAttribute('data-collected').split(',').map(Number);

    new Chart(canvas, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                { label: 'Vendido ($)', data: sold, backgroundColor: '#1e293b' },
                { label: 'Cobrado ($)', data: collected, backgroundColor: '#15803d' }
            ]
        },
        options: {
            responsive: true,
            scales: { y: { beginAtZero: true } }
        }
    });
});