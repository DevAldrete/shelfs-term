package com.devaldrete.services;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.devaldrete.domain.Administrator;
import com.devaldrete.domain.BookDefinition;
import com.devaldrete.domain.BookItem;
import com.devaldrete.domain.Loan;
import com.devaldrete.domain.Member;
import com.devaldrete.domain.Role;
import com.devaldrete.domain.User;

public class Library {

  private String id;
  private String name;
  private String address;

  private final BookService bookService;
  private final UserService userService;
  private final LoanService loanService;

  /**
   * Creates the library and wires the shared service instances so that all
   * three services operate on the same in-memory data stores.
   */
  public Library(String name, String address) {
    this.id = UUID.randomUUID().toString();
    this.name = name;
    this.address = address;
    this.bookService = new BookService();
    this.userService = new UserService();
    // Inject shared services into LoanService so it sees the same users/books
    this.loanService = new LoanService(userService, bookService);
  }

  // --- Getters / Setters ---

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  // --- Service accessors ---

  public UserService getUserService() {
    return userService;
  }

  public BookService getBookService() {
    return bookService;
  }

  public LoanService getLoanService() {
    return loanService;
  }

  // --- RBAC helpers ---

  public boolean canManageBooks(User user) {
    return user instanceof Administrator;
  }

  public boolean canManageUsers(User user) {
    return user instanceof Administrator;
  }

  public boolean canViewAllLoans(User user) {
    return user instanceof Administrator;
  }

  public boolean canUpgradeUserRole(User user) {
    return user instanceof Administrator;
  }

  // --- User delegation ---

  /**
   * Saves a pre-constructed User directly (used for seeding initial data).
   */
  public void addUser(User user) {
    userService.save(user);
  }

  // --- Loan delegation ---

  public Loan loanBook(String userId, String barcode) {
    return loanService.loanBook(userId, barcode);
  }

  public boolean returnBook(String loanId) {
    return loanService.returnBook(loanId);
  }

  public List<Loan> findAllLoans() {
    return loanService.getAll();
  }

  public List<Loan> findLoansByUserId(String userId) {
    return loanService.getByUserId(userId);
  }

  public List<Loan> findOverdueLoans() {
    return loanService.getOverdue();
  }

  /**
   * Returns the loans accessible to the given user:
   * - Administrators see all loans.
   * - Members see only their own loans.
   */
  public List<Loan> getAccessibleLoans(User user) {
    if (user instanceof Administrator) {
      return loanService.getAll();
    }
    if (user instanceof Member) {
      return ((Member) user).getAccessibleLoans(loanService.getAll());
    }
    return List.of();
  }

  // --- Quick overview ---

  /**
   * Returns a summary of library counts keyed by label.
   */
  public HashMap<String, Integer> quickOverview() {
    HashMap<String, Integer> overview = new HashMap<>();
    overview.put("Book titles", bookService.countDefinitions());
    overview.put("Book items", bookService.countItems());
    overview.put("Users", userService.count());
    overview.put("Active loans", loanService.count());
    overview.put("Overdue loans", loanService.getOverdue().size());
    return overview;
  }

  // --- Book menu ---

