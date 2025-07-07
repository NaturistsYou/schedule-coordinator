package NaturistsYou.coordinator;                   // Custom

import org.springframework.data.jpa.repository.JpaRepository; // Standard

public interface EventRepository extends JpaRepository<Event, Long> { // Standard
    // メソッドは自動生成される
}