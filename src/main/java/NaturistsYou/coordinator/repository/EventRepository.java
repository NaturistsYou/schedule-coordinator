package NaturistsYou.coordinator.repository;

import NaturistsYou.coordinator.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface EventRepository extends JpaRepository<Event, Long> {
    // 有効なイベント（少なくとも1つの候補日が今日以降のイベント）を取得
    @Query("SELECT DISTINCT e FROM Event e " +
           "LEFT JOIN e.eventDates ed " +
           "WHERE ed.candidateDate >= :today " +
           "ORDER BY e.createdAt DESC")
    Page<Event> findActiveEvents(@Param("today") LocalDate today, Pageable pageable);
}
