package com.devaldrete;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import com.devaldrete.domain.Administrator;
import com.devaldrete.domain.Member;
import com.devaldrete.domain.User;
import com.devaldrete.repositories.PermissionRepository;
import com.devaldrete.services.AuthService;
import com.devaldrete.services.Library;
import com.devaldrete.services.PersistenceService;

public class App {

  public static void main(String[] args) {
    IO.println("Welcome to the Shelfs");
    IO.println("The lightweight library management system");

    // --- Bootstrap ---
    Library library = new Library("Tecmilenio Library", "Av. Tecmilenio");

    // AuthService shares the Library's UserService so all users are in one store
    AuthService authService = new AuthService(library.getUserService(), new PermissionRepository());

    PersistenceService persistence = new PersistenceService();

    // Load persisted data if it exists; otherwise seed defaults
    if (Files.exists(Paths.get("data/users.json"))) {
      IO.println("Loading saved data...");
      persistence.loadAll(
          library.getUserService(),
          library.getBookService(),
          library.getLoanService());
      IO.println("Data loaded.");
    } else {
      // Seed default admin (ID "0" is stable so it can be referenced in tests/docs)
      library.addUser(new Administrator("0", "admin", "admin@example.com", "passwordsafe"));

      // Seed sample members
      library.addUser(new Member("1", "john", "john@example.com", "password123"));
      library.addUser(new Member("2", "anna", "anna@example.com", "password123"));
      library.addUser(new Member("3", "scarlet", "scarlet@example.com", "password123"));
      library.addUser(new Member("4", "nathan", "nathan@example.com", "password123"));
      library.addUser(new Member("5", "magnus", "magnus@example.com", "password123"));
    }

    // --- Authentication loop ---
    while (!authService.isAuthenticated()) {
      IO.println("\n=== Authentication ===\n");
      IO.println("1. Login");
      IO.println("2. Sign Up");
      IO.println("3. Exit");
      String authOption = IO.readln("Select an option (1-3): ");

      switch (authOption) {
        case "1":
          handleLogin(authService);
          break;
        case "2":
          handleSignup(authService);
          break;
        case "3":
          IO.println("\nExiting the application. Goodbye!");
          System.exit(0);
          break;
        default:
          IO.println("Invalid option. Please select a valid option (1-3).");
          break;
      }
    }

    IO.println("\nEnjoy your stay!");

    // --- Main application loop ---
    boolean running = true;
    while (running) {
      // AuthService middleware guard: if somehow not authenticated, return to auth
      // loop
      if (!authService.requireAuthenticated()) {
        IO.println("\nSession expired. Please log in again.");
        break;
      }

      User currentUser = authService.getCurrentUser();
      IO.println("\n=== Menu: Shelfs (Logged in as: " + currentUser.getUsername()
          + " [" + currentUser.getRole() + "]) ===\n");
      IO.println("1. Quick Overview");
      IO.println("2. Browse Books");
      IO.println("3. Manage Books  (admin)");
      IO.println("4. Manage Users  (admin)");
      IO.println("5. Manage Loans");
      IO.println("6. Logout");
      IO.println("7. Exit");

      String option = IO.readln("\nSelect an option (1-7): ");

      switch (option) {
        case "1":
          IO.println("\n=== Quick Overview ===\n");
          HashMap<String, Integer> overview = library.quickOverview();
          overview.forEach((k, v) -> IO.println(k + ": " + v));
          break;

        case "2":
          library.browseBooks();
          break;

        case "3":
          // Middleware guard: only Administrators can manage books
          if (authService.requireAdmin()) {
            library.manageBooks();
          } else {
            IO.println("\nAccess Denied: Only Administrators can manage books.");
          }
          break;

        case "4":
          // Middleware guard: only Administrators can manage users
          if (authService.requireAdmin()) {
            library.manageUsers();
          } else {
            IO.println("\nAccess Denied: Only Administrators can manage users.");
          }
          break;

        case "5":
          // Loan management is role-aware inside manageLoans()
          library.manageLoans(currentUser);
          break;

        case "6":
          IO.println("\nSaving data...");
          persistence.saveAll(
              library.getUserService(),
              library.getBookService(),
              library.getLoanService());
          IO.println("\nLogging out...");
          authService.logout();
          IO.println("Logged out successfully.");
          running = false; // Exit main loop — re-enter auth loop on next iteration
          break;

        case "7":
          IO.println("\nSaving data...");
          persistence.saveAll(
              library.getUserService(),
              library.getBookService(),
              library.getLoanService());
          IO.println("\nExiting the application. Goodbye!");
          System.exit(0);
          break;

        default:
          IO.println("Invalid option. Please select a valid option (1-7).");
          break;
      }

      // After logout, restart the outer flow from the top of main
      if (!running) {
        main(args);
        return;
      }
    }
  }

  // --- Auth helpers ---

  private static void handleLogin(AuthService authService) {
    IO.println("\n=== Login ===\n");
    String username = IO.readln("Enter email: ");
    String password = IO.readln("Enter password: ");

    boolean success = authService.login(username, password);

    if (success) {
      IO.println("\nLogin successful! Welcome, " + authService.getCurrentUser().getUsername() + "!");
    } else {
      IO.println("\nLogin failed. Invalid email or password.");
    }
  }

  private static void handleSignup(AuthService authService) {
    IO.println("\n=== Sign Up ===\n");
    String username = IO.readln("Enter username: ");
    String email = IO.readln("Enter email: ");
    String password = IO.readln("Enter password: ");

    try {
      // signup always creates a MEMBER; elevation happens through the admin panel
      User newUser = authService.signup(username, email, password);
      IO.println("\nSign up successful! Your account ID: " + newUser.getId());
      IO.println("Please log in to continue.");
    } catch (IllegalArgumentException e) {
      IO.println("\nSign up failed: " + e.getMessage());
    }
  }
}
