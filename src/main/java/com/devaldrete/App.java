package com.devaldrete;

import java.util.HashMap;

import com.devaldrete.domain.Administrator;
import com.devaldrete.services.AuthService;
import com.devaldrete.domain.Library;
import com.devaldrete.domain.Member;
import com.devaldrete.domain.Role;
import com.devaldrete.domain.User;

public class App {

  private static Library library = new Library("1", "Tecmilenio Library", "Av. Tecmilenio");
  private static AuthService authService = new AuthService(library);

  public static void main(String[] args) {
    IO.println("Welcome to the Shelfs");
    IO.println("The lightweight library management system");

    // Default admin user
    User adminUser = new Administrator("0", "admin", "admin@example.com", "passwordsafe");
    library.addUser(adminUser);

    // Populate library with sample users
    library.addUser(new Member("1", "john", "john@example.com", "password123"));
    library.addUser(new Member("2", "anna", "anna@example.com", "password123"));
    library.addUser(new Member("3", "scarlet", "scarlet@example.com", "password123"));
    library.addUser(new Member("4", "nathan", "nathan@example.com", "password123"));
    library.addUser(new Member("5", "magnus", "magnus@example.com", "password123"));

    // Authentication loop
    while (!authService.isAuthenticated()) {
      IO.println("\n=== Authentication ===\n");
      IO.println("1. Login");
      IO.println("2. Sign Up");
      IO.println("3. Exit");
      String authOption = IO.readln("Select an option (1-3): ");

      switch (authOption) {
        case "1":
          handleLogin();
          break;
        case "2":
          handleSignup();
          break;
        case "3":
          IO.println("\nExiting the application. Goodbye!");
          System.exit(0);
        default:
          IO.println("Invalid option. Please select a valid option (1-3).");
          break;
      }
    }

    IO.println("\nEnjoy your stay!");

    // Main application loop
    while (true) {
      User currentUser = authService.getCurrentUser();
      IO.println("\n=== Menu: Managing Shelfs (Logged in as: " + currentUser.getUsername() + " - "
          + currentUser.getRole() + ") ===\n");
      IO.println("1. Quick Overview");
      IO.println("2. Manage Books");
      IO.println("3. Manage Users");
      IO.println("4. Manage Loans");
      IO.println("5. Logout");
      IO.println("6. Exit");

      String option = IO.readln("\nSelect an option (1-6): ");

      switch (option) {
        case "1":
          IO.println("\nQuick Overview selected.");
          IO.println("\n=== Quick Overview ===\n");
          HashMap<String, Integer> overview = library.quickOverview();
          overview.forEach((k, v) -> {
            IO.println(k + ": " + v);
          });
          break;
        case "2":
          if (library.canManageBooks(currentUser)) {
            IO.println("\nManage Books selected.");
            library.manageBooks();
          } else {
            IO.println("\nAccess Denied: You don't have permission to manage books.");
            IO.println("Only Administrators can manage books.");
          }
          break;
        case "3":
          if (library.canManageUsers(currentUser)) {
            IO.println("\nManage Users selected.");
            library.manageUsers();
          } else {
            IO.println("\nAccess Denied: You don't have permission to manage users.");
            IO.println("Only Administrators can manage users.");
          }
          break;
        case "4":
          IO.println("\nManage Loans selected.");
          manageLoansWithRBAC(currentUser);
          break;
        case "5":
          IO.println("\nLogging out...");
          authService.logout();
          IO.println("Logged out successfully.");
          main(args); // Restart the application
          return;
        case "6":
          IO.println("\nExiting the application. Goodbye!");
          System.exit(0);
        default:
          IO.println("Invalid option. Please select a valid option (1-6).");
          break;
      }
    }
  }

  private static void handleLogin() {
    IO.println("\n=== Login ===\n");
    String username = IO.readln("Enter username: ");
    String password = IO.readln("Enter password: ");

    boolean success = authService.login(username, password);

    if (success) {
      IO.println("\nLogin successful! Welcome, " + authService.getCurrentUser().getUsername() + "!");
    } else {
      IO.println("\nLogin failed. Invalid username or password.");
    }
  }

