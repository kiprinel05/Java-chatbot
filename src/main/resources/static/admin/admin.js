// Modernized JavaScript for managing the festival admin panel

document.getElementById('fetchArtistsButton').addEventListener('click', fetchArtists);

async function fetchArtists() {
    try {
        const response = await fetch('/admin/artists');
        if (!response.ok) throw new Error('Eroare la încărcarea artiștilor.');

        const data = await response.json();
        const artistTable = document.getElementById('artistTable').querySelector('tbody');

        artistTable.innerHTML = '';

        data.forEach(artist => {
            const row = document.createElement('tr');
            row.innerHTML = `<td>${artist.name}</td><td>${artist.performanceDate}</td><td>${artist.performanceTime}</td>`;
            artistTable.appendChild(row);
        });
    } catch (error) {
        alert(error.message);
    }
}

document.getElementById('addArtistForm').addEventListener('submit', async function (event) {
    event.preventDefault();
    const artist = {
        name: document.getElementById('artistName').value,
        performanceDate: document.getElementById('performanceDate').value,
        performanceTime: document.getElementById('performanceTime').value
    };

    try {
        const response = await fetch('/admin/artists', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(artist)
        });
        if (response.ok) fetchArtists();
    } catch (error) {
        alert('Eroare la adăugarea artistului.');
    }
});
async function populateArtistSelect() {
    try {
        const response = await fetch('/admin/artists'); // Asigură-te că URL-ul este corect
        if (!response.ok) throw new Error('Eroare la încărcarea artiștilor.');

        const data = await response.json();
        const artistSelect = document.getElementById('artistSelect');

        // Curăță lista existentă
        artistSelect.innerHTML = '<option value="" disabled selected>Alege un Artist</option>';

        // Adaugă artiștii în lista de selectare
        data.forEach(artist => {
            const option = document.createElement('option');
            option.value = artist.id; // ID unic pentru artist
            option.textContent = artist.name; // Numele artistului
            artistSelect.appendChild(option);
        });
    } catch (error) {
        console.error('Eroare:', error.message); // Logare pentru debugging
        alert('Nu s-a putut încărca lista de artiști.');
    }
}

// Populează lista la încărcarea paginii
document.addEventListener('DOMContentLoaded', populateArtistSelect);

// Ștergere artist
document.getElementById('deleteArtistForm').addEventListener('submit', async function (event) {
    event.preventDefault();
    const artistId = document.getElementById('artistSelect').value;

    if (!artistId) {
        alert('Te rog să selectezi un artist.');
        return;
    }

    try {
        const response = await fetch(`/artists/${artistId}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Eroare la ștergerea artistului.');

        alert('Artistul a fost șters cu succes.');
        populateArtistSelect();
        fetchArtists();
    } catch (error) {
        alert(error.message);
    }
});
