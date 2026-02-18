package com.devaldrete.repositories;

import java.util.List;

/**
 * Interface defining the contract for repository operations.
 * All repositories must implement these methods to provide consistent
 * data access patterns across the application.
 *
 * @param <T> The entity type managed by this repository
 */
public interface IRepository<T> {
  /**
   * Persists a new entity to the repository.
   *
   * @param item the entity to save. Must not be null.
   * @throws IllegalArgumentException if item is null
   */
  void save(T item);

  /**
   * Updates an existing entity in the repository.
   *
   * @param item the entity to update. Must not be null.
   * @throws IllegalArgumentException if item is null
   * @throws IllegalStateException    if entity does not exist in repository
   */
  void update(T item);

  /**
   * Deletes an entity from the repository by its ID.
   *
   * @param id the unique identifier of the entity to delete. Must not be null or
   *           empty.
   * @throws IllegalArgumentException if id is null or empty
   * @throws IllegalStateException    if entity with given id does not exist
   */
  void delete(String id);

  /**
   * Retrieves an entity by its ID.
   *
   * @param id the unique identifier of the entity to retrieve. Must not be null
   *           or empty.
   * @return the entity if found, null if not found
   * @throws IllegalArgumentException if id is null or empty
   */
  T getById(String id);

  /**
   * Retrieves all entities from the repository.
   *
   * @return a list containing all entities. Never null, may be empty.
   */
  List<T> getAll();
}
