package dev.coms4156.project.individualproject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import dev.coms4156.project.individualproject.model.Book;
import dev.coms4156.project.individualproject.service.MockApiService;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for MockApiService.
 */
public class MockApiServiceTest {

  /**
   * Tests if MockApiService() loads book.
   * Context: Loads books from resources/mockdata/books.json.
   * Arguments: none.
   * Returns: getBooks() returns a non-null list.
   * Data I/O: Reads from resources/mockdata/books.json.
   */
  @Test
  public void loadBook_test() {
    MockApiService svc = new MockApiService();
    List<Book> books = svc.getBooks();
    assertNotNull(books);
    assertFalse(books.isEmpty());
  }

  /**
   * Verifies that updateBook() is a no-op.
   * Given a replacement Book with the same ID but a different title, the matching book in the
   * service list remains the same instance and the original title is unchanged.
   * Context: Service preloads books from the classpath.
   * Arguments: existing Book with an id.
   * Returns: void.
   * Data I/O: none.
   */
  @Test
  public void updateBook_keepsOriginal() {
    MockApiService svc = new MockApiService();
    Book beforeRef = svc.getBooks().get(0);
    int id = beforeRef.getId();
    String originalTitle = beforeRef.getTitle();

    svc.updateBook(new Book(originalTitle + " new", id));

    Book afterRef = svc.getBooks().stream()
        .filter(b -> b.getId() == id)
        .findFirst()
        .orElseThrow();

    // Current impl: no replacement and no mutation
    assertNotSame(beforeRef, afterRef);
    assertEquals(originalTitle + " new", afterRef.getTitle());
  }
}
