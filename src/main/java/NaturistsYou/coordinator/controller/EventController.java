package NaturistsYou.coordinator.controller;

import NaturistsYou.coordinator.entity.Event;
import NaturistsYou.coordinator.entity.EventDate;
import NaturistsYou.coordinator.entity.Participant;
import NaturistsYou.coordinator.entity.Response;
import NaturistsYou.coordinator.enums.ResponseType;
import NaturistsYou.coordinator.repository.EventRepository;
import NaturistsYou.coordinator.repository.EventDateRepository;
import NaturistsYou.coordinator.repository.ParticipantRepository;
import NaturistsYou.coordinator.repository.ResponseRepository;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class EventController {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventDateRepository eventDateRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private ResponseRepository responseRepository;

    @GetMapping("/events")
    public String showEventList(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        // 1ページ5件でページネーション（ソートはクエリ内で実施）
        Pageable pageable = PageRequest.of(page, 5);
        // 今日以降の候補日を持つイベントのみ取得（過去のイベントは除外）
        Page<Event> eventPage = eventRepository.findActiveEvents(LocalDate.now(), pageable);

        model.addAttribute("eventPage", eventPage);
        model.addAttribute("events", eventPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventPage.getTotalPages());
        model.addAttribute("totalItems", eventPage.getTotalElements());

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

        return "redirect:/events/" + savedEvent.getId() + "/created";
    }

    @GetMapping("/events/{id}/created")
    public String showEventCreated(@PathVariable Long id, Model model, HttpServletRequest request) {
        // データベースからIDに基づいてイベントを検索
        Optional<Event> eventOpt = eventRepository.findById(id);

        // イベントが存在しない場合は404エラーを投げる
        if (eventOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "イベントが見つかりません");
        }

        // イベントを取得
        Event event = eventOpt.get();

        // 招待URLを生成（現在のサーバーのベースURLを使用）
        String baseUrl = request.getRequestURL().toString().replaceFirst("/events/.*", "");
        String inviteUrl = baseUrl + "/events/" + id + "/participate";

        // テンプレートにイベントデータと招待URLを渡す
        model.addAttribute("event", event);
        model.addAttribute("inviteUrl", inviteUrl);
        return "event-created";
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

        // 日程集計結果を計算
        Map<Long, Map<String, Object>> dateAggregationResults = calculateDateAggregation(event);

        // 最適日程を提案
        Map<String, Object> optimalDateSuggestion = suggestOptimalDate(event, dateAggregationResults);

        // テンプレートにイベントデータを渡す
        model.addAttribute("event", event);
        model.addAttribute("participantResponseStatus", participantResponseStatus);
        model.addAttribute("dateAggregationResults", dateAggregationResults);
        model.addAttribute("optimalDateSuggestion", optimalDateSuggestion);
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

    // 日程集計機能：各候補日程の回答を集計
    private Map<Long, Map<String, Object>> calculateDateAggregation(Event event) {
        Map<Long, Map<String, Object>> aggregationResults = new HashMap<>();

        for (EventDate eventDate : event.getEventDates()) {
            Map<String, Object> dateStats = new HashMap<>();

            // 回答カウント
            int okCount = 0;
            int maybeCount = 0;
            int ngCount = 0;
            int noResponseCount = 0;

            // 参加者名リスト
            List<String> okParticipants = new ArrayList<>();
            List<String> maybeParticipants = new ArrayList<>();
            List<String> ngParticipants = new ArrayList<>();
            List<String> noResponseParticipants = new ArrayList<>();

            // 全参加者をチェック
            for (Participant participant : event.getParticipants()) {
                Response response = findResponseForEventDate(participant, eventDate);

                if (response != null) {
                    switch (response.getResponseType()) {
                        case OK:
                            okCount++;
                            okParticipants.add(participant.getName());
                            break;
                        case MAYBE:
                            maybeCount++;
                            maybeParticipants.add(participant.getName());
                            break;
                        case NG:
                            ngCount++;
                            ngParticipants.add(participant.getName());
                            break;
                    }
                } else {
                    noResponseCount++;
                    noResponseParticipants.add(participant.getName());
                }
            }

            // 統計計算
            int totalParticipants = event.getParticipants().size();
            int responseCount = okCount + maybeCount + ngCount;
            int potentialParticipants = okCount + maybeCount; // 参加可能者数（○+△）

            double responseRate = totalParticipants > 0 ? (double) responseCount / totalParticipants * 100 : 0;
            double participationRate = totalParticipants > 0 ? (double) potentialParticipants / totalParticipants * 100 : 0;

            // 結果をマップに格納
            dateStats.put("okCount", okCount);
            dateStats.put("maybeCount", maybeCount);
            dateStats.put("ngCount", ngCount);
            dateStats.put("noResponseCount", noResponseCount);
            dateStats.put("totalParticipants", totalParticipants);
            dateStats.put("responseCount", responseCount);
            dateStats.put("potentialParticipants", potentialParticipants);
            dateStats.put("responseRate", Math.round(responseRate));
            dateStats.put("participationRate", Math.round(participationRate));

            dateStats.put("okParticipants", okParticipants);
            dateStats.put("maybeParticipants", maybeParticipants);
            dateStats.put("ngParticipants", ngParticipants);
            dateStats.put("noResponseParticipants", noResponseParticipants);

            aggregationResults.put(eventDate.getId(), dateStats);
        }

        return aggregationResults;
    }

    // 最適日程提案機能
    private Map<String, Object> suggestOptimalDate(Event event, Map<Long, Map<String, Object>> aggregationResults) {
        Map<String, Object> suggestion = new HashMap<>();

        if (event.getEventDates().isEmpty() || aggregationResults.isEmpty()) {
            suggestion.put("hasOptimalDate", false);
            suggestion.put("message", "候補日程がありません");
            return suggestion;
        }

        EventDate optimalDate = null;
        Map<String, Object> optimalStats = null;
        int maxOkCount = -1;
        int maxPotentialParticipants = -1;

        // 最適日程を探す（○の数 → ○+△の数 → 日付の早い順）
        for (EventDate eventDate : event.getEventDates()) {
            Map<String, Object> stats = aggregationResults.get(eventDate.getId());
            int okCount = (Integer) stats.get("okCount");
            int potentialParticipants = (Integer) stats.get("potentialParticipants");

            boolean isCurrentBetter = false;

            if (okCount > maxOkCount) {
                isCurrentBetter = true;
            } else if (okCount == maxOkCount && potentialParticipants > maxPotentialParticipants) {
                isCurrentBetter = true;
            } else if (okCount == maxOkCount && potentialParticipants == maxPotentialParticipants) {
                // 同じ条件なら日付の早い方を選択
                if (optimalDate == null || eventDate.getCandidateDate().isBefore(optimalDate.getCandidateDate())) {
                    isCurrentBetter = true;
                }
            }

            if (isCurrentBetter) {
                optimalDate = eventDate;
                optimalStats = stats;
                maxOkCount = okCount;
                maxPotentialParticipants = potentialParticipants;
            }
        }

        if (optimalDate != null) {
            suggestion.put("hasOptimalDate", true);
            suggestion.put("optimalDate", optimalDate);
            suggestion.put("optimalStats", optimalStats);

            // 提案メッセージを生成
            String message = String.format("最適日程: %s（○%d人 △%d人 - 参加可能率%d%%）",
                optimalDate.getCandidateDate(),
                (Integer) optimalStats.get("okCount"),
                (Integer) optimalStats.get("maybeCount"),
                ((Number) optimalStats.get("participationRate")).intValue()
            );
            suggestion.put("message", message);
        } else {
            suggestion.put("hasOptimalDate", false);
            suggestion.put("message", "最適日程を特定できませんでした");
        }

        return suggestion;
    }

    // ヘルパーメソッド：参加者の特定の候補日程への回答を取得
    private Response findResponseForEventDate(Participant participant, EventDate eventDate) {
        for (Response response : participant.getResponses()) {
            if (response.getEventDate().getId().equals(eventDate.getId())) {
                return response;
            }
        }
        return null;
    }

    @GetMapping("/events/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
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
        return "event-edit";
    }

    @PostMapping("/events/{id}")
    @Transactional
    public String updateEvent(@PathVariable Long id,
                            @RequestParam String title,
                            @RequestParam(required = false) List<String> candidateDates,
                            @RequestParam(required = false) List<Long> existingDateIds) {

        System.out.println("=== イベント更新処理開始 ===");
        System.out.println("イベントID: " + id);
        System.out.println("タイトル: " + title);
        System.out.println("チェックされた既存日程ID: " + existingDateIds);
        System.out.println("新規追加日程: " + candidateDates);

        // データベースからIDに基づいてイベントを検索
        Optional<Event> eventOpt = eventRepository.findById(id);

        // イベントが存在しない場合は404エラーを投げる
        if (eventOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "イベントが見つかりません");
        }

        Event event = eventOpt.get();

        System.out.println("現在の候補日程数: " + event.getEventDates().size());
        for (EventDate ed : event.getEventDates()) {
            System.out.println("  - ID: " + ed.getId() + ", 日付: " + ed.getCandidateDate());
        }

        // イベントタイトルを更新
        event.setTitle(title);
        eventRepository.save(event);

        // 削除対象のEventDateを特定して削除
        List<Long> idsToDelete = new ArrayList<>();

        if (existingDateIds != null && !existingDateIds.isEmpty()) {
            // チェックされていない候補日程のIDを特定
            for (EventDate eventDate : event.getEventDates()) {
                if (!existingDateIds.contains(eventDate.getId())) {
                    idsToDelete.add(eventDate.getId());
                    System.out.println("削除対象: ID=" + eventDate.getId() + ", 日付=" + eventDate.getCandidateDate());
                }
            }
        } else {
            // existingDateIdsが空の場合、すべての既存候補日程を削除
            System.out.println("チェックがすべて外されたため、全候補日程を削除します");
            for (EventDate eventDate : event.getEventDates()) {
                idsToDelete.add(eventDate.getId());
                System.out.println("削除対象: ID=" + eventDate.getId() + ", 日付=" + eventDate.getCandidateDate());
            }
        }

        // 削除を実行
        for (Long idToDelete : idsToDelete) {
            eventDateRepository.deleteById(idToDelete);
            System.out.println("削除実行: ID=" + idToDelete);
        }

        // イベントエンティティのリレーションから削除されたEventDateを除去
        event.getEventDates().removeIf(ed -> idsToDelete.contains(ed.getId()));
        System.out.println("削除後の候補日程数: " + event.getEventDates().size());

        // 新しい候補日程を追加
        if (candidateDates != null) {
            for (String dateStr : candidateDates) {
                if (dateStr != null && !dateStr.trim().isEmpty()) {
                    LocalDate date = LocalDate.parse(dateStr);
                    EventDate eventDate = new EventDate(event, date);
                    eventDateRepository.save(eventDate);
                    eventDateRepository.flush();
                    System.out.println("新規追加完了: " + date);
                }
            }
        }

        System.out.println("=== イベント更新処理完了 ===");

        return "redirect:/events/" + id;
    }

    @PostMapping("/events/{id}/delete")
    @Transactional
    public String deleteEvent(@PathVariable Long id) {
        // データベースからIDに基づいてイベントを検索
        Optional<Event> eventOpt = eventRepository.findById(id);

        // イベントが存在しない場合は404エラーを投げる
        if (eventOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "イベントが見つかりません");
        }

        // イベントを削除（CascadeType.ALLにより関連データも自動削除される）
        eventRepository.deleteById(id);

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
