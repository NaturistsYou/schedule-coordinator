package NaturistsYou.coordinator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;

@RestController
public class HelloController {
    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/events")
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @PostMapping("/events")
    public Event createEvent() {
        Event event = new Event("テストイベント");
        return eventRepository.save(event);
    }
    @GetMapping("/")
    public String hello() {
        return "Hello, Schedule App!";
    }
}
