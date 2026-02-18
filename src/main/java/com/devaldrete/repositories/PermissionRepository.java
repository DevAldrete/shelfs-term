package com.devaldrete.repositories;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.devaldrete.domain.Permission;

/**
 * Initializes the PermissionRepository with an empty permissions list.
 * Manages Permission instances with full CRUD operations and soft-delete
 * support.
 */
public class PermissionRepository extends BaseRepository<Permission> {
  private List<Permission> permissions;

  public PermissionRepository() {
    super();
    this.permissions = new ArrayList<>();
  }

  /**
   * Persists a new permission to the repository.
   * 
   * @param item the permission to save. Must not be null with valid id.
   * @throws IllegalArgumentException if item is null, id is null/empty, or
   *                                  permission already exists
   */
  @Override
  public void save(Permission item) {
    validateNotNull(item, "Permission");
    validateNotNull(item.getId(), "Permission ID");
    validateNotEmpty(item.getId(), "Permission ID");

    if (exists(item.getId())) {
      throw new IllegalArgumentException("Permission with ID '" + item.getId() + "' already exists");
    }

    item.setCreatedAt(Instant.now());
    item.setUpdatedAt(Instant.now());
    permissions.add(item);
  }

  /**
   * Updates an existing permission in the repository.
   * Updates the updatedAt timestamp automatically.
   * 
   * @param item the permission to update. Must not be null with valid id.
   * @throws IllegalArgumentException if item is null or id is null/empty
   * @throws IllegalStateException    if permission does not exist in repository
   */
  @Override
  public void update(Permission item) {
    validateNotNull(item, "Permission");
    validateNotNull(item.getId(), "Permission ID");
    validateNotEmpty(item.getId(), "Permission ID");

    if (!exists(item.getId())) {
      throw new IllegalStateException("Permission with ID '" + item.getId() + "' not found");
    }

    item.setUpdatedAt(Instant.now());
    for (int i = 0; i < permissions.size(); i++) {
      if (permissions.get(i).getId().equals(item.getId())) {
        permissions.set(i, item);
        return;
      }
    }
  }

  /**
   * Deletes a permission from the repository by ID.
   * Performs a hard delete from the collection.
   * 
   * @param id the unique identifier of the permission to delete. Must not be null
   *           or empty.
   * @throws IllegalArgumentException if id is null or empty
   * @throws IllegalStateException    if permission with given id does not exist
   */
  @Override
  public void delete(String id) {
    validateNotEmpty(id, "Permission ID");

    if (!exists(id)) {
      throw new IllegalStateException("Permission with ID '" + id + "' not found");
    }

    permissions.removeIf(permission -> permission.getId().equals(id));
  }

  /**
   * Retrieves a permission by their ID.
   * 
   * @param id the unique identifier of the permission. Must not be null or empty.
   * @return the permission if found, null if not found
   * @throws IllegalArgumentException if id is null or empty
   */
  @Override
  public Permission getById(String id) {
    validateNotEmpty(id, "Permission ID");

    return permissions.stream().filter(permission -> permission.getId().equals(id)).findFirst().orElse(null);
  }

  /**
   * Retrieves all permissions from the repository.
   * 
   * @return a list containing all permissions. Never null, may be empty.
   */
  @Override
  public List<Permission> getAll() {
    return new ArrayList<>(permissions);
  }

  /**
   * Extracts the ID from a permission entity.
   * 
   * @param item the permission to extract ID from
   * @return the unique identifier of the permission
   * @throws IllegalArgumentException if item is null
   */
  @Override
  protected String getId(Permission item) {
    validateNotNull(item, "Permission");
    return item.getId();
  }

  /**
   * Determines if a permission with the given ID exists in the repository.
   * 
   * @param id the unique identifier to check
   * @return true if permission exists, false otherwise
   */
  @Override
  protected boolean exists(String id) {
    if (id == null || id.trim().isEmpty()) {
      return false;
    }

    return permissions.stream().anyMatch(permission -> permission.getId().equals(id));
  }

  /**
   * Finds a permission by name.
   * 
   * @param name the permission name to search for. Must not be null or empty.
   * @return the permission if found, null if not found
   * @throws IllegalArgumentException if name is null or empty
   */
  public Permission findByName(String name) {
    validateNotEmpty(name, "Permission name");

    return permissions.stream().filter(permission -> permission.getName().equals(name)).findFirst().orElse(null);
  }

  /**
   * Counts the total number of permissions in the repository.
   * 
   * @return the number of permissions
   */
  public int count() {
    return permissions.size();
  }

  /**
   * Checks if a permission name is already in use.
   * 
   * @param name the permission name to check. Must not be null or empty.
   * @return true if permission name exists, false otherwise
   * @throws IllegalArgumentException if name is null or empty
   */
  public boolean nameExists(String name) {
    validateNotEmpty(name, "Permission name");

    return permissions.stream().anyMatch(permission -> permission.getName().equals(name));
  }
}
