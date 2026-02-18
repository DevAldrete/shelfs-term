package com.devaldrete.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.devaldrete.domain.Administrator;
import com.devaldrete.domain.BookDefinition;
import com.devaldrete.domain.BookItem;
import com.devaldrete.domain.Loan;
import com.devaldrete.domain.Member;
import com.devaldrete.domain.Status;
import com.devaldrete.domain.User;

/**
 * Handles persistence of library data to JSON files stored in the {@code data/}
 * directory (relative to the working directory).
 *
 * <p>Three files are maintained:
 * <ul>
 *   <li>{@code data/users.json}         — users (administrators and members)
 *   <li>{@code data/books.json}         — book definitions and book items
 *   <li>{@code data/loans.json}         — active loans
 * </ul>
 *
 * <p>No external libraries are used; JSON is produced and consumed with simple
 * string manipulation so that {@code pom.xml} stays unchanged.
 */
public class PersistenceService {

  private static final Path DATA_DIR = Paths.get("data");
  private static final Path USERS_FILE = DATA_DIR.resolve("users.json");
  private static final Path BOOKS_FILE = DATA_DIR.resolve("books.json");
  private static final Path LOANS_FILE = DATA_DIR.resolve("loans.json");

  // -------------------------------------------------------------------------
  // Save
  // -------------------------------------------------------------------------

  /**
   * Persists the full state of all three service data stores to disk.
   */
  public void saveAll(UserService userService, BookService bookService, LoanService loanService) {
    ensureDataDir();
    saveUsers(userService.getAll());
    saveBooks(bookService.getAllDefinitions(), bookService.getAllItems());
    saveLoans(loanService.getAll());
  }

  private void saveUsers(List<User> users) {
    StringBuilder sb = new StringBuilder("[\n");
    for (int i = 0; i < users.size(); i++) {
      User u = users.get(i);
      sb.append("  {\n");
      sb.append("    \"id\": ").append(jsonStr(u.getId())).append(",\n");
      sb.append("    \"username\": ").append(jsonStr(u.getUsername())).append(",\n");
      sb.append("    \"email\": ").append(jsonStr(u.getEmail())).append(",\n");
      sb.append("    \"password\": ").append(jsonStr(u.getPassword())).append(",\n");
      sb.append("    \"role\": ").append(jsonStr(u.getRole().name())).append("\n");
      sb.append("  }");
      if (i < users.size() - 1) sb.append(",");
      sb.append("\n");
    }
    sb.append("]");
    write(USERS_FILE, sb.toString());
  }

  private void saveBooks(List<BookDefinition> defs, List<BookItem> items) {
    StringBuilder sb = new StringBuilder("{\n");

    // definitions
    sb.append("  \"definitions\": [\n");
    for (int i = 0; i < defs.size(); i++) {
      BookDefinition d = defs.get(i);
      sb.append("    {\n");
      sb.append("      \"id\": ").append(jsonStr(d.getId())).append(",\n");
      sb.append("      \"title\": ").append(jsonStr(d.getTitle())).append(",\n");
      sb.append("      \"author\": ").append(jsonStr(d.getAuthor())).append(",\n");
      sb.append("      \"isbn\": ").append(jsonStr(d.getIsbn())).append(",\n");
      sb.append("      \"publisher\": ").append(jsonStr(d.getPublisher())).append("\n");
      sb.append("    }");
      if (i < defs.size() - 1) sb.append(",");
      sb.append("\n");
    }
    sb.append("  ],\n");

    // items
    sb.append("  \"items\": [\n");
    for (int i = 0; i < items.size(); i++) {
      BookItem item = items.get(i);
      sb.append("    {\n");
      sb.append("      \"id\": ").append(jsonStr(item.getId())).append(",\n");
      sb.append("      \"barcode\": ").append(jsonStr(item.getBarcode())).append(",\n");
      sb.append("      \"bookDefId\": ").append(jsonStr(item.getBookDefId())).append(",\n");
      sb.append("      \"status\": ").append(jsonStr(item.getStatus().name())).append(",\n");
      sb.append("      \"acquisitionDate\": ").append(jsonStr(item.getAcquisitionDate().toString())).append("\n");
      sb.append("    }");
      if (i < items.size() - 1) sb.append(",");
      sb.append("\n");
    }
    sb.append("  ]\n");

    sb.append("}");
    write(BOOKS_FILE, sb.toString());
  }

