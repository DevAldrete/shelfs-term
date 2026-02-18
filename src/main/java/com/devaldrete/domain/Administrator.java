package com.devaldrete.domain;

import java.util.UUID;

public class Administrator extends User {

  public Administrator(String id, String username, String email, String password) {
    super(id, username, email, password, Role.ADMINISTRATOR);
  }

  public Administrator(String username, String email, String password) {
    super(UUID.randomUUID().toString(), username, email, password, Role.ADMINISTRATOR);
  }

  public boolean canManageUsers() {
    return true;
  }

  public boolean canManageBooks() {
    return true;
  }

  public boolean canViewAllLoans() {
    return true;
  }

  public boolean canManageLoans() {
    return true;
  }

  public boolean canUpgradeUserRole() {
    return true;
  }
}
