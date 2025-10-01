package dev.coms4156.project.individualproject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.individualproject.controller.RouteController;
import dev.coms4156.project.individualproject.model.Book;
import dev.coms4156.project.individualproject.service.MockApiService;
import java.util.List;
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
   * Tests getAvailableBooks() excludes books having 0 copies.
   * Arguments: none.
   * Returns: 200 OK. Book with 0 copies excluded in response.
   * Data I/O: checkouts to make 0 copies for the first book, reads filtered list.
   */
  @Test
  public void getAvailableBooks_zeroCopy_test() {
    Book zeroCopyBook = service.getBooks().get(0);
    int copies = zeroCopyBook.getCopiesAvailable();
    for (int i = 0; i < copies; i++) {
      zeroCopyBook.checkoutCopy();
    }
    final int zeroCopyBookId = zeroCopyBook.getId();

    ResponseEntity<?> resp = controller.getAvailableBooks();
    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertInstanceOf(List.class, resp.getBody());

    List<?> raw = (List<?>) resp.getBody();
    List<Book> books = raw.stream()
        .map(Book.class::cast)
        .toList();

    List<Integer> ids = books.stream()
        .map(Book::getId)
        .toList();
    assertFalse(ids.contains(zeroCopyBookId));
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
   * Tests getRecommendations() when there are less than 10 books.
   * Context: Creates only 8 books when 10 are required.
   * Arguments: none.
   * Returns: 500 INTERNAL_SERVER_ERROR with error message.
   * Data I/O: Creates 5 test books, attempts to get recommendations.
   */
  @Test
  public void getRecommendations_lessThan10Books_test() {
    seedBooks(8);
    ResponseEntity<?> resp = controller.getRecommendations();
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
    assertTrue(resp.getBody().toString().contains("Not enough unique books (need at least 10)"));
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

  /**
   * Tests getRecommendations() path when getBooks() returns null.
   * Arguments: none.
   * Returns: 500 INTERNAL_SERVER_ERROR with error message.
   * Data I/O: none.
   */
  @Test
  public void getRecommendations_Getbooks_null_test() {
    MockApiService nullService = new MockApiService() {
      @Override
      public List<Book> getBooks() {
        return null;
      }
    };

    RouteController ctrl = new RouteController(nullService);
    ResponseEntity<?> resp = ctrl.getRecommendations();
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
    assertInstanceOf(String.class, resp.getBody());
    assertTrue(resp.getBody().toString().contains("Getting books failed."));
  }

  /**
   * Tests checkout() when book exists and has copies.
   * Context: Creates book with 2 copies.
   * Arguments: none.
   * Returns: 200 OK with updated book object.
   * Data I/O: Creates book, does checkout, checks field updates.
   */
  @Test
  public void checkout_test() {
    service.getBooks().clear();
    service.getBooks().add(new Book("NewBook", 111));
    // add copy so it has 2 copies
    service.getBooks().get(0).addCopy();
  
    Book beforeCheckout = service.getBooks().get(0);
    int copiesAvailable = beforeCheckout.getCopiesAvailable();
    final int checkoutNumber = beforeCheckout.getAmountOfTimesCheckedOut();
    int returnDates = beforeCheckout.getReturnDates().size();
  
    ResponseEntity<?> resp = controller.checkout(111);
  
    assertEquals(HttpStatus.OK, resp.getStatusCode());

    Book afterCheckout = (Book) resp.getBody();

    assertEquals(copiesAvailable - 1, afterCheckout.getCopiesAvailable());
    assertEquals(returnDates + 1, afterCheckout.getReturnDates().size());
    assertEquals(checkoutNumber + 1, afterCheckout.getAmountOfTimesCheckedOut());
  }

  /**
   * Tests checkout() with 3 checkouts until no copies left.
   * Context: Creates book with 2 copies.
   * Arguments: none.
   * Returns: 200 OK for first 2 checkouts, then for third checkout returns 409 CONFLICT.
   * Data I/O: Creates book, does 3 checkouts.
   */
  @Test
  public void checkout_3checkouts_test() {
    service.getBooks().clear();
    service.getBooks().add(new Book("NewBook", 111));
    Book b = service.getBooks().get(0);
    // make it to have 2 copies
    b.addCopy();
  
    assertEquals(HttpStatus.OK, controller.checkout(111).getStatusCode());
    assertEquals(HttpStatus.OK, controller.checkout(111).getStatusCode());
    assertEquals(HttpStatus.CONFLICT, controller.checkout(111).getStatusCode());
  }

  /**
   * Tests checkout() when book id doesn't exist.
   * Context: Creates book but tries checkout with ID that doesn't exist.
   * Arguments: none.
   * Returns: 404 NOT_FOUND with error message.
   * Data I/O: Creates book, attempts checkout with invalid ID.
   */
  @Test
  public void checkout_noId_test() {
    service.getBooks().clear();
    service.getBooks().add(new Book("NewBook", 111));
  
    ResponseEntity<?> resp = controller.checkout(10000);
    assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    assertInstanceOf(String.class, resp.getBody());
  }  

  /**
   * Tests checkout() when book has 0 copies.
   * Context: Creates book and makes it have 0 copies.
   * Arguments: none.
   * Returns: 409 CONFLICT with error message.
   * Data I/O: Creates book, makes it to have 0 copies, tries checkout.
   */
  @Test
  public void checkout_0copies_test() {
    // 1 copy
    service.getBooks().clear();
    service.getBooks().add(new Book("NewBook", 111));
    // check out to make it to have 0 copies
    service.getBooks().get(0).checkoutCopy();
  
    ResponseEntity<?> resp = controller.checkout(111);
    assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
    assertInstanceOf(String.class, resp.getBody());
  }
}
