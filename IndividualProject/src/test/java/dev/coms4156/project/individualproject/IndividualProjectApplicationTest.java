package dev.coms4156.project.individualproject;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

/**
 * Test to check if IndividualProjectApplication.main() runs without throwing exceptions.
 */
public class IndividualProjectApplicationTest {

  @Test
  public void main_test() {
    assertDoesNotThrow(() ->
        IndividualProjectApplication.main(
            new String[] {"--spring.main.web-application-type=none"}));
  }
}
