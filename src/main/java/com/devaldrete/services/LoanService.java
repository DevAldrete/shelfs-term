package com.devaldrete.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import com.devaldrete.domain.BookItem;
import com.devaldrete.domain.Loan;
import com.devaldrete.domain.Status;
import com.devaldrete.domain.User;
import com.devaldrete.repositories.LoanRepository;

public class LoanService {

  private static final int MAX_LOANS_PER_USER = 2;
  private static final int LOAN_PERIOD_DAYS = 14;

  private LoanRepository loanRepository;
  private UserService userService;
  private BookService bookService;

  public LoanService(LoanRepository loanRepository, UserService userService, BookService bookService) {
    this.loanRepository = loanRepository;
    this.userService = userService;
    this.bookService = bookService;
  }

  public Loan loanBook(String userId, String barcode) {
    User user = userService.getById(userId);
    if (user == null) {
      throw new IllegalArgumentException("User with ID " + userId + " not found.");
    }

    List<Loan> active = loanRepository.findByUserId(userId);
    if (active.size() >= MAX_LOANS_PER_USER) {
      throw new IllegalArgumentException(
          "User " + userId + " has reached the maximum of " + MAX_LOANS_PER_USER + " active loans.");
    }

    BookItem bookItem = bookService.findByBarcode(barcode);
    if (bookItem == null) {
      throw new IllegalArgumentException("Book item with barcode " + barcode + " not found.");
    }

    if (bookItem.getStatus() != Status.AVAILABLE) {
      throw new IllegalArgumentException(
          "Book item with barcode " + barcode + " is not available (status: " + bookItem.getStatus() + ").");
    }

    Instant now = Instant.now();
    Loan loan = new Loan(UUID.randomUUID().toString(), userId, bookItem.getId(), now,
        now.plus(LOAN_PERIOD_DAYS, ChronoUnit.DAYS));

    bookItem.setStatus(Status.BORROWED);
    bookService.updateBookItem(bookItem);
    loanRepository.save(loan);

    return loan;
  }

  public boolean returnBook(String loanId) {
    Loan loan = loanRepository.getById(loanId);
    if (loan == null) {
      return false;
    }
    loanRepository.delete(loanId);
    return true;
  }

  public List<Loan> getAll() {
    return loanRepository.getAll();
  }

  public List<Loan> getByUserId(String userId) {
    return loanRepository.findByUserId(userId);
  }

  public List<Loan> getOverdue() {
    return loanRepository.findOverdue();
  }

  public int count() {
    return loanRepository.count();
  }
}
