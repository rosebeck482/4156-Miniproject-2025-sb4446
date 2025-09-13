package dev.coms4156.project.individualproject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.individualproject.model.Book;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Book class.
 */
public class BookTest {

  private Book book;

  @BeforeEach
  public void setup() {
    book = new Book("Book Test", 111);
  }

  /**
   * Tests both branches of hasCopies().
   * Context: totalCopies=1, copiesAvailable = 1.
   * Arguments: none.
   * Returns: true when copiesAvailable >= 0, false when copiesAvailable < 0.
   * Data I/O: checkout decrements copiesAvailable to 0, reflection force copiesAvailable == -1.
   */
  @Test
  public void hasCopies_test() throws Exception {
    // Branch 1: copiesAvailable == 0
    book.checkoutCopy();
    assertEquals(0, book.getCopiesAvailable());
    assertFalse(book.hasCopies());

    // Branch 2: copiesAvailable < 0
    Field f = Book.class.getDeclaredField("copiesAvailable");
    f.setAccessible(true);
    f.setInt(book, -1);
    assertFalse(book.hasCopies());
  }

  /**
   * Tests both branches of deleteCopy().
   * Context: totalCopies=1, copiesAvailable=1.
   * Arguments: none.
   * Returns: First deleteCopy returns false (successful delete), 
   *          second deleteCopy with 0 copies returns true.
   * Data I/O: Decrements copiesAvailable to 0.
   */
  @Test
  public void deleteCopy_test() {
    // First delete (branch 1)
    boolean branch1 = book.deleteCopy();
    assertTrue(branch1);
    assertEquals(0, book.getCopiesAvailable());

    // Second delete (branch 2)
    boolean branch2 = book.deleteCopy();
    assertFalse(branch2);
    assertEquals(0, book.getCopiesAvailable());
  }


  /**
   * Tests both branches of checkoutCopy() (when a copy is available and when no copies are
   * available).
   * Context: totalCopies = 1 and copiesAvailable = 1.
   * Arguments: none.
   * Returns: First checkout returns due date string, second checkout with 0 copies returns null.
   * Data I/O: decrements copiesAvailable to 0, 1 due date added to returnDates.
   * Exceptions: none.
   */
  @Test
  public void checkoutCopy_test() {
    String due = book.checkoutCopy();
    // Branch 1 with 1 copy
    assertNotNull(due);
    assertEquals(0, book.getCopiesAvailable());
    assertEquals(1, book.getReturnDates().size());

    // Branch 2 with 0 copies
    assertNull(book.checkoutCopy());
  }

  /**
   * Tests both paths of returnCopy() (0 returnDates vs 1 matching due date).
   * Context: returnDates is initially empty.
   * Arguments: due date string.
   * Returns: both false.
   */
  @Test
  public void returnCopy_test() {
    // Branch 1: empty returnDates
    assertFalse(book.returnCopy("2020-11-11"));
    assertEquals(1, book.getCopiesAvailable());

    // Add due date
    String due = book.checkoutCopy();
    assertNotNull(due);
    assertEquals(0, book.getCopiesAvailable());

    // Branch 2: non-empty returnDates with matching due date
    assertTrue(book.returnCopy(due));
  }

}