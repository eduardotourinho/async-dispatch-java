package dev.eduardo.scalable_test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TaskManagerTestcontainersConfiguration.class)
class TaskManagerApplicationTests {

    @Test
    void contextLoads() {
    }
}
