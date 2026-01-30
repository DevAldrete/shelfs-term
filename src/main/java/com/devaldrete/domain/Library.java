package com.devaldrete.domain;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.devaldrete.utils.BarcodeGenerator;

public class Library {
  private String id;
  private String name;
  private String address;
  private ArrayList<BookDefinition> bookDefinitions;
  private ArrayList<BookItem> bookItems;
  private ArrayList<User> users;
  private ArrayList<Loan> loans;

  public Library(String id, String name, String address) {
    this.bookDefinitions = new ArrayList<>();
    this.bookItems = new ArrayList<>();
    this.users = new ArrayList<>();
    this.loans = new ArrayList<>();

    this.id = id;
    this.name = name;
    this.address = address;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  // Book Management
  public void manageBooks() {
    IO.println("\n=== Manage Books ===\n");
    IO.println("1. Add Book");
    IO.println("2. Remove Book");
    IO.println("3. All Books");
    IO.println("4. Available Books");
    IO.println("5. Update a Book");
    IO.println("6. Back to Main Menu");
    String option = IO.readln("Choose an option: ");

    String isbn;
    String barcode;

    switch (option) {
      case "1":
        IO.println("\nAdd Book selected.");
        IO.println("\n=== Add Book Item ===\n");
        isbn = IO.readln("What's the ISBN of the new book: ");
        addBook(isbn);
        break;
      case "2":
        IO.println("\nRemove Book selected.");
        IO.println("\n=== Remove Book Item ===\n");
        barcode = IO.readln("Give me the barcode of the book item to remove: ");
        Boolean removed = removeBook(barcode);

        if (removed) {
          IO.println("Book item with barcode " + barcode + " removed successfully.");
        } else {
          IO.println("Book item with barcode " + barcode + " not found.");
        }
        break;
      case "3":
        IO.println("\nAll Books selected.");
        IO.println("\n=== List of Books ===\n");
        bookDefinitions.forEach(book -> {
          IO.println("ID: " + book.getId());
          IO.println("Title: " + book.getTitle());
          IO.println("Author: " + book.getAuthor());
          IO.println("ISBN: " + book.getIsbn());
          IO.println("Publisher: " + book.getPublisher());
          IO.println("-----------------------");
        });
        break;
      case "4":
        IO.println("\nAvailable Books by ISBN selected.");
        IO.println("\n=== Search Available Books by ISBN ===\n");
        BookDefinition bookDefinition = findBookByISBN(IO.readln("Enter the ISBN to search: "));

        List<BookItem> availableBooks = findAvailableBooksByDefinition(bookDefinition);

        IO.println("\n=== Available Book Items for ISBN: " + bookDefinition.getIsbn() + " ===\n");
        IO.println("Title: " + bookDefinition.getTitle());

        if (availableBooks.isEmpty()) {
          IO.println("No available book items found for this ISBN.");
          break;
        }

        for (BookItem book : availableBooks) {
          IO.println("Book Item Barcode: " + book.getBarcode() + " | Status: " + book.getStatus());
          IO.println("-----------------------");
        }

        break;
      case "5":
        IO.println("\nUpdate a Book selected.");
        IO.println("\n=== Update Book ===\n");
        isbn = IO.readln("Insert the ISBN to update: ");

        boolean updated = updateBookDefinition(isbn);

        if (!updated) {
          IO.println("Book with ISBN " + isbn + " not found.");
          break;
        }

        IO.println("Book with ISBN " + isbn + " updated successfully.");
        break;

      case "6":
        IO.println("\nReturning to Main Menu.");
        break;
      default:
        IO.println("\nInvalid option. Please select a valid option (1-5).");
        break;
    }
  }

  // Removing a book item logic
  public boolean removeBook(String barcode) {
    boolean removed = bookItems.removeIf(b -> b.getBarcode().equalsIgnoreCase(barcode));

    if (removed) {
      return true;
    }

    return false;
  }

  // Reading a book logic
  public BookItem findBookItemByBarcode(String barcode) {
    for (BookItem item : bookItems) {
      if (item.getBarcode().equals(barcode)) {
        return item;
      }
    }
    return null;
  }

  public BookDefinition findBookByISBN(String isbn) {
    for (BookDefinition def : bookDefinitions) {
      if (def.getIsbn().equals(isbn)) {
        return def;
      }
    }
    return null;
  }

  public List<BookItem> findAvailableBooksByDefinition(BookDefinition bookDefinition) {
    // We find all the book items related to the book definition
    List<BookItem> availableBooks = bookItems.stream()
        .filter(b -> b.getBookDefId().equalsIgnoreCase(bookDefinition.getId()))
        .collect(Collectors.toList());

    return availableBooks;
  }

  // Adding a new book logic
  private void addBookItem(String bookDefId) {

    String id = UUID.randomUUID().toString();
    String barcode = BarcodeGenerator.generateBarcode();

    BookItem bookItem = new BookItem(id, barcode, bookDefId, Status.AVAILABLE, Instant.now());

    bookItems.add(bookItem);
  }

  public BookDefinition addBook(String isbn) {
    BookDefinition bookDef = findBookByISBN(isbn);

    if (bookDef != null) {
      IO.println("Book with ISBN " + isbn + " already exists in the library.");
      IO.println("Adding it to the pile.");

      addBookItem(bookDef.getId());

      IO.println("Book item added successfully.");
      return bookDef;
    }

    String id = UUID.randomUUID().toString();
    String title = IO.readln("Enter book title: ");
    String author = IO.readln("Enter book author: ");
    String publisher = IO.readln("Enter book publisher: ");

    BookDefinition bookDefinition = new BookDefinition(id, title, author, isbn, publisher);

    bookDefinitions.add(bookDefinition);
    addBookItem(bookDefinition.getId());

    IO.println("New book added successfully.");

    return bookDefinition;
  }

  // Update book logic
  public boolean updateBookDefinition(String isbn) {

    BookDefinition currentBookDef = findBookByISBN(isbn);

    if (currentBookDef == null) {
      return false;
    }

    String title = IO.readln("Enter new title (current: " + currentBookDef.getTitle() + "): ");
    String author = IO.readln("Enter new author (current: " + currentBookDef.getAuthor() + "): ");
    String publisher = IO.readln("Enter new publisher (current: " + currentBookDef.getPublisher() + "): ");

    currentBookDef.setTitle(title);
    currentBookDef.setAuthor(author);
    currentBookDef.setPublisher(publisher);

    return true;
  }

  // --- User Management ---
  public void manageUsers() {
    IO.println("\n=== Manage Users ===\n");
    IO.println("1. Register User");
    IO.println("2. List Users");
    IO.println("3. Update User");
    IO.println("4. Back to Main Menu");
    String option = IO.readln("Choose an option: ");

    switch (option) {
      case "1":
        IO.println("\nRegister User selected.");
        IO.println("\n=== Register New User ===\n");
        String username = IO.readln("Enter username: ");
        String email = IO.readln("Enter email: ");
        String password = IO.readln("Enter password: ");

        User newUser = registerUser(username, email, password);
        IO.println("\nUser registered successfully with ID: " + newUser.getId());

        IO.println("-----------------------");
        IO.println("\nAdding new user to the library system...");

        boolean added = addUser(newUser);

        if (added) {
          IO.println("User added successfully.");
        } else {
          IO.println("Failed to add user. User with ID " + newUser.getId() + " already exists.");
        }

        break;
      case "2":
        IO.println("\nList Users selected.");
        IO.println("\n=== List of Users ===\n");
        users.forEach(user -> {
          IO.println("ID: " + user.getId());
          IO.println("Username: " + user.getUsername());
          IO.println("Email: " + user.getEmail());
          IO.println("-----------------------");
        });
        break;
      case "3":
        IO.println("\nUpdate User selected.");
        IO.println("\n=== Update User ===\n");
        String userId = IO.readln("Enter User ID to update: ");

        String newUsername = IO.readln("Enter new username: ");
        String newEmail = IO.readln("Enter new email: ");
        String newPassword = IO.readln("Enter new password: ");

        boolean updated = updateUser(userId, newUsername, newEmail, newPassword);

        if (updated) {
          IO.println("User with ID " + userId + " updated successfully.");
        } else {
          IO.println("User with ID " + userId + " not found.");
        }

        break;
      case "4":
        IO.println("\nReturning to Main Menu.");
        break;
      default:
        IO.println("\nInvalid option. Please select a valid option (1-3).");
        break;
    }
  }

  public User registerUser(String username, String email, String password) {
    String id = UUID.randomUUID().toString();
    User user = new User(id, username, email, password);
    return user;
  }

  public boolean addUser(User user) {
    // Prevent duplicate IDs
    if (findUserById(user.getId()).isPresent()) {
      return false;
    }
    this.users.add(user);
    return true;
  }

  public Optional<User> findUserById(String id) {
    return users.stream()
        .filter(user -> user.getId().equals(id))
        .findFirst();
  }

  public List<User> findAllUsers() {
    return new ArrayList<>(users);
  }

  public boolean removeUser(String id) {
    if (users.removeIf(user -> user.getId().equals(id))) {
      return true;
    }

    return false;
  }

  public boolean updateUser(String id, String username, String email, String password) {
    Optional<User> userOpt = findUserById(id);
    if (userOpt.isEmpty()) {
      return false;
    }

    User user = userOpt.get();
    user.setUsername(username);
    user.setEmail(email);
    user.setPassword(password);
    return true;
  }

  // --- Loan Management ---
  public void manageLoans() {
    IO.println("\n=== Manage Loans ===\n");
    IO.println("1. Loan a Book");
    IO.println("2. Return a Book");
    IO.println("3. List All Loans");
    IO.println("4. List Loans by User");
    IO.println("5. List Overdue Loans");
    IO.println("6. Back to Main Menu");
    String option = IO.readln("Choose an option: ");

    String userId;
    String barcode;

    switch (option) {
      case "1":
        IO.println("\nLoan a Book selected.");
        IO.println("\n=== Loan Book ===\n");
        userId = IO.readln("Enter User ID: ");
        barcode = IO.readln("Enter Book Item Barcode: ");

        try {
          Loan loan = loanBook(userId, barcode);
          IO.println("Book loaned successfully with Loan ID: " + loan.getId());
        } catch (IllegalArgumentException e) {
          IO.println(e.getMessage());
        }

        break;
      case "2":
        IO.println("\nReturn a Book selected.");
        IO.println("\n=== Return Book ===\n");
        String loanId = IO.readln("Enter Loan ID: ");

        boolean returned = returnBook(loanId);

        if (returned) {
          IO.println("Book returned successfully.");
        } else {
          IO.println("Loan with ID " + loanId + " not found.");
        }

        break;
      case "3":
        IO.println("\nList All Loans selected.");
        IO.println("\n=== List of All Loans ===\n");
        List<Loan> allLoans = findAllLoans();
        for (Loan loan : allLoans) {
          IO.println("Loan ID: " + loan.getId() + " | User ID: " + loan.getUserId() + " | Book ID: " + loan.getBookId()
              + " | Created At: " + loan.getCreatedAt() + " | Due Date: " + loan.getDueDate());
          IO.println("-----------------------");
        }
        break;
      case "4":
        IO.println("\nList Loans by User selected.");
        IO.println("\n=== List Loans by User ===\n");
        userId = IO.readln("Enter User ID: ");
        findLoansByUserId(userId);
        break;
      case "5":
        IO.println("\nList Overdue Loans selected.");
        IO.println("\n=== List of Overdue Loans ===\n");
        List<Loan> overdueLoans = findOverdueLoans();
        for (Loan loan : overdueLoans) {
          IO.println("Loan ID: " + loan.getId() + " | User ID: " + loan.getUserId() + " | Book ID: " + loan.getBookId()
              + " | Created At: " + loan.getCreatedAt() + " | Due Date: " + loan.getDueDate());
          IO.println("-----------------------");
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

  public Loan loanBook(String userId, String barcode) {
    // Validate User
    Optional<User> user = findUserById(userId);

    if (user.isEmpty()) {
      throw new IllegalArgumentException("User with ID " + userId + " not found.");
    }

    // Validate max loans per user
    List<Loan> loansPerUser = findLoansByUserId(userId);

    if (loansPerUser.size() >= 2) {
      throw new IllegalArgumentException("User with ID " + userId + " has reached the maximum number of loans.");
    }

    // Create Loan
    String id = UUID.randomUUID().toString();
    Instant now = Instant.now();
    Instant dueDate = now.plus(14, ChronoUnit.DAYS); // 2 week loan period

    BookItem bookItem = findBookItemByBarcode(barcode);

    if (bookItem.getStatus() != Status.AVAILABLE) {
      throw new IllegalArgumentException("Book item with barcode " + barcode + " is not available for loan.");
    }

    Loan loan = new Loan(id, userId, bookItem.getId(), now, dueDate);

    bookItem.setStatus(Status.BORROWED);

    loans.add(loan);

    return loan;
  }

  public boolean returnBook(String loanId) {
    boolean removed = loans.removeIf(loan -> loan.getId().equals(loanId));
    if (!removed) {
      return false;
    }

    return true;
  }

  public List<Loan> findAllLoans() {
    return new ArrayList<>(loans);
  }

  public List<Loan> findLoansByUserId(String userId) {
    return loans.stream()
        .filter(loan -> loan.getUserId().equals(userId))
        .collect(Collectors.toList());
  }

  public List<Loan> findOverdueLoans() {
    Instant now = Instant.now();
    return loans.stream()
        .filter(loan -> loan.getDueDate().isBefore(now))
        .collect(Collectors.toList());
  }

  // Quick overview of the library
  public HashMap<String, Integer> quickOverview() {
    HashMap<String, Integer> overview = new HashMap<>();

    overview.put("Books", bookItems.size());
    overview.put("Users", users.size());
    overview.put("Loans", loans.size());
    overview.put("Overdue Loans", findOverdueLoans().size());

    return overview;
  }
}
