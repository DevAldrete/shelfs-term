package com.devaldrete.repositories;

import java.util.ArrayList;
import java.util.List;

import com.devaldrete.domain.Loan;

/**
 * LoanRepository manages the persistence of Loan entities using an in-memory
 * list.
 * 
 * This repository provides CRUD operations for loans with proper validation and
 * error handling. All loans are stored in memory and persisted throughout the
 * application lifecycle.
 * 
 * Error handling:
 * - Validates all input parameters (null/empty checks)
 * - Throws IllegalArgumentException for invalid inputs
 * - Throws IllegalStateException when attempting invalid state transitions
 */
public class LoanRepository extends BaseRepository<Loan> {
  /**
   * Constructs a new LoanRepository with an empty loan collection.
   */
  public LoanRepository() {
    super();
  }

  /**
   * Persists a new loan to the repository.
   * 
   * @param item the loan to save. Must not be null.
   * @throws IllegalArgumentException if item is null or loan already exists with
   *                                  same ID
   */
  @Override
  public void save(Loan item) {
    validateNotNull(item, "Loan");
    validateNotNull(item.getId(), "Loan ID");

    if (exists(item.getId())) {
      throw new IllegalArgumentException("Loan with ID " + item.getId() + " already exists");
    }

    getAll().add(item);
  }

  /**
   * Updates an existing loan in the repository.
   * 
   * @param item the loan to update. Must not be null.
   * @throws IllegalArgumentException if item is null
   * @throws IllegalStateException    if loan with given ID does not exist
   */
  @Override
  public void update(Loan item) {
    validateNotNull(item, "Loan");
    validateNotNull(item.getId(), "Loan ID");

    if (!exists(item.getId())) {
      throw new IllegalStateException("Cannot update: Loan with ID " + item.getId() + " does not exist");
    }

    for (int i = 0; i < getAll().size(); i++) {
      if (getAll().get(i).getId().equals(item.getId())) {
        getAll().set(i, item);
        return;
      }
    }
  }

  /**
   * Deletes a loan from the repository by its ID.
   * 
   * @param id the unique identifier of the loan to delete. Must not be null or
   *           empty.
   * @throws IllegalArgumentException if id is null or empty
   * @throws IllegalStateException    if loan with given ID does not exist
   */
  @Override
  public void delete(String id) {
    validateNotEmpty(id, "Loan ID");

    if (!exists(id)) {
      throw new IllegalStateException("Cannot delete: Loan with ID " + id + " does not exist");
    }

    getAll().removeIf(loan -> loan.getId().equals(id));
  }

  /**
   * Retrieves a loan by its ID.
   * 
   * @param id the unique identifier of the loan to retrieve. Must not be null or
   *           empty.
   * @return the loan if found, null if not found
   * @throws IllegalArgumentException if id is null or empty
   */
  @Override
  public Loan getById(String id) {
    validateNotEmpty(id, "Loan ID");

    return getAll().stream()
        .filter(loan -> loan.getId().equals(id))
        .findFirst()
        .orElse(null);
  }

  /**
   * Retrieves all loans from the repository.
   * 
   * @return a list containing all loans. Never null, may be empty.
   */
  @Override
  public List<Loan> getAll() {
    return new ArrayList<>(getAll());
  }

  /**
   * Extracts the ID from a loan entity.
   * 
   * @param item the loan to extract ID from. Must not be null.
   * @return the unique identifier of the loan
   */
  @Override
  protected String getId(Loan item) {
    validateNotNull(item, "Loan");
    return item.getId();
  }

  /**
   * Determines if a loan with the given ID exists in the repository.
   * 
   * @param id the unique identifier to check. Must not be null or empty.
   * @return true if loan exists, false otherwise
   * @throws IllegalArgumentException if id is null or empty
   */
  @Override
  protected boolean exists(String id) {
    validateNotEmpty(id, "Loan ID");

    return getAll().stream()
        .anyMatch(loan -> loan.getId().equals(id));
  }
}
