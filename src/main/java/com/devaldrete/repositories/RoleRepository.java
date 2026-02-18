package com.devaldrete.repositories;

import java.util.List;

import com.devaldrete.domain.Role;

public class RoleRepository extends BaseRepository<Role> {

  /**
   * Persists a new role to the repository.
   * Since Role is an enum, persistence is not applicable.
   *
   * @param item the role to save. Must not be null.
   * @throws IllegalArgumentException      if item is null
   * @throws UnsupportedOperationException always, as roles are enum constants and
   *                                       cannot be persisted
   */
  @Override
  public void save(Role item) {
    validateNotNull(item, "Role");
    throw new UnsupportedOperationException("Roles are enum constants and cannot be persisted to the repository");
  }

  /**
   * Updates an existing role in the repository.
   * Since Role is an enum, updates are not applicable.
   *
   * @param item the role to update. Must not be null.
   * @throws IllegalArgumentException      if item is null
   * @throws UnsupportedOperationException always, as roles are immutable enum
   *                                       constants
   */
  @Override
  public void update(Role item) {
    validateNotNull(item, "Role");
    throw new UnsupportedOperationException("Roles are immutable enum constants and cannot be updated");
  }

  /**
   * Deletes a role from the repository by ID.
   * Since Role is an enum, deletion is not applicable.
   *
   * @param id the unique identifier of the role to delete. Must not be null or
   *           empty.
   * @throws IllegalArgumentException      if id is null or empty
   * @throws UnsupportedOperationException always, as roles are enum constants and
   *                                       cannot be deleted
   */
  @Override
  public void delete(String id) {
    validateNotEmpty(id, "Role ID");
    throw new UnsupportedOperationException("Roles are enum constants and cannot be deleted from the repository");
  }

  /**
   * Retrieves a role by its ID.
   * Converts the provided ID string to the corresponding Role enum value.
   *
   * @param id the unique identifier of the role (should match enum constant
   *           name). Must not be null or empty.
   * @return the role if found, null if not found
   * @throws IllegalArgumentException if id is null or empty
   */
  @Override
  public Role getById(String id) {
    validateNotEmpty(id, "Role ID");

    try {
      return Role.valueOf(id.toUpperCase().trim());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Retrieves all roles from the repository.
   * Returns a list of all enum constants defined in the Role enum.
   *
   * @return a list containing all available roles. Never null, never empty.
   */
  @Override
  public List<Role> getAll() {
    return java.util.Arrays.asList(Role.values());
  }

  /**
   * Extracts the ID from a role entity.
   * For enum roles, the ID is the enum constant's name.
   *
   * @param item the role to extract ID from. Must not be null.
   * @return the unique identifier (name) of the role
   * @throws IllegalArgumentException if item is null
   */
  @Override
  protected String getId(Role item) {
    validateNotNull(item, "Role");
    return item.name();
  }

  /**
   * Determines if a role with the given ID exists in the repository.
   * Checks if the provided ID matches any Role enum constant name.
   *
   * @param id the unique identifier to check (case-insensitive). Must not be null
   *           or empty.
   * @return true if a matching role exists, false otherwise
   */
  @Override
  protected boolean exists(String id) {
    if (id == null || id.trim().isEmpty()) {
      return false;
    }

    try {
      Role.valueOf(id.toUpperCase().trim());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Finds a role by name, performing a case-insensitive comparison.
   * Convenience method for retrieving roles by name.
   *
   * @param name the role name to search for (case-insensitive). Must not be null
   *             or empty.
   * @return the role if found, null if not found
   * @throws IllegalArgumentException if name is null or empty
   */
  public Role findByName(String name) {
    validateNotEmpty(name, "Role name");
    return getById(name);
  }

  /**
   * Counts the total number of available roles in the repository.
   * Since Role is an enum, this returns the number of enum constants.
   *
   * @return the total number of available roles
   */
  public int count() {
    return Role.values().length;
  }

  /**
   * Checks if a role name is valid (exists as an enum constant).
   * Performs case-insensitive comparison.
   *
   * @param name the role name to validate. Must not be null or empty.
   * @return true if the role name is valid, false otherwise
   * @throws IllegalArgumentException if name is null or empty
   */
  public boolean isValidRoleName(String name) {
    validateNotEmpty(name, "Role name");
    return exists(name);
  }
}