  public void manageBooks() {
    IO.println("\n=== Manage Books ===\n");
    IO.println("1. Add Book");
    IO.println("2. Remove Book Item");
    IO.println("3. List All Books");
    IO.println("4. Search Available Copies by ISBN");
    IO.println("5. Update Book Info");
    IO.println("6. Back to Main Menu");
    String option = IO.readln("Choose an option: ");

    switch (option) {
      case "1":
        IO.println("\n=== Add Book ===\n");
        String isbn = IO.readln("Enter ISBN: ");
        BookDefinition existing = bookService.findByISBN(isbn);
        if (existing != null) {
          IO.println("Book with ISBN " + isbn + " already exists. Adding a new copy.");
          bookService.addBookItem(existing.getId());
          IO.println("Copy added successfully.");
        } else {
          String title = IO.readln("Enter book title: ");
          String author = IO.readln("Enter book author: ");
          String publisher = IO.readln("Enter book publisher: ");
          bookService.addBook(isbn, title, author, publisher);
          IO.println("New book added successfully.");
        }
        break;

      case "2":
        IO.println("\n=== Remove Book Item ===\n");
        String barcode = IO.readln("Enter barcode of the book item to remove: ");
        boolean removed = bookService.removeBookItem(barcode);
        if (removed) {
          IO.println("Book item removed successfully.");
        } else {
          IO.println("Book item with barcode " + barcode + " not found.");
        }
        break;

      case "3":
        IO.println("\n=== All Books ===\n");
        List<BookDefinition> allDefs = bookService.getAllDefinitions();
        if (allDefs.isEmpty()) {
          IO.println("No books in the library.");
          break;
        }
        for (BookDefinition def : allDefs) {
          IO.println("ID: " + def.getId());
          IO.println("Title: " + def.getTitle());
          IO.println("Author: " + def.getAuthor());
          IO.println("ISBN: " + def.getIsbn());
          IO.println("Publisher: " + def.getPublisher());
          IO.println("-----------------------");
        }
        break;

      case "4":
        IO.println("\n=== Available Copies by ISBN ===\n");
        String searchIsbn = IO.readln("Enter ISBN to search: ");
        BookDefinition def = bookService.findByISBN(searchIsbn);
        if (def == null) {
          IO.println("No book found with ISBN " + searchIsbn + ".");
          break;
        }
        List<BookItem> available = bookService.getAvailableCopies(searchIsbn);
        IO.println("Title: " + def.getTitle());
        if (available.isEmpty()) {
          IO.println("No available copies.");
          break;
        }
        for (BookItem item : available) {
          IO.println("Barcode: " + item.getBarcode() + " | Status: " + item.getStatus());
          IO.println("-----------------------");
        }
        break;

      case "5":
        IO.println("\n=== Update Book Info ===\n");
        String updateIsbn = IO.readln("Enter ISBN of the book to update: ");
        BookDefinition current = bookService.findByISBN(updateIsbn);
        if (current == null) {
          IO.println("Book with ISBN " + updateIsbn + " not found.");
          break;
        }
        String newTitle = IO.readln("New title (current: " + current.getTitle() + "): ");
        String newAuthor = IO.readln("New author (current: " + current.getAuthor() + "): ");
        String newPublisher = IO.readln("New publisher (current: " + current.getPublisher() + "): ");
        bookService.updateDefinition(updateIsbn, newTitle, newAuthor, newPublisher);
        IO.println("Book updated successfully.");
        break;

      case "6":
        IO.println("\nReturning to Main Menu.");
        break;

      default:
        IO.println("\nInvalid option. Please select a valid option (1-6).");
        break;
    }
  }

  // --- User menu ---

  public void manageUsers() {
    IO.println("\n=== Manage Users ===\n");
    IO.println("1. Register User");
    IO.println("2. List Users");
    IO.println("3. Update User");
    IO.println("4. Upgrade to Administrator");
    IO.println("5. Remove User");
    IO.println("6. Back to Main Menu");
    String option = IO.readln("Choose an option: ");

    switch (option) {
      case "1":
        IO.println("\n=== Register New User ===\n");
        String username = IO.readln("Enter username: ");
        String email = IO.readln("Enter email: ");
        String password = IO.readln("Enter password: ");
        String roleChoice = IO.readln("Role (1: Member, 2: Administrator): ");
        Role role = roleChoice.equals("2") ? Role.ADMINISTRATOR : Role.MEMBER;
        try {
          User newUser = userService.register(username, email, password, role);
          IO.println("User registered with ID: " + newUser.getId() + " | Role: " + newUser.getRole());
        } catch (IllegalArgumentException e) {
          IO.println("Failed to register user: " + e.getMessage());
        }
        break;

      case "2":
        IO.println("\n=== List of Users ===\n");
        List<User> users = userService.getAll();
        if (users.isEmpty()) {
          IO.println("No users registered.");
          break;
        }
        for (User user : users) {
          IO.println("ID: " + user.getId());
          IO.println("Username: " + user.getUsername());
          IO.println("Email: " + user.getEmail());
          IO.println("Role: " + user.getRole());
          IO.println("-----------------------");
        }
        break;

      case "3":
        IO.println("\n=== Update User ===\n");
        String updateId = IO.readln("Enter User ID to update: ");
        User toUpdate = userService.getById(updateId);
        if (toUpdate == null) {
          IO.println("User with ID " + updateId + " not found.");
          break;
        }
        String newUsername = IO.readln("New username (current: " + toUpdate.getUsername() + "): ");
        String newEmail = IO.readln("New email (current: " + toUpdate.getEmail() + "): ");
        String newPassword = IO.readln("New password: ");
        userService.update(updateId, newUsername, newEmail, newPassword);
        IO.println("User updated successfully.");
        break;

      case "4":
        IO.println("\n=== Upgrade to Administrator ===\n");
        String upgradeId = IO.readln("Enter User ID to upgrade: ");
        boolean upgraded = userService.upgradeToAdministrator(upgradeId);
        if (upgraded) {
          IO.println("User upgraded to Administrator successfully.");
        } else {
          IO.println("Upgrade failed. User not found or already an Administrator.");
        }
        break;

      case "5":
        IO.println("\n=== Remove User ===\n");
        String removeId = IO.readln("Enter User ID to remove: ");
        boolean removed = userService.remove(removeId);
        if (removed) {
          IO.println("User removed successfully.");
        } else {
          IO.println("User with ID " + removeId + " not found.");
        }
        break;

      case "6":
        IO.println("\nReturning to Main Menu.");
        break;

      default:
        IO.println("\nInvalid option. Please select a valid option (1-6).");
        break;
    }
  }