  private void saveLoans(List<Loan> loans) {
    StringBuilder sb = new StringBuilder("[\n");
    for (int i = 0; i < loans.size(); i++) {
      Loan l = loans.get(i);
      sb.append("  {\n");
      sb.append("    \"id\": ").append(jsonStr(l.getId())).append(",\n");
      sb.append("    \"userId\": ").append(jsonStr(l.getUserId())).append(",\n");
      sb.append("    \"bookId\": ").append(jsonStr(l.getBookId())).append(",\n");
      sb.append("    \"createdAt\": ").append(jsonStr(l.getCreatedAt().toString())).append(",\n");
      sb.append("    \"dueDate\": ").append(jsonStr(l.getDueDate().toString())).append("\n");
      sb.append("  }");
      if (i < loans.size() - 1) sb.append(",");
      sb.append("\n");
    }
    sb.append("]");
    write(LOANS_FILE, sb.toString());
  }

  // -------------------------------------------------------------------------
  // Load
  // -------------------------------------------------------------------------

  /**
   * Loads persisted data into the given services.
   * If any file is missing the corresponding store is left as-is (empty).
   */
  public void loadAll(UserService userService, BookService bookService, LoanService loanService) {
    if (Files.exists(USERS_FILE)) {
      loadUsers(userService);
    }
    if (Files.exists(BOOKS_FILE)) {
      loadBooks(bookService);
    }
    if (Files.exists(LOANS_FILE)) {
      loadLoans(loanService);
    }
  }

  private void loadUsers(UserService userService) {
    String json = read(USERS_FILE);
    List<String> objects = parseJsonArray(json);
    for (String obj : objects) {
      String id = field(obj, "id");
      String username = field(obj, "username");
      String email = field(obj, "email");
      String password = field(obj, "password");
      String role = field(obj, "role");

      User user;
      if ("ADMINISTRATOR".equals(role)) {
        user = new Administrator(id, username, email, password);
      } else {
        user = new Member(id, username, email, password);
      }
      userService.save(user);
    }
  }

  private void loadBooks(BookService bookService) {
    String json = read(BOOKS_FILE);

    // Split the top-level object into "definitions" and "items" arrays
    String defsSection = extractArraySection(json, "definitions");
    String itemsSection = extractArraySection(json, "items");

    // Load definitions first (items reference them)
    for (String obj : parseJsonArray(defsSection)) {
      String id = field(obj, "id");
      String title = field(obj, "title");
      String author = field(obj, "author");
      String isbn = field(obj, "isbn");
      String publisher = field(obj, "publisher");
      bookService.saveDefinition(new BookDefinition(id, title, author, isbn, publisher));
    }

    // Load items
    for (String obj : parseJsonArray(itemsSection)) {
      String id = field(obj, "id");
      String barcode = field(obj, "barcode");
      String bookDefId = field(obj, "bookDefId");
      String statusStr = field(obj, "status");
      String acquisitionDateStr = field(obj, "acquisitionDate");

      Status status = Status.valueOf(statusStr);
      Instant acquisitionDate = Instant.parse(acquisitionDateStr);

      bookService.saveItem(new BookItem(id, barcode, bookDefId, status, acquisitionDate));
    }
  }

  private void loadLoans(LoanService loanService) {
    String json = read(LOANS_FILE);
    for (String obj : parseJsonArray(json)) {
      String id = field(obj, "id");
      String userId = field(obj, "userId");
      String bookId = field(obj, "bookId");
      Instant createdAt = Instant.parse(field(obj, "createdAt"));
      Instant dueDate = Instant.parse(field(obj, "dueDate"));
      loanService.saveLoan(new Loan(id, userId, bookId, createdAt, dueDate));
    }
  }

