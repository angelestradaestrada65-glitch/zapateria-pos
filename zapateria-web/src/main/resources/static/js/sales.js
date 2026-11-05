document.addEventListener('DOMContentLoaded', function() {
    var app = document.getElementById('sale-app');
    if (!app) {
        return;
    }

    var STORAGE_KEY = 'zapateria.cart';
    var MAX_VISIBLE = 8;
    var discount = parseFloat(app.getAttribute('data-discount')) || 0;

    var searchInput = document.getElementById('product-search');
    var hint = document.getElementById('search-hint');
    var productList = document.getElementById('product-list');
    var rows = Array.prototype.slice.call(productList.querySelectorAll('tr[data-id]'));

    var form = document.getElementById('sale-form');
    var cartList = document.getElementById('cart-list');
    var cartEmpty = document.getElementById('cart-empty');
    var cartHidden = document.getElementById('cart-hidden');
    var totalEl = document.getElementById('cart-total');
    var submitBtn = document.getElementById('submit-btn');
    var creditBox = form.querySelector('input[name="credit"]');
    var advanceBox = document.getElementById('advance-box');
    var advanceInput = form.querySelector('input[name="amountPaid"]');
    var pendingEl = document.getElementById('cart-pending');

    var products = {}; // id -> datos del producto
    var cart = {};     // id -> cantidad
    var total = 0;

    // ---------- utilidades ----------

    function normalize(text) {
        return text.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    }

    function money(n) {
        return '$' + (Math.round(n * 100) / 100).toFixed(2);
    }

    function el(tag, className, text) {
        var node = document.createElement(tag);
        if (className) {
            node.className = className;
        }
        if (text !== undefined) {
            node.textContent = text;
        }
        return node;
    }

    // ---------- buscador ----------

    function filterList() {
        var terms = normalize(searchInput.value).split(/\s+/).filter(Boolean);
        var shown = 0;
        var matches = 0;

        rows.forEach(function(row) {
            var ok = terms.every(function(t) {
                return row.getAttribute('data-search').indexOf(t) !== -1;
            });
            if (ok) {
                matches++;
            }
            var visible = ok && shown < MAX_VISIBLE;
            row.hidden = !visible;
            if (visible) {
                shown++;
            }
        });

        if (matches === 0) {
            hint.textContent = 'No se encontró ningún producto.';
        } else if (matches > shown) {
            hint.textContent = 'Mostrando ' + shown + ' de ' + matches + '. Escribí más para acotar.';
        } else {
            hint.textContent = matches + ' resultado(s).';
        }
    }

    // ---------- carrito ----------

    function addToCart(id) {
        var p = products[id];
        if (!p) {
            return;
        }
        var qty = cart[id] || 0;
        if (qty >= p.stock) {
            hint.textContent = 'Solo hay ' + p.stock + ' en stock de ese producto.';
            return;
        }
        cart[id] = qty + 1;
        render();
    }

    function changeQty(id, delta) {
        var p = products[id];
        if (!p || !cart[id]) {
            return;
        }
        var qty = cart[id] + delta;
        if (qty > p.stock) {
            hint.textContent = 'Solo hay ' + p.stock + ' en stock de ese producto.';
            return;
        }
        if (qty <= 0) {
            delete cart[id];
        } else {
            cart[id] = qty;
        }
        render();
    }

    function qtyButton(id, action, label) {
        var b = el('button', 'w-7 h-7 rounded border border-slate-300 hover:bg-slate-100', label);
        b.type = 'button';
        b.setAttribute('data-id', id);
        b.setAttribute('data-action', action);
        return b;
    }

    function addHidden(name, value) {
        var input = document.createElement('input');
        input.type = 'hidden';
        input.name = name;
        input.value = value;
        cartHidden.appendChild(input);
    }

    function render() {
        cartList.innerHTML = '';
        cartHidden.innerHTML = '';
        total = 0;

        var ids = Object.keys(cart);

        ids.forEach(function(id, index) {
            var p = products[id];
            var qty = cart[id];
            var unit = p.price - discount;
            total += unit * qty;

            var li = el('li', 'py-2 flex items-center justify-between gap-2');

            var info = el('div', 'min-w-0');
            info.appendChild(el('p', 'truncate', p.name));
            info.appendChild(el('p', 'text-xs text-slate-400',
                'Talla ' + p.size + ' · ' + p.color + ' · ' + money(unit)));

            var controls = el('div', 'flex items-center gap-1');
            controls.appendChild(qtyButton(id, 'dec', '−'));
            controls.appendChild(el('span', 'w-6 text-center', String(qty)));
            controls.appendChild(qtyButton(id, 'inc', '+'));

            li.appendChild(info);
            li.appendChild(controls);
            li.appendChild(el('span', 'w-20 text-right', money(unit * qty)));
            cartList.appendChild(li);

            // Estos campos ocultos son los que recibe el servidor (items[0].productDetailId, etc.)
            addHidden('items[' + index + '].productDetailId', id);
            addHidden('items[' + index + '].quantity', qty);
        });

        cartEmpty.hidden = ids.length > 0;
        totalEl.textContent = money(total);
        submitBtn.disabled = ids.length === 0;

        updateCredit();
        saveCart();
    }

    function updateCredit() {
        var credit = creditBox.checked;
        advanceBox.hidden = !credit;
        if (!credit) {
            pendingEl.textContent = '';
            return;
        }
        var advance = Math.min(Math.max(parseFloat(advanceInput.value) || 0, 0), total);
        pendingEl.textContent = 'Queda debiendo: ' + money(total - advance);
    }

    // ---------- que no se pierda el carrito si el servidor rechaza la venta ----------

    function saveCart() {
        try {
            sessionStorage.setItem(STORAGE_KEY, JSON.stringify(cart));
        } catch (err) {
            // si el navegador no deja guardar, la pantalla sigue funcionando igual
        }
    }

    function loadCart() {
        try {
            // Si la venta se registró bien, se empieza de cero
            if (app.getAttribute('data-success') === 'true') {
                sessionStorage.removeItem(STORAGE_KEY);
                return;
            }
            var saved = JSON.parse(sessionStorage.getItem(STORAGE_KEY) || '{}');
            Object.keys(saved).forEach(function(id) {
                var p = products[id];
                var qty = Math.min(parseInt(saved[id], 10) || 0, p ? p.stock : 0);
                if (p && qty > 0) {
                    cart[id] = qty;
                }
            });
        } catch (err) {
            cart = {};
        }
    }

    // ---------- arranque ----------

    rows.forEach(function(row) {
        var id = row.getAttribute('data-id');
        products[id] = {
            name: row.getAttribute('data-name'),
            size: row.getAttribute('data-size'),
            color: row.getAttribute('data-color'),
            price: parseFloat(row.getAttribute('data-price')),
            stock: parseInt(row.getAttribute('data-stock'), 10)
        };
        row.setAttribute('data-search', normalize([
            row.getAttribute('data-name'),
            row.getAttribute('data-brand'),
            row.getAttribute('data-size'),
            row.getAttribute('data-color')
        ].join(' ')));
    });

    productList.addEventListener('click', function(e) {
        var btn = e.target.closest('button[data-add]');
        if (btn) {
            addToCart(btn.closest('tr').getAttribute('data-id'));
        }
    });

    cartList.addEventListener('click', function(e) {
        var btn = e.target.closest('button[data-action]');
        if (btn) {
            changeQty(btn.getAttribute('data-id'), btn.getAttribute('data-action') === 'inc' ? 1 : -1);
        }
    });

    searchInput.addEventListener('input', filterList);

    // Enter agrega el producto cuando queda uno solo en la lista
    searchInput.addEventListener('keydown', function(e) {
        if (e.key !== 'Enter') {
            return;
        }
        e.preventDefault();
        var visible = rows.filter(function(r) {
            return !r.hidden;
        });
        if (visible.length === 1) {
            addToCart(visible[0].getAttribute('data-id'));
            searchInput.value = '';
            filterList();
        }
    });

    creditBox.addEventListener('change', updateCredit);
    advanceInput.addEventListener('input', updateCredit);

    // Evita registrar la misma venta dos veces por un doble clic
    form.addEventListener('submit', function() {
        submitBtn.disabled = true;
    });

    loadCart();
    filterList();
    render();
    searchInput.focus();
});