  private static void handleSignup() {
    IO.println("\n=== Sign Up ===\n");
    String username = IO.readln("Enter username: ");
    String email = IO.readln("Enter email: ");
    String password = IO.readln("Enter password: ");

    Role role = Role.MEMBER; // Default role for new users

    try {
      User newUser = authService.signup(username, email, password, role);
      IO.println("\nSign up successful! User registered with ID: " + newUser.getId());
      IO.println("Please login to continue.");
    } catch (IllegalArgumentException e) {
      IO.println("\nSign up failed: " + e.getMessage());
    }
  }

  private static void manageLoansWithRBAC(User currentUser) {
    IO.println("\n=== Manage Loans ===\n");
    IO.println("1. Loan a Book");
    IO.println("2. Return a Book");

    if (library.canViewAllLoans(currentUser)) {
      IO.println("3. List All Loans");
      IO.println("4. List Loans by User");
      IO.println("5. List Overdue Loans");
      IO.println("6. Back to Main Menu");
    } else {
      IO.println("3. List My Loans");
      IO.println("4. Back to Main Menu");
    }

    String option = IO.readln("Choose an option: ");

    String userId;
    String barcode;

    switch (option) {
      case "1":
        IO.println("\nLoan a Book selected.");
        IO.println("\n=== Loan Book ===\n");

        // Members can only loan books for themselves
        if (library.canViewAllLoans(currentUser)) {
          userId = IO.readln("Enter User ID: ");
        } else {
          userId = currentUser.getId();
          IO.println("Loaning book for: " + currentUser.getUsername());
        }

        barcode = IO.readln("Enter Book Item Barcode: ");

        try {
          library.loanBook(userId, barcode);
          IO.println("Book loaned successfully.");
        } catch (IllegalArgumentException e) {
          IO.println(e.getMessage());
        }

        break;
      case "2":
        IO.println("\nReturn a Book selected.");
        IO.println("\n=== Return Book ===\n");
        String loanId = IO.readln("Enter Loan ID: ");

        boolean returned = library.returnBook(loanId);

        if (returned) {
          IO.println("Book returned successfully.");
        } else {
          IO.println("Loan with ID " + loanId + " not found.");
        }

        break;
      case "3":
        if (library.canViewAllLoans(currentUser)) {
          IO.println("\nList All Loans selected.");
          IO.println("\n=== List of All Loans ===\n");
          library.findAllLoans().forEach(loan -> {
            IO.println(
                "Loan ID: " + loan.getId() + " | User ID: " + loan.getUserId() + " | Book ID: " + loan.getBookId()
                    + " | Created At: " + loan.getCreatedAt() + " | Due Date: " + loan.getDueDate());
            IO.println("-----------------------");
          });
        } else {
          IO.println("\nList My Loans selected.");
          IO.println("\n=== My Loans ===\n");
          library.getAccessibleLoans(currentUser).forEach(loan -> {
            IO.println("Loan ID: " + loan.getId() + " | Book ID: " + loan.getBookId()
                + " | Created At: " + loan.getCreatedAt() + " | Due Date: " + loan.getDueDate());
            IO.println("-----------------------");
          });
        }
        break;
      case "4":
        if (library.canViewAllLoans(currentUser)) {
          IO.println("\nList Loans by User selected.");
          IO.println("\n=== List Loans by User ===\n");
          userId = IO.readln("Enter User ID: ");
          library.findLoansByUserId(userId).forEach(loan -> {
            IO.println(
                "Loan ID: " + loan.getId() + " | User ID: " + loan.getUserId() + " | Book ID: " + loan.getBookId()
                    + " | Created At: " + loan.getCreatedAt() + " | Due Date: " + loan.getDueDate());
            IO.println("-----------------------");
          });
        } else {
          IO.println("\nReturning to Main Menu.");
        }
        break;
      case "5":
        if (library.canViewAllLoans(currentUser)) {
          IO.println("\nList Overdue Loans selected.");
          IO.println("\n=== List of Overdue Loans ===\n");
          library.findOverdueLoans().forEach(loan -> {
            IO.println(
                "Loan ID: " + loan.getId() + " | User ID: " + loan.getUserId() + " | Book ID: " + loan.getBookId()
                    + " | Created At: " + loan.getCreatedAt() + " | Due Date: " + loan.getDueDate());
            IO.println("-----------------------");
          });
        } else {
          IO.println("\nInvalid option.");
        }
        break;
      case "6":
        if (library.canViewAllLoans(currentUser)) {
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

}
