package NaturistsYou.coordinator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    // 特定のイベントの参加者一覧を取得
    List<Participant> findByEventId(Long eventId);

    // 特定のイベントの特定の名前の参加者を検索（重複チェック用）
    Optional<Participant> findByEventIdAndName(Long eventId, String name);

    // 特定のイベントの参加者数をカウント
    @Query("SELECT COUNT(p) FROM Participant p WHERE p.event.id = :eventId")
    long countByEventId(@Param("eventId") Long eventId);
}