package NaturistsYou.coordinator;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private LocalDateTime createdAt;

    // 候補日程のリスト（1つのイベントに複数の候補日）
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<EventDate> eventDates = new ArrayList<>();

    // コンストラクタ
    public Event() {
    }

    public Event(String title) {
        this.title = title;
        this.createdAt = LocalDateTime.now();
    }

    // Getter・Setter
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<EventDate> getEventDates() {
        return eventDates;
    }

    public void setEventDates(List<EventDate> eventDates) {
        this.eventDates = eventDates;
    }

    // 候補日を追加するためのヘルパーメソッド
    public void addEventDate(EventDate eventDate) {
        eventDates.add(eventDate);
        eventDate.setEvent(this);
    }

}