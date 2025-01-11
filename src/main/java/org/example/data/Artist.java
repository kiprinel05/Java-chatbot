package org.example.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "artists")
public class Artist {
    @Id
    private String name;
    private String performanceDate;
    private String performanceTime;  // ✅ Noua coloană adăugată

    public Artist() {}

    public Artist(String name, String performanceDate, String performanceTime) {
        this.name = name;
        this.performanceDate = performanceDate;
        this.performanceTime = performanceTime;
    }

    public String getName() { return name; }
    public String getPerformanceDate() { return performanceDate; }
    public String getPerformanceTime() { return performanceTime; }
}
