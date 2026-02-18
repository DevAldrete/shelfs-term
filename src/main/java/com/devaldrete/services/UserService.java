package com.devaldrete.services;

import java.util.List;

import com.devaldrete.domain.Administrator;
import com.devaldrete.domain.Member;
import com.devaldrete.domain.Role;
import com.devaldrete.domain.User;
import com.devaldrete.repositories.UserRepository;

public class UserService {

  private final UserRepository userRepository;

  public UserService() {
    this.userRepository = new UserRepository();
  }

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User register(String username, String email, String password, Role role) {
    User user = role == Role.ADMINISTRATOR
        ? new Administrator(username, email, password)
        : new Member(username, email, password);
    userRepository.save(user);
    return user;
  }

  /**
   * Persists a pre-constructed User object directly.
   * Use this when the caller is responsible for building the entity
   * (e.g. seeding the default admin, or signup via AuthService).
   */
  public void save(User user) {
    userRepository.save(user);
  }

  public List<User> getAll() {
    return userRepository.getAll();
  }

  public User getById(String id) {
    return userRepository.getById(id);
  }

  public boolean update(String id, String username, String email, String password) {
    User user = userRepository.getById(id);
    if (user == null) {
      return false;
    }
    user.setUsername(username);
    user.setEmail(email);
    user.setPassword(password);
    userRepository.update(user);
    return true;
  }

  public boolean upgradeToAdministrator(String userId) {
    User user = userRepository.getById(userId);
    if (user == null || user instanceof Administrator) {
      return false;
    }
    // Preserve the original ID so the repository can find and replace the record
    Administrator admin = new Administrator(
        user.getId(), user.getUsername(), user.getEmail(), user.getPassword());
    userRepository.update(admin);
    return true;
  }

  public User findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public boolean remove(String id) {
    User user = userRepository.getById(id);
    if (user == null) {
      return false;
    }
    userRepository.delete(id);
    return true;
  }

  public int count() {
    return userRepository.getAll().size();
  }
}
