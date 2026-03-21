package com.removebg;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// This is a true integration smoke test. It should run only when the project
// has a dedicated test datasource or Testcontainers configuration.
@Disabled("Requires integration-test infrastructure and should not run in the unit-test suite.")
@SpringBootTest
class RemovebgApplicationTests {

    @Test
    void contextLoads() {
    }

}
