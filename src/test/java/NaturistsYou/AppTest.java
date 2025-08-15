package NaturistsYou;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Unit test for Spring Boot Application.
 */
@SpringBootTest
public class AppTest {
    
    /**
     * Test that the application context loads successfully.
     */
    @Test
    public void contextLoads() {
        // Spring Bootアプリケーションが正常に起動することをテスト
        Assertions.assertTrue(true);
    }
    
    /**
     * Basic application test.
     */
    @Test
    public void testApp() {
        // 基本的なアプリケーションテスト
        Assertions.assertTrue(true);
    }
}