  // --- Loan menu ---

  public void manageLoans(User currentUser) {
    boolean isAdmin = currentUser instanceof Administrator;

    IO.println("\n=== Manage Loans ===\n");
    IO.println("1. Loan a Book");
    IO.println("2. Return a Book");

    if (isAdmin) {
      IO.println("3. List All Loans");
      IO.println("4. List Loans by User");
      IO.println("5. List Overdue Loans");
      IO.println("6. Back to Main Menu");
    } else {
      IO.println("3. My Loans");
      IO.println("4. Back to Main Menu");
    }

    String option = IO.readln("Choose an option: ");

    switch (option) {
      case "1":
        IO.println("\n=== Loan a Book ===\n");
        String userId = isAdmin
            ? IO.readln("Enter User ID: ")
            : currentUser.getId();
        if (!isAdmin) {
          IO.println("Loaning for: " + currentUser.getUsername());
        }
        String barcode = IO.readln("Enter Book Item Barcode: ");
        try {
          Loan loan = loanService.loanBook(userId, barcode);
          IO.println("Book loaned successfully. Loan ID: " + loan.getId());
        } catch (IllegalArgumentException e) {
          IO.println(e.getMessage());
        }
        break;

      case "2":
        IO.println("\n=== Return a Book ===\n");
        String loanId = IO.readln("Enter Loan ID: ");
        boolean returned = loanService.returnBook(loanId);
        if (returned) {
          IO.println("Book returned successfully.");
        } else {
          IO.println("Loan with ID " + loanId + " not found.");
        }
        break;

      case "3":
        if (isAdmin) {
          IO.println("\n=== All Loans ===\n");
          printLoans(loanService.getAll());
        } else {
          IO.println("\n=== My Loans ===\n");
          printLoans(loanService.getByUserId(currentUser.getId()));
        }
        break;

      case "4":
        if (isAdmin) {
          IO.println("\n=== Loans by User ===\n");
          String searchUserId = IO.readln("Enter User ID: ");
          printLoans(loanService.getByUserId(searchUserId));
        } else {
          IO.println("\nReturning to Main Menu.");
        }
        break;

      case "5":
        if (isAdmin) {
          IO.println("\n=== Overdue Loans ===\n");
          printLoans(loanService.getOverdue());
        } else {
          IO.println("\nInvalid option.");
        }
        break;

      case "6":
        if (isAdmin) {
          IO.println("\nReturning to Main Menu.");
        } else {
          IO.println("\nInvalid option.");
        }
        break;

      default:
        IO.println("\nInvalid option.");
        break;
    }
  }

  // --- Permissions / Roles menus (stubs for future expansion) ---

