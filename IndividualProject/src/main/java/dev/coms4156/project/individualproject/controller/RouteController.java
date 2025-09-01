package dev.coms4156.project.individualproject.controller;

import org.springframework.web.bind.annotation.*;
import dev.coms4156.project.individualproject.model.BOOK;
import java.util.ArrayList;

import org.springframework.http.*;
import dev.coms4156.project.individualproject.service.MockAPIService;

@RestController
public class RouteController {

  private final MockAPIService mockApiService;

  public RouteController(MockAPIService mockApiService) {
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
    for (BOOK book : mockApiService.getBooks()) {
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
      ArrayList<BOOK> availableBooks = new ArrayList<>();

      for (BOOK book : mockApiService.getBooks()) {
        if (book.hasCopies()) {
          availableBooks.add(book);
        }
      }

      return new ResponseEntity<>(mockApiService.getBooks(), HttpStatus.OK);
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
      for (BOOK book : mockApiService.getBooks()) {
        StringBuilder currBookId = new StringBuilder(book.getId());
        if (bookId.equals(book.getId())) {
          book.addCopy();
          return new ResponseEntity<>(book, HttpStatus.OK);
        }
      }

      return new ResponseEntity<>("Book not found.", HttpStatus.I_AM_A_TEAPOT);
    } catch (Exception e) {
    }
  }

}
