// ✅ JavaScript pentru controlarea tabelului și gestionarea artiștilor

// Funcție pentru a afișa/ascunde tabelul
function toggleArtistsTable() {
    const table = document.getElementById('artistsTable');
    table.classList.toggle('show-table');
}

// ✅ Funcția de afișare a artiștilor în tabel
async function fetchArtists() {
    try {
        const response = await fetch('/admin/artists');
        if (!response.ok) {
            throw new Error('Eroare la încărcarea artiștilor.');
        }

        const artists = await response.json();
        const tableBody = document.querySelector('#artistsTable tbody');

        // Curățare tabel înainte de reîncărcare
        tableBody.innerHTML = '';

        // Adăugare date în tabel
        artists.forEach(artist => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${artist.name}</td>
                <td>${artist.performanceDate}</td>
                <td>${artist.performanceTime}</td>
            `;
            tableBody.appendChild(row);
        });

        // Afișarea tabelului doar dacă există artiști
        if (artists.length > 0) {
            toggleArtistsTable();
        }
    } catch (error) {
        alert('Eroare: ' + error.message);
    }
}

// ✅ Adăugarea unui artist
const addArtistForm = document.getElementById('addArtistForm');
if (addArtistForm) {
    addArtistForm.addEventListener('submit', async function (event) {
        event.preventDefault();
        const artist = {
            name: document.getElementById('artistName').value,
            performanceDate: document.getElementById('performanceDate').value,
            performanceTime: document.getElementById('performanceTime').value
        };

        await fetch('/admin/artists', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(artist)
        });
        fetchArtists();
    });
}

// ✅ Adăugarea unui tip de bilet
const addTicketForm = document.getElementById('addTicketForm');
if (addTicketForm) {
    addTicketForm.addEventListener('submit', async function (event) {
        event.preventDefault();
        const ticket = {
            type: document.getElementById('ticketType').value,
            available: document.getElementById('availableTickets').value,
            price: document.getElementById('ticketPrice').value
        };

        await fetch('/admin/tickets', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ticket)
        });
    });
}
