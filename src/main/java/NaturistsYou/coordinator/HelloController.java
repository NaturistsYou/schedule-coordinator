package NaturistsYou.coordinator;

import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

//@RestController
@Controller
public class HelloController {
    @Autowired
    private EventRepository eventRepository;

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
    public String createEventFromForm(@RequestParam String title) { // Standard
        Event event = new Event(title);                    // Custom
        eventRepository.save(event);                       // Standard
        return "redirect:/events";                          // Standard - 作成後一覧に戻る
    }
}
