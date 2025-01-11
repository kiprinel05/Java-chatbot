package org.example.admin;

import org.example.data.Artist;
import org.example.data.ArtistRepository;
import org.example.data.Ticket;
import org.example.data.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private TicketRepository ticketRepository;

    // ✅ Listare artiști
    @GetMapping("/artists")
    public List<Artist> getAllArtists() {
        return artistRepository.findAll();
    }

    // ✅ Adăugare artist
    @PostMapping("/artists")
    public ResponseEntity<String> addArtist(@RequestBody Artist artist) {
        artistRepository.save(artist);
        return ResponseEntity.ok("Artist adăugat cu succes!");
    }

    // ✅ Ștergere artist
    @DeleteMapping("/artists/{name}")
    public ResponseEntity<String> deleteArtist(@PathVariable String name) {
        if (artistRepository.existsById(name)) {
            artistRepository.deleteById(name);
            return ResponseEntity.ok("Artist șters cu succes!");
        }
        return ResponseEntity.status(404).body("Artistul nu a fost găsit.");
    }

    // ✅ Listare bilete
    @GetMapping("/tickets")
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    // ✅ Adăugare tip de bilet
    @PostMapping("/tickets")
    public ResponseEntity<String> addTicket(@RequestBody Ticket ticket) {
        ticketRepository.save(ticket);
        return ResponseEntity.ok("Tip de bilet adăugat cu succes!");
    }

    // ✅ Actualizare preț bilet
    @PutMapping("/tickets/{type}")
    public ResponseEntity<String> updateTicketPrice(@PathVariable String type, @RequestBody Ticket ticket) {
        Optional<Ticket> existingTicket = ticketRepository.findById(type);
        if (existingTicket.isPresent()) {
            existingTicket.get().setPrice(ticket.getPrice());
            ticketRepository.save(existingTicket.get());
            return ResponseEntity.ok("Prețul biletului a fost actualizat cu succes!");
        }
        return ResponseEntity.status(404).body("Tipul de bilet nu a fost găsit.");
    }
}
