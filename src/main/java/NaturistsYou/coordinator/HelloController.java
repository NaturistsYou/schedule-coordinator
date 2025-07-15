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
//@RestController
@Controller
public class HelloController {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventDateRepository eventDateRepository;

    @GetMapping("/events")
    @ResponseBody
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

//    @PostMapping("/events")
//    public Event createEvent() {
//        Event event = new Event("テストイベント");
//        return eventRepository.save(event);
//    }
    @GetMapping("/")
    @ResponseBody
    public String hello() {
        return "Hello, Schedule App!";
    }

//    @GetMapping("/events/new")                              // Standard
//    public String showEventForm() {                         // Custom
//        return "event-form";                                // Custom - event-form.htmlを表示
//    }

    @GetMapping("/events/new")
    public String showEventForm() {
        System.out.println("showEventForm メソッドが呼ばれました"); // Custom - デバッグ用
        return "event-form";
    }

    @PostMapping("/events")                                 // Standard
    public String createEventFromForm(
            @RequestParam String title,
            @RequestParam List<String> candidateDates) {

        System.out.println("受信したタイトル：" + title);
        System.out.println("受信した候補日：" + candidateDates);

//        イベントを作成
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
}
