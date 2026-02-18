package com.devaldrete.services;

import com.devaldrete.domain.Administrator;
import com.devaldrete.domain.Member;
import com.devaldrete.domain.Permission;
import com.devaldrete.domain.User;
import com.devaldrete.repositories.PermissionRepository;

/**
 * AuthService acts as an authentication and authorization middleware.
 *
 * It holds the current session user and exposes guard methods that the
 * application layer calls before performing protected operations.
 *
 * It shares the same UserService instance as the rest of the application so
 * that users registered via the library are also visible to the auth layer.
 */
public class AuthService {

  private User currentUser;
  private final UserService userService;
  private final PermissionRepository permissionRepository;

  /**
   * @param userService          shared UserService (same instance used by
   *                             Library)
   * @param permissionRepository repository for named permissions
   */
  public AuthService(UserService userService, PermissionRepository permissionRepository) {
    this.userService = userService;
    this.permissionRepository = permissionRepository;
    this.currentUser = null;

    // Seed default permissions
    permissionRepository.save(new Permission("0", "VIEW_OWN_LOANS", "Allows members to view their own loan history."));
    permissionRepository
        .save(new Permission("1", "VIEW_ALL_BOOKS", "Allows members to view all books in the library."));
    permissionRepository.save(new Permission("2", "LOAN_BOOKS", "Allows members to loan books from the library."));
    permissionRepository
        .save(new Permission("3", "RETURN_BOOKS", "Allows members to return loaned books to the library."));
  }

  // --- Session state ---

  public User getCurrentUser() {
    return currentUser;
  }

  public boolean isAuthenticated() {
    return currentUser != null;
  }

  // --- Authentication ---

  /**
   * Authenticates by username (case-sensitive) and plain-text password.
   * Returns false (does NOT throw) when credentials are wrong, so callers
   * can show a user-friendly message without catching exceptions.
   */
  public boolean login(String email, String password) {
    if (email == null || email.trim().isEmpty()
        || password == null || password.trim().isEmpty()) {
      return false;
    }

    User user = userService.findByEmail(email);
    if (user == null) {
      return false;
    }

    if (!user.getPassword().equals(password)) {
      return false;
    }

    this.currentUser = user;
    return true;
  }

  public void logout() {
    this.currentUser = null;
  }

  /**
   * Registers a new Member-role account. Always creates a MEMBER — elevation
   * to Administrator must go through the admin panel
   * (UserService.upgradeToAdministrator).
   *
   * @throws IllegalArgumentException if email is already used
   */
  public User signup(String username, String email, String password) {
    if (userService.findByEmail(email) != null) {
      throw new IllegalArgumentException("Email '" + email + "' is already registered.");
    }

    Member newUser = new Member(username, email, password);
    userService.save(newUser);
    return newUser;
  }

  // --- Authorization middleware ---

  /**
   * Returns true only when a user is currently logged in.
   * Use this as a guard before any authenticated action.
   */
  public boolean requireAuthenticated() {
    return isAuthenticated();
  }

  /**
   * Returns true only when the current user is an Administrator.
   * Use this as a guard before admin-only actions.
   */
  public boolean requireAdmin() {
    return isAuthenticated() && currentUser instanceof Administrator;
  }

  /**
   * Checks whether the current user holds a named permission.
   * Administrators are implicitly granted every permission.
   * Members are checked against their capability methods.
   *
   * @param permissionName the logical name (e.g. "LOAN_BOOKS")
   */
  public boolean hasPermission(String permissionName) {
    if (!isAuthenticated()) {
      return false;
    }

    if (currentUser instanceof Administrator) {
      return true;
    }

    if (currentUser instanceof Member) {
      Member member = (Member) currentUser;
      switch (permissionName) {
        case "VIEW_OWN_LOANS":
          return member.canViewOwnLoans();
        case "VIEW_ALL_BOOKS":
          return member.canViewAllBooks();
        case "LOAN_BOOKS":
          return member.canLoanBooks();
        case "RETURN_BOOKS":
          return member.canReturnBooks();
        default:
          return false;
      }
    }

    return false;
  }

  public boolean isAdministrator() {
    return isAuthenticated() && currentUser instanceof Administrator;
  }

  public boolean isMember() {
    return isAuthenticated() && currentUser instanceof Member;
  }
}
