package NaturistsYou.coordinator;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;

@Entity
public class Response {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 誰の回答かを示す
    @ManyToOne
    @JoinColumn(name = "participant_id")
    @JsonBackReference("participant-responses")
    private Participant participant;

    // どの候補日への回答かを示す
    @ManyToOne
    @JoinColumn(name = "event_date_id")
    @JsonBackReference("eventdate-responses")
    private EventDate eventDate;

    // 回答種別（○×△）
    @Enumerated(EnumType.STRING)
    private ResponseType responseType;

    // 回答日時
    private LocalDateTime createdAt;

    // コンストラクタ
    public Response() {
    }

    public Response(Participant participant, EventDate eventDate, ResponseType responseType) {
        this.participant = participant;
        this.eventDate = eventDate;
        this.responseType = responseType;
        this.createdAt = LocalDateTime.now();
    }

    // Getter・Setter
    public Long getId() {
        return id;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public EventDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(EventDate eventDate) {
        this.eventDate = eventDate;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // 便利メソッド：回答の記号を取得
    public String getSymbol() {
        return responseType != null ? responseType.getSymbol() : "-";
    }

    // 便利メソッド：回答の説明を取得
    public String getDescription() {
        return responseType != null ? responseType.getDescription() : "未回答";
    }
}