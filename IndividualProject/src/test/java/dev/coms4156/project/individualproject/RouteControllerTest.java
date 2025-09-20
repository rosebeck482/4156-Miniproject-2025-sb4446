package dev.coms4156.project.individualproject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.individualproject.controller.RouteController;
import dev.coms4156.project.individualproject.model.Book;
import dev.coms4156.project.individualproject.service.MockApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit and integration tests for RouteController.
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

  // Helper methods for testing getRecommendations()
  private void seedBooks(int n) {
    service.getBooks().clear();
    for (int i = 1; i <= n; i++) {
      service.getBooks().add(new Book("B" + i, i));
    }
  }

  private void checkoutRange(int startId, int endId) {
    for (int i = startId; i <= endId; i++) {
      Book b = service.getBooks().get(i - 1);
      if (b.getCopiesAvailable() <= 0) {
        b.addCopy();
      }
      b.checkoutCopy();
    }
  }

  @SuppressWarnings("unchecked")
  private java.util.List<Book> getRecommendedBooks() {
    ResponseEntity<?> resp = controller.getRecommendations();
    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertNotNull(resp.getBody());
    assertInstanceOf(java.util.List.class, resp.getBody());
    return (java.util.List<Book>) resp.getBody();
  }

  private java.util.Set<Integer> ids(java.util.List<Book> books) {
    return books.stream().map(Book::getId)
        .collect(java.util.stream.Collectors.toSet());
  }

  private java.util.Set<Integer> idsRange(int start, int end) {
    return java.util.stream.IntStream.rangeClosed(start, end).boxed()
        .collect(java.util.stream.Collectors.toSet());
  }

  /**
   * Tests getRecommendations() logic when there are books with same popularity.
   * Context: Creates 12 books and makes books 1-6 equally popular (1 checkout each).
   * Arguments: none.
   * Returns: 200 OK with exactly 10 unique books including top 5 by ID.
   * Data I/O: Creates 12 test books, checks out books 1-6 once each.
   */
  @Test
  public void getRecommendations_samePopularity_test() {
    seedBooks(12);
    checkoutRange(1, 6);
  
    var recommended = getRecommendedBooks();
    assertEquals(10, recommended.size());
  
    var recommendedIds = ids(recommended);
    assertEquals(10, recommendedIds.size());
    assertTrue(recommendedIds.containsAll(idsRange(1, 5)));
  }

  /**
   * Tests getRecommendations() randomness by checking if randomly selected books varies for runs.
   * Context: Creates 20 books with equal popularity (0 checkouts each).
   * Arguments: none.
   * Returns: 200 OK.
   * Data I/O: Creates 20 test books, calls endpoint 25 times to check randomness.
   */
  @Test
  public void getRecommendations_randomness_test() {
    seedBooks(20); // equally popular
    java.util.Set<String> seenRandomHalves = new java.util.HashSet<>();
    for (int i = 0; i < 25; i++) {
      var recommendedIds = ids(getRecommendedBooks());
      recommendedIds.removeAll(idsRange(1, 5)); 
      String key = new java.util.TreeSet<>(recommendedIds).toString(); 
      seenRandomHalves.add(key);
      if (seenRandomHalves.size() >= 2) {
        break;
      }
    }
    assertTrue(seenRandomHalves.size() >= 2, "Random half did not vary");
  }

  /**
   * Tests getRecommendations() boundary condition with exactly 10 unique books.
   * Context: Creates exactly 10 books, makes books 1-5 most popular.
   * Arguments: none.
   * Returns: 200 OK with all 10 books (5 popular + 5 random).
   * Data I/O: Creates 10 test books, checks out books 1-5 once each.
   */
  @Test
  public void getRecommendations_TenBooks_test() {
    seedBooks(10);
    checkoutRange(1, 5); 
  
    var recommended = getRecommendedBooks();
    assertEquals(10, recommended.size());
  
    var recommendedIds = ids(recommended);
    assertEquals(10, recommendedIds.size());
    assertTrue(recommendedIds.containsAll(idsRange(1, 5))); 
  }
  
}
