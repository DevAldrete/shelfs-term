package com.devaldrete.repositories;

/**
 * Abstract base class providing common repository functionality and error
 * handling.
 * Subclasses should implement storage logic and validation specific to their
 * entity type.
 * This class handles argument validation and provides helper methods for
 * consistent error reporting.
 *
 * @param <T> The entity type managed by this repository
 */
public abstract class BaseRepository<T> implements IRepository<T> {

  /**
   * Validates that a required string parameter is not null or empty.
   *
   * @param value     the value to validate
   * @param paramName the name of the parameter for error messages
   * @throws IllegalArgumentException if value is null or empty
   */
  protected void validateNotEmpty(String value, String paramName) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(paramName + " must not be null or empty");
    }
  }

  /**
   * Validates that a required object parameter is not null.
   *
   * @param value     the value to validate
   * @param paramName the name of the parameter for error messages
   * @throws IllegalArgumentException if value is null
   */
  protected void validateNotNull(Object value, String paramName) {
    if (value == null) {
      throw new IllegalArgumentException(paramName + " must not be null");
    }
  }

  /**
   * Extracts the ID from an entity for use in repository operations.
   * Subclasses must implement this to provide access to entity IDs.
   *
   * @param item the entity to extract ID from
   * @return the unique identifier of the entity
   */
  protected abstract String getId(T item);

  /**
   * Determines if an entity with the given ID exists in the repository.
   * Subclasses must implement this method.
   *
   * @param id the unique identifier to check
   * @return true if entity exists, false otherwise
   */
  protected abstract boolean exists(String id);
}
