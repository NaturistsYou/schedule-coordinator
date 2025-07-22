package NaturistsYou.coordinator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ResponseRepository extends JpaRepository<Response, Long> {

    // 特定の参加者の回答一覧を取得
    List<Response> findByParticipantId(Long participantId);

    // 特定の候補日への回答一覧を取得
    List<Response> findByEventDateId(Long eventDateId);

    // 特定のイベントの全回答を取得
    @Query("SELECT r FROM Response r WHERE r.participant.event.id = :eventId")
    List<Response> findByEventId(@Param("eventId") Long eventId);

    // 特定の候補日の特定回答種別の数をカウント
    @Query("SELECT COUNT(r) FROM Response r WHERE r.eventDate.id = :eventDateId AND r.responseType = :responseType")
    long countByEventDateIdAndResponseType(@Param("eventDateId") Long eventDateId, @Param("responseType") ResponseType responseType);

    // 特定の候補日の○の数を取得（最適日程判定用）
    @Query("SELECT COUNT(r) FROM Response r WHERE r.eventDate.id = :eventDateId AND r.responseType = 'OK'")
    long countOkResponsesByEventDateId(@Param("eventDateId") Long eventDateId);

    // 特定の参加者が特定の候補日に既に回答済みかチェック
    @Query("SELECT r FROM Response r WHERE r.participant.id = :participantId AND r.eventDate.id = :eventDateId")
    List<Response> findByParticipantIdAndEventDateId(@Param("participantId") Long participantId, @Param("eventDateId") Long eventDateId);
}