  // -------------------------------------------------------------------------
  // Minimal JSON helpers  (no external library)
  // -------------------------------------------------------------------------

  /** Wraps a value in JSON double-quotes, escaping backslash and double-quote. */
  private String jsonStr(String value) {
    if (value == null) return "null";
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }

  /**
   * Splits a JSON array string {@code [ {...}, {...} ]} into individual object
   * strings.  Only handles flat objects (no nested arrays/objects inside values).
   */
  private List<String> parseJsonArray(String json) {
    List<String> results = new ArrayList<>();
    if (json == null || json.isBlank()) return results;

    int depth = 0;
    int start = -1;
    for (int i = 0; i < json.length(); i++) {
      char c = json.charAt(i);
      if (c == '{') {
        if (depth == 0) start = i;
        depth++;
      } else if (c == '}') {
        depth--;
        if (depth == 0 && start >= 0) {
          results.add(json.substring(start, i + 1));
          start = -1;
        }
      }
    }
    return results;
  }

  /**
   * Extracts the JSON array assigned to {@code key} from a top-level JSON
   * object string.  For example, given {@code "items"} it returns the raw
   * {@code [ ... ]} substring for that key.
   */
  private String extractArraySection(String json, String key) {
    String marker = "\"" + key + "\"";
    int keyIdx = json.indexOf(marker);
    if (keyIdx < 0) return "[]";

    int bracketStart = json.indexOf('[', keyIdx + marker.length());
    if (bracketStart < 0) return "[]";

    int depth = 0;
    for (int i = bracketStart; i < json.length(); i++) {
      char c = json.charAt(i);
      if (c == '[') depth++;
      else if (c == ']') {
        depth--;
        if (depth == 0) return json.substring(bracketStart, i + 1);
      }
    }
    return "[]";
  }

  /**
   * Extracts the string value for {@code key} from a flat JSON object string.
   * Only handles string values (surrounded by double-quotes).
   */
  private String field(String obj, String key) {
    String marker = "\"" + key + "\"";
    int keyIdx = obj.indexOf(marker);
    if (keyIdx < 0) return "";

    int colon = obj.indexOf(':', keyIdx + marker.length());
    if (colon < 0) return "";

    // Skip whitespace after colon
    int valStart = colon + 1;
    while (valStart < obj.length() && Character.isWhitespace(obj.charAt(valStart))) {
      valStart++;
    }
    if (valStart >= obj.length()) return "";

    if (obj.charAt(valStart) == '"') {
      // Quoted string — find the closing quote (skip escaped quotes)
      int i = valStart + 1;
      StringBuilder sb = new StringBuilder();
      while (i < obj.length()) {
        char c = obj.charAt(i);
        if (c == '\\' && i + 1 < obj.length()) {
          char next = obj.charAt(i + 1);
          if (next == '"') { sb.append('"'); i += 2; continue; }
          if (next == '\\') { sb.append('\\'); i += 2; continue; }
        }
        if (c == '"') break;
        sb.append(c);
        i++;
      }
      return sb.toString();
    }
    // Unquoted value (number, boolean, null) — read until comma or }
    int end = valStart;
    while (end < obj.length() && obj.charAt(end) != ',' && obj.charAt(end) != '}') {
      end++;
    }
    return obj.substring(valStart, end).trim();
  }

  // -------------------------------------------------------------------------
  // File I/O helpers
  // -------------------------------------------------------------------------

  private void ensureDataDir() {
    try {
      Files.createDirectories(DATA_DIR);
    } catch (IOException e) {
      throw new RuntimeException("Could not create data directory: " + e.getMessage(), e);
    }
  }

  private void write(Path path, String content) {
    try {
      Files.writeString(path, content, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Could not write file " + path + ": " + e.getMessage(), e);
    }
  }

  private String read(Path path) {
    try {
      return Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Could not read file " + path + ": " + e.getMessage(), e);
    }
  }
}
