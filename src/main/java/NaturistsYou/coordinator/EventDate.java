package NaturistsYou.coordinator;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class EventDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // どのイベントの候補日かを示す
    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonBackReference
    private Event event;

    // 候補日（例：2025-07-20）
    private LocalDate candidateDate;

    // コンストラクタ
    public EventDate() {
    }

    public EventDate(Event event, LocalDate candidateDate) {
        this.event = event;
        this.candidateDate = candidateDate;
    }

    // Getter・Setter
    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public LocalDate getCandidateDate() {
        return candidateDate;
    }

    public void setCandidateDate(LocalDate candidateDate) {
        this.candidateDate = candidateDate;
    }
}