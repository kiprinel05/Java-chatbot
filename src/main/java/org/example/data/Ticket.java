package org.example.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    private String type;
    private int available;
    private double price;

    public Ticket() {}

    public Ticket(String type, int available, double price) {
        this.type = type;
        this.available = available;
        this.price = price;
    }

    public String getType() { return type; }
    public int getAvailable() { return available; }
    public double getPrice() { return price; }
}
