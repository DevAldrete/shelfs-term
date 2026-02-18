package com.devaldrete.repositories;

import java.util.ArrayList;
import java.util.List;
import com.devaldrete.domain.User;

public class UserRepository extends BaseRepository<User> {

  private List<User> users;

  /**
   * Initializes the UserRepository with an empty user list.
   * Supports managing User instances and its subclasses (Administrator, Member).
   */
  public UserRepository() {
    super();
    this.users = new ArrayList<>();
  }

  /**
   * Persists a new user to the repository.
   * 
   * @param item the user to save. Must not be null.
   * @throws IllegalArgumentException if item is null or user already exists
   */
  @Override
  public void save(User item) {
    validateNotNull(item, "User");
    validateNotNull(item.getId(), "User ID");
    validateNotEmpty(item.getId(), "User ID");

    if (exists(item.getId())) {
      throw new IllegalArgumentException("User with ID '" + item.getId() + "' already exists");
    }

    users.add(item);
  }

  /**
   * Updates an existing user in the repository.
   * 
   * @param item the user to update. Must not be null.
   * @throws IllegalArgumentException if item is null
   * @throws IllegalStateException    if user does not exist in repository
   */
  @Override
  public void update(User item) {
    validateNotNull(item, "User");
    validateNotNull(item.getId(), "User ID");
    validateNotEmpty(item.getId(), "User ID");

    if (!exists(item.getId())) {
      throw new IllegalStateException("User with ID '" + item.getId() + "' not found");
    }

    for (int i = 0; i < users.size(); i++) {
      if (users.get(i).getId().equals(item.getId())) {
        users.set(i, item);
        return;
      }
    }
  }

  /**
   * Deletes a user from the repository by ID.
   * 
   * @param id the unique identifier of the user to delete. Must not be null or
   *           empty.
   * @throws IllegalArgumentException if id is null or empty
   * @throws IllegalStateException    if user with given id does not exist
   */
  @Override
  public void delete(String id) {
    validateNotEmpty(id, "User ID");

    if (!exists(id)) {
      throw new IllegalStateException("User with ID '" + id + "' not found");
    }

    users.removeIf(user -> user.getId().equals(id));
  }

  /**
   * Retrieves a user by their ID.
   * 
   * @param id the unique identifier of the user. Must not be null or empty.
   * @return the user if found, null if not found
   * @throws IllegalArgumentException if id is null or empty
   */
  @Override
  public User getById(String id) {
    validateNotEmpty(id, "User ID");

    return users.stream()
        .filter(user -> user.getId().equals(id))
        .findFirst()
        .orElse(null);
  }

  /**
   * Retrieves all users from the repository.
   * 
   * @return a list containing all users. Never null, may be empty.
   */
  @Override
  public List<User> getAll() {
    return new ArrayList<>(users);
  }

  /**
   * Extracts the ID from a user entity.
   * 
   * @param item the user to extract ID from
   * @return the unique identifier of the user
   * @throws IllegalArgumentException if item is null
   */
  @Override
  protected String getId(User item) {
    validateNotNull(item, "User");
    return item.getId();
  }

  /**
   * Determines if a user with the given ID exists in the repository.
   * 
   * @param id the unique identifier to check
   * @return true if user exists, false otherwise
   */
  @Override
  protected boolean exists(String id) {
    if (id == null || id.trim().isEmpty()) {
      return false;
    }

    return users.stream()
        .anyMatch(user -> user.getId().equals(id));
  }

  /**
   * Finds a user by username.
   * Supports all User types including Administrator and Member subclasses.
   * 
   * @param username the username to search for. Must not be null or empty.
   * @return the user if found, null if not found
   * @throws IllegalArgumentException if username is null or empty
   */
  public User findByUsername(String username) {
    validateNotEmpty(username, "Username");

    return users.stream()
        .filter(user -> user.getUsername().equals(username))
        .findFirst()
        .orElse(null);
  }

  /**
   * Finds a user by email.
   * Supports all User types including Administrator and Member subclasses.
   * 
   * @param email the email to search for. Must not be null or empty.
   * @return the user if found, null if not found
   * @throws IllegalArgumentException if email is null or empty
   */
  public User findByEmail(String email) {
    validateNotEmpty(email, "Email");

    return users.stream()
        .filter(user -> user.getEmail().equals(email))
        .findFirst()
        .orElse(null);
  }

  /**
   * Counts the total number of users in the repository.
   * 
   * @return the number of users
   */
  public int count() {
    return users.size();
  }

  /**
   * Checks if a username is already in use.
   * 
   * @param username the username to check. Must not be null or empty.
   * @return true if username exists, false otherwise
   * @throws IllegalArgumentException if username is null or empty
   */
  public boolean usernameExists(String username) {
    validateNotEmpty(username, "Username");

    return users.stream()
        .anyMatch(user -> user.getUsername().equals(username));
  }

  /**
   * Checks if an email is already in use.
   * 
   * @param email the email to check. Must not be null or empty.
   * @return true if email exists, false otherwise
   * @throws IllegalArgumentException if email is null or empty
   */
  public boolean emailExists(String email) {
    validateNotEmpty(email, "Email");

    return users.stream()
        .anyMatch(user -> user.getEmail().equals(email));
  }
}
