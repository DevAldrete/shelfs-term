package com.devaldrete.domain;

public class Administrator extends User {

  public Administrator(String id, String username, String email, String password) {
    super(id, username, email, password, Role.ADMINISTRATOR);
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
