document.addEventListener('DOMContentLoaded', function () {
	var canvas = document.getElementById('salesChart');
	if (!canvas) {
		return;
	}

	var labels = canvas.getAttribute('data-labels').split(',');
	var values = canvas.getAttribute('data-values').split(',').map(Number);

	new Chart(canvas, {
		type: 'bar',
		data: {
			labels: labels,
			datasets: [{
				label: 'Vendido ($)',
				data: values,
				backgroundColor: '#1e293b'
			}]
		},
		options: {
			responsive: true,
			plugins: { legend: { display: false } },
			scales: { y: { beginAtZero: true } }
		}
	});
});