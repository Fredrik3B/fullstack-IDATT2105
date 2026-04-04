package edu.ntnu.idatt2105.backend;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {

  @Test
  void contextLoads() {
  }

  @AfterAll
  static void shutdownMysqlCleanupThread() {
    try {
      AbandonedConnectionCleanupThread.checkedShutdown();
    } catch (Throwable ignored) {
      // Ignore: only needed when MySQL driver spawns a non-daemon cleanup thread.
    }
  }
}
