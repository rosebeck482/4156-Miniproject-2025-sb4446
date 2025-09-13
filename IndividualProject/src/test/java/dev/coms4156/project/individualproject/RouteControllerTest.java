package dev.coms4156.project.individualproject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.coms4156.project.individualproject.controller.RouteController;
import dev.coms4156.project.individualproject.model.Book;
import dev.coms4156.project.individualproject.service.MockApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for RouteController.
 */
public class RouteControllerTest {

  private RouteController controller;
  private MockApiService service;

  /**
   * Sets up test fixtures before each test.
   */
  @BeforeEach
  public void setup() {
    service = new MockApiService();
    controller = new RouteController(service);
    if (service.getBooks() == null || service.getBooks().isEmpty()) {
      service.getBooks().add(new Book("Seed Book", 1));
    }
  }

  /**
   * Tests both branches of getBook() (found vs. not found).
   * Context: MockApiService is preloaded with at least one Book.
   * Arguments: id path variable.
   * Returns: ResponseEntity with Book 200 when found, 404 otherwise.
   * Data I/O: Reads from MockApiService books list.
   */
  @Test
  public void getBook_test() {
    int existingId = service.getBooks().get(0).getId();
    int maxId = service.getBooks().stream().mapToInt(Book::getId).max().orElse(0);
    int nonExistingId = (maxId == Integer.MAX_VALUE) ? Integer.MIN_VALUE : maxId + 1;

    ResponseEntity<?> ok = controller.getBook(existingId);
    assertEquals(HttpStatus.OK, ok.getStatusCode());
    assertInstanceOf(Book.class, ok.getBody());

    ResponseEntity<?> missing = controller.getBook(nonExistingId);
    assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
  }

  /**
   * Tests getAvailableBooks() happy path.
   * Arguments: none.
   * Returns: 200 OK with list body.
   * Data I/O: Reads from MockApiService books list.
   */
  @Test
  public void getAvailableBooks_test() {
    ResponseEntity<?> resp = controller.getAvailableBooks();
    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertNotNull(resp.getBody());
  }

  /**
   * Tests all branches of addCopy().
   * Context: MockApiService is preloaded.
   * Args: bookId path variable.
   * Returns: 200 OK when id exists, 418 I_AM_A_TEAPOT when not found, 
   *          500 INTERNAL_SERVER_ERROR on exception.
   * Data I/O: Mutates Book via addCopy().
   */
  @Test
  public void addCopy_test() {
    int existingId = service.getBooks().get(0).getId();
    int maxId = service.getBooks().stream().mapToInt(Book::getId).max().orElse(0);
    int nonExistingId = (maxId == Integer.MAX_VALUE) ? Integer.MIN_VALUE : maxId + 1;

    ResponseEntity<?> ok = controller.addCopy(existingId);
    assertEquals(HttpStatus.OK, ok.getStatusCode());

    ResponseEntity<?> teapot = controller.addCopy(nonExistingId);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, teapot.getStatusCode());

    ResponseEntity<?> error = controller.addCopy(null);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getStatusCode());
  }
}
