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
