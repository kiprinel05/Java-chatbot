package org.example.data;

import org.example.data.interfaces.ArtistRepository;
import org.example.data.interfaces.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Service
public class DataLoader {

    private final ArtistRepository artistRepository;
    private final TicketRepository ticketRepository;

    @Autowired
    public DataLoader(ArtistRepository artistRepository, TicketRepository ticketRepository) {
        this.artistRepository = artistRepository;
        this.ticketRepository = ticketRepository;
    }

    @PostConstruct
    public void loadFestivalData() {
        if (artistRepository.count() == 0) {
            artistRepository.save(new Artist("Oscar", "2024-07-12", "20:00"));
            artistRepository.save(new Artist("Dua Lipa", "2024-07-13", "22:00"));
        }

        if (ticketRepository.count() == 0) {
            ticketRepository.save(new Ticket("general admission", 100, 250.0));
        }
    }
}

