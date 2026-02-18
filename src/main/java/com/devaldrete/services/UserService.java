package com.devaldrete.services;

import java.util.List;
import java.util.UUID;

import com.devaldrete.domain.Administrator;
import com.devaldrete.domain.Member;
import com.devaldrete.domain.Role;
import com.devaldrete.domain.User;
import com.devaldrete.repositories.UserRepository;

public class UserService {

  private UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User register(String username, String email, String password, Role role) {
    String id = UUID.randomUUID().toString();
    User user = role == Role.ADMINISTRATOR
        ? new Administrator(id, username, email, password)
        : new Member(id, username, email, password);
    userRepository.save(user);
    return user;
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
    Administrator admin = new Administrator(
        user.getId(), user.getUsername(), user.getEmail(), user.getPassword());
    userRepository.update(admin);
    return true;
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
