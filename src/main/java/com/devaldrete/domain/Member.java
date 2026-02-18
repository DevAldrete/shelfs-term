package com.devaldrete.domain;

import java.util.List;
import java.util.UUID;

public class Member extends User {

  public Member(String id, String username, String email, String password) {
    super(id, username, email, password, Role.MEMBER);
  }

  public Member(String username, String email, String password) {
    super(UUID.randomUUID().toString(), username, email, password, Role.MEMBER);
  }

  public boolean canViewOwnLoans() {
    return true;
  }

  public boolean canViewAllBooks() {
    return true;
  }

  public boolean canLoanBooks() {
    return true;
  }

  public boolean canReturnBooks() {
    return true;
  }

  // Members can only access their own loans
  public List<Loan> getAccessibleLoans(List<Loan> allLoans) {
    return allLoans.stream()
        .filter(loan -> loan.getUserId().equals(this.getId()))
        .toList();
  }
}
