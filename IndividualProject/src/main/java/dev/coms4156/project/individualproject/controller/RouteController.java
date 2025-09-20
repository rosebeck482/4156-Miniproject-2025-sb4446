package dev.coms4156.project.individualproject.controller;

import dev.coms4156.project.individualproject.model.Book;
import dev.coms4156.project.individualproject.service.MockApiService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for book operations.
 */
@RestController
public class RouteController {

  private final MockApiService mockApiService;

  public RouteController(MockApiService mockApiService) {
    this.mockApiService = mockApiService;
  }

  @GetMapping({"/", "/index"})
  public String index() {
    return "Welcome to the home page! In order to make an API call direct your browser"
        + "or Postman to an endpoint.";
  }

  /**
   * Returns the details of the specified book.
   *
   * @param id An {@code int} representing the unique identifier of the book to retrieve.
   *
   * @return A {@code ResponseEntity} containing either the matching {@code Book} object with an
   *         HTTP 200 response, or a message indicating that the book was not
   *         found with an HTTP 404 response.
   */
  @GetMapping({"/book/{id}"})
  public ResponseEntity<?> getBook(@PathVariable int id) {
    for (Book book : mockApiService.getBooks()) {
      if (book.getId() == id) {
        return new ResponseEntity<>(book, HttpStatus.OK);
      }
    }

    return new ResponseEntity<>("Book not found.", HttpStatus.NOT_FOUND);
  }

  /**
   * Get and return a list of all the books with available copies.
   *
   * @return A {@code ResponseEntity} containing a list of available {@code Book} objects with an
   *         HTTP 200 response if sucessful, or a message indicating an error occurred with an
   *         HTTP 500 response.
   */
  @PutMapping({"/books/available"})
  public ResponseEntity<?> getAvailableBooks() {
    try {
      ArrayList<Book> availableBooks = new ArrayList<>();

      for (Book book : mockApiService.getBooks()) {
        if (book.hasCopies()) {
          availableBooks.add(book);
        }
      }

      return new ResponseEntity<>(availableBooks, HttpStatus.OK);
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when getting all available books",
          HttpStatus.OK);
    }
  }

  /**
   * Adds a copy to the {@code} Book object if it exists.
   *
   * @param bookId An {@code Integer} representing the unique id of the book.
   * @return A {@code ResponseEntity} containing the updated {@code Book} object with an
   *         HTTP 200 response if successful or HTTP 404 if the book is not found,
   *         or a message indicating an error occurred with an HTTP 500 code.
   */
  @PatchMapping({"/book/{bookId}/add"})
  public ResponseEntity<?> addCopy(@PathVariable Integer bookId) {
    try {
      for (Book book : mockApiService.getBooks()) {
        if (bookId.equals(book.getId())) {
          book.addCopy();
          return new ResponseEntity<>(book, HttpStatus.OK);
        }
      }

      return new ResponseEntity<>("Book not found.", HttpStatus.I_AM_A_TEAPOT);
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when adding book.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Returns 10 unique recommended books (5 most popular by number of checkouts, 5 random books).
   *
   * @return A {@code ResponseEntity} containing list of 10 unique {@code Book} objects with an
   *         HTTP 200 if successful or HTTP status and error message indicating an error occurred.
   */
  @GetMapping({"/books/recommendation"})
  public ResponseEntity<?> getRecommendations() {
    final int recommendedBooksCount = 10;
    final int popularBooksCount = 5;
    try {

      List<Book> books = mockApiService.getBooks();
      if (books == null) {
        return new ResponseEntity<>("Getting books failed.",
            HttpStatus.INTERNAL_SERVER_ERROR);
      }

      // Make sure there are at least 10 unique books
      long uniqueBooks = books.stream()
          .map(Book::getId)
          .distinct()
          .count();
  
      if (uniqueBooks < recommendedBooksCount) {
        return new ResponseEntity<>(
            "Not enough unique books (need at least 10).",
            HttpStatus.INTERNAL_SERVER_ERROR
        );
      }
  
      // Copy of original list
      List<Book> originalListCopy = new ArrayList<>(books);
      // List of popular books
      List<Book> popularBooks = new ArrayList<>();
      // Set of selected ids for popular books
      Set<Integer> selectedIds = new HashSet<>();

      // Sort list of items by the number of times checked out in descending order
      // If number of times checked out is same, sort by id in ascending order
      originalListCopy.sort((b1, b2) -> {
        int numCheckoutsCompare = Integer.compare(b2.getAmountOfTimesCheckedOut(),
            b1.getAmountOfTimesCheckedOut());
        if (numCheckoutsCompare != 0) {
          return numCheckoutsCompare;
        }
        return Integer.compare(b1.getId(), b2.getId());
      });

      // Add top 5 popular books to list
      for (Book b : originalListCopy) {
        if (popularBooks.size() >= popularBooksCount) {
          break;
        }
        if (!selectedIds.contains(b.getId())) {
          popularBooks.add(b);
          selectedIds.add(b.getId());
        }
      }

      // Filter out selected popular books and shuffle and collect to a list
      List<Book> randomBooks = books.stream()
          .filter(b -> !selectedIds.contains(b.getId()))
          .collect(Collectors.toList());
  
      Collections.shuffle(randomBooks);

      List<Book> selectedRandomBooks = new ArrayList<>();
      for (Book b : randomBooks) {
        if (popularBooks.size() + selectedRandomBooks.size() >= recommendedBooksCount) {
          break;
        }
        if (!selectedIds.contains(b.getId())) {
          selectedRandomBooks.add(b);
          selectedIds.add(b.getId());
        }
      }

      // Make sure the total number of books is 10
      if (popularBooks.size() + selectedRandomBooks.size() != recommendedBooksCount) {
        return new ResponseEntity<>("Generated recommendations is not 10 books.",
            HttpStatus.INTERNAL_SERVER_ERROR);
      }

      List<Book> result = new ArrayList<>(recommendedBooksCount);
      result.addAll(popularBooks);
      result.addAll(selectedRandomBooks);

      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error while generating 10 book recommendations.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Check out copy of book.
   *
   * @param bookId An {@code int}, id of the book to check out.
   * @return A {@code ResponseEntity} containing updated {@code Book} with an
   *         HTTP 200 if successful, HTTP 404 if book is not found,
   *         HTTP 409 if no copy available, or a message indicating an error occurred with an
   *         HTTP 500 code.
   */
  @PatchMapping("/checkout")
  public ResponseEntity<?> checkout(@RequestParam("id") int bookId) {
    try {
      Book book = mockApiService.getBooks().stream()
          .filter(b -> b.getId() == bookId)
          .findFirst()
          .orElse(null);

      if (book == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("No book with id " + bookId + " found.");
      }

      String dueDate = book.checkoutCopy();
      if (dueDate == null) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body("No copy available to checkout for book with id " + bookId + ".");
      }

      return ResponseEntity.ok(book);
    } catch (Exception e) {
      System.err.println(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error occurred for check out of book with id " + bookId + ".");
    }
  }

}
