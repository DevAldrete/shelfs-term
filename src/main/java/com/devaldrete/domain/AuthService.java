package com.devaldrete.domain;

import java.util.Optional;
import java.util.UUID;

public class AuthService {
  private Library library;
  private User currentUser;

  public AuthService(Library library) {
    this.library = library;
    this.currentUser = null;
  }

  public User getCurrentUser() {
    return currentUser;
  }

  public boolean isAuthenticated() {
    return currentUser != null;
  }

  public boolean login(String username, String password) {
    Optional<User> userOpt = library.findUserByUsername(username);

    if (userOpt.isEmpty()) {
      return false;
    }

    User user = userOpt.get();
    if (user.getPassword().equals(password)) {
      this.currentUser = user;
      return true;
    }

    return false;
  }

  public void logout() {
    this.currentUser = null;
  }

  public User signup(String username, String email, String password, Role role) {
    // Check if username already exists
    if (library.findUserByUsername(username).isPresent()) {
      throw new IllegalArgumentException("Username already exists.");
    }

    // Check if email already exists
    if (library.findUserByEmail(email).isPresent()) {
      throw new IllegalArgumentException("Email already exists.");
    }

    String id = UUID.randomUUID().toString();
    User newUser;

    if (role == Role.ADMINISTRATOR) {
      newUser = new Administrator(id, username, email, password);
    } else {
      newUser = new Member(id, username, email, password);
    }

    boolean added = library.addUser(newUser);

    if (!added) {
      throw new IllegalStateException("Failed to add user to library.");
    }

    return newUser;
  }

  public boolean hasPermission(String permission) {
    if (!isAuthenticated()) {
      return false;
    }

    if (currentUser instanceof Administrator) {
      return true; // Administrators have all permissions
    }

    if (currentUser instanceof Member) {
      Member member = (Member) currentUser;
      switch (permission) {
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
