package dev.coms4156.project.individualproject;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.coms4156.project.individualproject.model.BOOK;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This class contains the unit tests for the Book class.
 */
@SpringBootTest
public class BOOKUnitTests {

  public static BOOK book;

  @BeforeAll
  public static void setUpBookForTesting() {
    book = new BOOK("When Breath Becomes Air", 0);
  }

  @Test
  public void equalsBothAreTheSameTest() {
    BOOK cmpBook = book;
    assertEquals(cmpBook, book);
  }

}
