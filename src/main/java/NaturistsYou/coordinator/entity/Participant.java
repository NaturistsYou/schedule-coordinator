package NaturistsYou.coordinator.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // どのイベントの参加者かを示す
    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonBackReference("event-participants")
    private Event event;

    // 参加者名（例：田中太郎）
    private String name;

    // 回答作成日時
    private LocalDateTime createdAt;

    // この参加者の回答リスト（○×△）
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("participant-responses")
    private List<Response> responses = new ArrayList<>();

    // コンストラクタ
    public Participant() {
    }

    public Participant(Event event, String name) {
        this.event = event;
        this.name = name;
        this.createdAt = LocalDateTime.now();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
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
        response.setParticipant(this);
    }
}
