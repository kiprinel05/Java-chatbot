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
        if (!response.ok) throw new Error("Eroare la încărcarea artiștilor.");

        const data = await response.json();
        const artistTable = document.getElementById('artistTable');

        if (!artistTable) {
            console.error("Elementul 'artistTable' nu a fost găsit în DOM.");
            return;
        }

        artistTable.innerHTML = ''; // Curăță tabelul anterior

        data.forEach(artist => {
            artistTable.innerHTML += `
                <tr>
                    <td>${artist.name}</td>
                    <td>${artist.performanceDate}</td>
                    <td>${artist.performanceTime}</td>
                </tr>
            `;
        });
    } catch (error) {
        alert(error.message);
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
