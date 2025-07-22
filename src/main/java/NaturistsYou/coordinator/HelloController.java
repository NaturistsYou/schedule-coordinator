package NaturistsYou.coordinator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;

@Controller
public class HelloController {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventDateRepository eventDateRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private ResponseRepository responseRepository;

    @GetMapping("/events")
    @ResponseBody
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @GetMapping("/")
    @ResponseBody
    public String hello() {
        return "Hello, Schedule App!";
    }

    @GetMapping("/events/new")
    public String showEventForm() {
        System.out.println("showEventForm メソッドが呼ばれました");
        return "event-form";
    }

    @PostMapping("/events")
    public String createEventFromForm(
            @RequestParam String title,                    // ← カンマを追加
            @RequestParam List<String> candidateDates) {   // ← 型宣言を修正

        System.out.println("受信したタイトル：" + title);
        System.out.println("受信した候補日：" + candidateDates);

        // イベントを作成
        Event event = new Event(title);
        Event savedEvent = eventRepository.save(event);

        // 候補日程を保存（空でない日付のみ）
        for (String dateStr : candidateDates) {
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                LocalDate date = LocalDate.parse(dateStr);
                EventDate eventDate = new EventDate(savedEvent, date);
                eventDateRepository.save(eventDate);
            }
        }

        return "redirect:/events";
    }

    // テスト用：データベーステーブル確認
    @GetMapping("/test/tables")
    @ResponseBody
    public String testTables() {
        long eventCount = eventRepository.count();
        long eventDateCount = eventDateRepository.count();
        long participantCount = participantRepository.count();
        long responseCount = responseRepository.count();

        return String.format(
                "📊 データベーステーブル確認\n" +
                        "Events: %d 件\n" +
                        "EventDates: %d 件\n" +
                        "Participants: %d 件\n" +
                        "Responses: %d 件\n" +
                        "✅ 全テーブル正常作成済み",
                eventCount, eventDateCount, participantCount, responseCount
        );
    }
}