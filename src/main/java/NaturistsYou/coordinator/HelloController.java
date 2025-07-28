package NaturistsYou.coordinator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Comparator;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletRequest;

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
    public String showEventList(Model model) {
        List<Event> events = eventRepository.findAll();
        model.addAttribute("events", events);
        return "event-list";
    }

    @GetMapping("/api/events")
    @ResponseBody
    public List<Event> getAllEventsJson() {
        return eventRepository.findAll();
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/events";
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

    @GetMapping("/events/{id}")
    public String showEventDetail(@PathVariable Long id, Model model) {
        // データベースからIDに基づいてイベントを検索
        Optional<Event> eventOpt = eventRepository.findById(id);
        
        // イベントが存在しない場合は404エラーを投げる
        if (eventOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "イベントが見つかりません");
        }
        
        // イベントを取得
        Event event = eventOpt.get();
        
        // 候補日程を日付順でソート（早い日付から順番に）
        event.getEventDates().sort(Comparator.comparing(EventDate::getCandidateDate));
        
        // 各参加者の回答状況を計算
        Map<Long, Map<String, Object>> participantResponseStatus = new HashMap<>();
        for (Participant participant : event.getParticipants()) {
            Map<String, Object> status = calculateResponseStatus(participant, event);
            participantResponseStatus.put(participant.getId(), status);
        }
        
        // テンプレートにイベントデータを渡す
        model.addAttribute("event", event);
        model.addAttribute("participantResponseStatus", participantResponseStatus);
        return "event-detail";
    }

    @GetMapping("/events/{id}/participate")
    public String showParticipateForm(@PathVariable Long id, Model model) {
        // データベースからIDに基づいてイベントを検索
        Optional<Event> eventOpt = eventRepository.findById(id);
        
        // イベントが存在しない場合は404エラーを投げる
        if (eventOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "イベントが見つかりません");
        }
        
        // イベントを取得
        Event event = eventOpt.get();
        
        // 候補日程を日付順でソート
        event.getEventDates().sort(Comparator.comparing(EventDate::getCandidateDate));
        
        // テンプレートにイベントデータを渡す
        model.addAttribute("event", event);
        return "participate-form";
    }

    @PostMapping("/events/{id}/participate")
    public String processParticipation(@PathVariable Long id, 
                                     @RequestParam String participantName,
                                     HttpServletRequest request) {
        // データベースからIDに基づいてイベントを検索
        Optional<Event> eventOpt = eventRepository.findById(id);
        
        // イベントが存在しない場合は404エラーを投げる
        if (eventOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "イベントが見つかりません");
        }
        
        // イベントを取得
        Event event = eventOpt.get();
        
        // 新しい参加者を作成（Eventが最初、nameが2番目の順序）
        Participant participant = new Participant(event, participantName);
        
        // 参加者をデータベースに保存（IDを取得するため）
        Participant savedParticipant = participantRepository.save(participant);
        
        // 各候補日程への回答を処理
        for (EventDate eventDate : event.getEventDates()) {
            String responseParam = request.getParameter("response_" + eventDate.getId());
            String reasonParam = request.getParameter("reason_" + eventDate.getId());
            
            // 回答が選択されている場合のみ処理
            if (responseParam != null && !responseParam.trim().isEmpty()) {
                ResponseType responseType = ResponseType.valueOf(responseParam);
                
                // 新規回答の作成
                Response response = new Response(savedParticipant, eventDate, responseType, reasonParam);
                
                // データベースに保存
                responseRepository.save(response);
            }
        }
        
        // イベント詳細画面にリダイレクト
        return "redirect:/events/" + id;
    }

    @GetMapping("/events/{eventId}/participants/{participantId}/responses")
    public String showResponseForm(@PathVariable Long eventId, 
                                 @PathVariable Long participantId, 
                                 Model model) {
        // イベントの存在確認
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "イベントが見つかりません");
        }
        
        // 参加者の存在確認
        Optional<Participant> participantOpt = participantRepository.findById(participantId);
        if (participantOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "参加者が見つかりません");
        }
        
        Event event = eventOpt.get();
        Participant participant = participantOpt.get();
        
        // 候補日程を日付順でソート
        event.getEventDates().sort(Comparator.comparing(EventDate::getCandidateDate));
        
        // 既存の回答を取得してマップ化（候補日程ID → 回答）
        Map<Long, Response> existingResponses = new HashMap<>();
        for (Response response : participant.getResponses()) {
            if (response.getEventDate().getEvent().getId().equals(eventId)) {
                existingResponses.put(response.getEventDate().getId(), response);
            }
        }
        
        // テンプレートにデータを渡す
        model.addAttribute("event", event);
        model.addAttribute("participant", participant);
        model.addAttribute("existingResponses", existingResponses);
        
        return "response-form";
    }

    @PostMapping("/events/{eventId}/participants/{participantId}/responses")
    public String processResponses(@PathVariable Long eventId,
                                 @PathVariable Long participantId,
                                 HttpServletRequest request) {
        // イベントと参加者の存在確認
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Optional<Participant> participantOpt = participantRepository.findById(participantId);
        
        if (eventOpt.isEmpty() || participantOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "イベントまたは参加者が見つかりません");
        }
        
        Event event = eventOpt.get();
        Participant participant = participantOpt.get();
        
        // 既存の回答を取得
        Map<Long, Response> existingResponses = new HashMap<>();
        for (Response response : participant.getResponses()) {
            if (response.getEventDate().getEvent().getId().equals(eventId)) {
                existingResponses.put(response.getEventDate().getId(), response);
            }
        }
        
        // 各候補日程への回答を処理
        for (EventDate eventDate : event.getEventDates()) {
            String responseParam = request.getParameter("response_" + eventDate.getId());
            String reasonParam = request.getParameter("reason_" + eventDate.getId());
            
            // 回答が選択されている場合のみ処理
            if (responseParam != null && !responseParam.trim().isEmpty()) {
                ResponseType responseType = ResponseType.valueOf(responseParam);
                
                // 既存回答があれば更新、なければ新規作成
                Response response = existingResponses.get(eventDate.getId());
                if (response != null) {
                    // 既存回答の更新
                    response.setResponseType(responseType);
                    response.setReason(reasonParam != null ? reasonParam.trim() : null);
                } else {
                    // 新規回答の作成
                    response = new Response(participant, eventDate, responseType, reasonParam);
                }
                
                // データベースに保存
                responseRepository.save(response);
            }
        }
        
        // イベント詳細画面にリダイレクト
        return "redirect:/events/" + eventId;
    }

    // 参加者の回答状況を計算するヘルパーメソッド
    private Map<String, Object> calculateResponseStatus(Participant participant, Event event) {
        Map<String, Object> status = new HashMap<>();
        
        // このイベントの候補日程数
        int totalDates = event.getEventDates().size();
        
        // この参加者の回答を取得
        int responseCount = 0;
        int okCount = 0;
        int ngCount = 0;
        int maybeCount = 0;
        
        for (Response response : participant.getResponses()) {
            if (response.getEventDate().getEvent().getId().equals(event.getId())) {
                responseCount++;
                switch (response.getResponseType()) {
                    case OK:
                        okCount++;
                        break;
                    case NG:
                        ngCount++;
                        break;
                    case MAYBE:
                        maybeCount++;
                        break;
                }
            }
        }
        
        // 回答状況の判定
        String statusText;
        String statusClass;
        
        if (responseCount == 0) {
            statusText = "回答待ち";
            statusClass = "pending";
        } else if (responseCount == totalDates) {
            statusText = "回答完了";
            statusClass = "completed";
        } else {
            statusText = responseCount + "/" + totalDates + "件回答";
            statusClass = "partial";
        }
        
        status.put("statusText", statusText);
        status.put("statusClass", statusClass);
        status.put("responseCount", responseCount);
        status.put("totalDates", totalDates);
        status.put("okCount", okCount);
        status.put("ngCount", ngCount);
        status.put("maybeCount", maybeCount);
        
        return status;
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