  public void managePermissions() {
    IO.println("\n=== Manage Permissions ===\n");
    IO.println("(Not yet implemented)");
  }

  public void manageRoles() {
    IO.println("\n=== Manage Roles ===\n");
    IO.println("(Not yet implemented)");
  }

  // --- Member book browsing ---

  /**
   * Interactive book-browsing menu available to all authenticated users.
   * Allows searching by title, author, or ISBN and viewing all copies with
   * their barcodes and availability status.
   */
  public void browseBooks() {
    IO.println("\n=== Browse Books ===\n");
    IO.println("1. Search by Title");
    IO.println("2. Search by Author");
    IO.println("3. Search by ISBN");
    IO.println("4. List All Books");
    IO.println("5. Back to Main Menu");
    String option = IO.readln("Choose an option: ");

    switch (option) {
      case "1": {
        String query = IO.readln("Enter title (partial match): ");
        List<BookDefinition> results = bookService.findByTitle(query);
        if (results.isEmpty()) {
          IO.println("No books found matching \"" + query + "\".");
        } else {
          IO.println("\nFound " + results.size() + " result(s):\n");
          for (BookDefinition def : results) {
            printBookWithCopies(def);
          }
        }
        break;
      }

      case "2": {
        String query = IO.readln("Enter author (partial match): ");
        List<BookDefinition> results = bookService.findByAuthor(query);
        if (results.isEmpty()) {
          IO.println("No books found for author \"" + query + "\".");
        } else {
          IO.println("\nFound " + results.size() + " result(s):\n");
          for (BookDefinition def : results) {
            printBookWithCopies(def);
          }
        }
        break;
      }

      case "3": {
        String isbn = IO.readln("Enter ISBN: ");
        BookDefinition def = bookService.findByISBN(isbn);
        if (def == null) {
          IO.println("No book found with ISBN \"" + isbn + "\".");
        } else {
          printBookWithCopies(def);
        }
        break;
      }

      case "4": {
        List<BookDefinition> allDefs = bookService.getAllDefinitions();
        if (allDefs.isEmpty()) {
          IO.println("No books in the library.");
        } else {
          IO.println("\n" + allDefs.size() + " book title(s) in the library:\n");
          for (BookDefinition def : allDefs) {
            printBookWithCopies(def);
          }
        }
        break;
      }

      case "5":
        IO.println("\nReturning to Main Menu.");
        break;

      default:
        IO.println("\nInvalid option. Please select a valid option (1-5).");
        break;
    }
  }

  // --- Helpers ---

  /**
   * Prints a book definition together with all its physical copies, their
   * barcodes and current availability status.
   */
  private void printBookWithCopies(BookDefinition def) {
    IO.println("Title     : " + def.getTitle());
    IO.println("Author    : " + def.getAuthor());
    IO.println("ISBN      : " + def.getIsbn());
    IO.println("Publisher : " + def.getPublisher());
    IO.println("ID        : " + def.getId());

    List<BookItem> copies = bookService.getAllItemsForDefinition(def.getId());
    if (copies.isEmpty()) {
      IO.println("Copies    : none");
    } else {
      IO.println("Copies (" + copies.size() + "):");
      for (BookItem item : copies) {
        IO.println("  Barcode: " + item.getBarcode() + "  |  Status: " + item.getStatus());
      }
    }
    IO.println("-----------------------");
  }

  private void printLoans(List<Loan> loans) {
    if (loans.isEmpty()) {
      IO.println("No loans found.");
      return;
    }
    for (Loan loan : loans) {
      BookItem item = bookService.findById(loan.getBookId());
      String barcode = item != null ? item.getBarcode() : "(unknown)";
      String title = "(unknown)";
      if (item != null) {
        BookDefinition def = bookService.findDefinitionById(item.getBookDefId());
        if (def != null) {
          title = def.getTitle() + " by " + def.getAuthor();
        }
      }
      IO.println("Loan ID : " + loan.getId());
      IO.println("Book    : " + title);
      IO.println("Barcode : " + barcode);
      IO.println("User ID : " + loan.getUserId());
      IO.println("Due     : " + loan.getDueDate());
      IO.println("-----------------------");
    }
  }
}
