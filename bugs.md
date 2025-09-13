Bugs in IndividualProject/src/main/java/dev/coms4156/project/individualproject/model/Book.java:

1. In field `bookmarks`:
   - The bug was an unused private field `bookmarks`.
   - Fixed so that it is removed.

2. In deleteCopy():
   - The bug was returning `false` when successful deletion and `true` when there are no book copies.
   - Fixed so that it correctly returns `true` on successful deletion and else `false`.

3. In addCopy():
   - The bug was an empty implementation that didn't incremented copy counts.
   - Fixed so that it increments both `totalCopies` and `copiesAvailable`.

4. In checkoutCopy():
   - The bug was decrementing `amountOfTimesCheckedOut` on checkout.
   - Fixed so that it increments `amountOfTimesCheckedOut` for each checkout.

5. In returnCopy():
   - The bug was executing only when `returnDates` was empty, making returns impossible.
   - Fixed so that it executes when `returnDates` is not empty and returns `true` when a matching date is removed.

6. In hasCopies():
   - The bug was returning `true` even when `copiesAvailable == 0`.
   - Fixed so that it returns `true` only when `copiesAvailable > 0`.

7. In setShelvingLocation():
   - The bug was storing the string "shelvingLocation" instead of the provided value.
   - Fixed so that it stores the passed `shelvingLocation` argument.


Bugs in IndividualProject/src/main/java/dev/coms4156/project/individualproject/service/MockApiService.java:

1. In updateBook():
   - The bug was a no-op assignment `this.books = this.books;` and didn't apply updates.
   - Fixed so that `this.books` is replaced with the rebuilt `tmpBooks` which contains updated entry.

2. In constructor catch block:
   - The bug was an empty `catch` block that swallowed exceptions.
   - Fixed so that it logs an error message to `System.err`.

Bugs in IndividualProject/src/main/java/dev/coms4156/project/individualproject/controller/RouteController.java:

1. In getAvailableBooks():
   - The bug was returning the entire books list instead of only those with available copies.
   - Fixed so that it returns the computed `availableBooks` list.

2. In addCopy():
   - The bug was an unused local variable `currBookId`.
   - Fixed so that it is removed. 


