package NaturistsYou.coordinator;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventDateRepository extends JpaRepository<EventDate, Long> {
    // 基本的なCRUD操作は自動生成される
    // 必要に応じて後でカスタムメソッドを追加可能
}