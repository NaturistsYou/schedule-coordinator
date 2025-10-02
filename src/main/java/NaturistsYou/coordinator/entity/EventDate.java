package NaturistsYou.coordinator.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
public class EventDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JSON循環参照を防ぐアノテーション追加
    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonBackReference
    private Event event;

    // 候補日（例：2025-07-20）
    private LocalDate candidateDate;

    // この候補日への回答リスト
    @OneToMany(mappedBy = "eventDate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("eventdate-responses")
    private List<Response> responses = new ArrayList<>();

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

    public List<Response> getResponses() {
        return responses;
    }

    public void setResponses(List<Response> responses) {
        this.responses = responses;
    }

    // 回答を追加するためのヘルパーメソッド
    public void addResponse(Response response) {
        responses.add(response);
        response.setEventDate(this);
    }
}
