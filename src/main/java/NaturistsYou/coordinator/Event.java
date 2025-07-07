package NaturistsYou.coordinator;                   // Custom

import jakarta.persistence.Entity;                  // Standard
import jakarta.persistence.GeneratedValue;          // Standard
import jakarta.persistence.GenerationType;          // Standard
import jakarta.persistence.Id;                      // Standard
import java.time.LocalDateTime;                     // Standard

@Entity                                             // Standard
public class Event {                                // Custom

    @Id                                             // Standard
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Standard
    private Long id;                                // Custom

    private String title;                           // Custom
    private LocalDateTime createdAt;                // Custom

    // コンストラクタ（Standard）
    public Event() {                                // Custom
    }

    public Event(String title) {                    // Custom
        this.title = title;                         // Custom
        this.createdAt = LocalDateTime.now();       // Standard
    }

    // Getter・Setter（Standard）
    public Long getId() {                           // Custom
        return id;                                  // Custom
    }

    public String getTitle() {                      // Custom
        return title;                               // Custom
    }

    public void setTitle(String title) {            // Custom
        this.title = title;                         // Custom
    }

    public LocalDateTime getCreatedAt() {           // Custom
        return createdAt;                           // Custom
    }
}
