package com.devaldrete.repositories;

import java.util.ArrayList;
import java.util.List;

import com.devaldrete.domain.BookDefinition;
import com.devaldrete.domain.BookItem;

public class BookRepository extends BaseRepository<BookItem> {

  private List<BookItem> bookItems;
  private List<BookDefinition> bookDefinitions;

  public BookRepository() {
    super();
    this.bookItems = new ArrayList<>();
    this.bookDefinitions = new ArrayList<>();
  }

  public void saveBookDefinition(BookDefinition bookDefinition) {
    validateNotNull(bookDefinition, "BookDefinition");
    validateNotEmpty(bookDefinition.getId(), "BookDefinition ID");

    if (!exists(bookDefinition.getId())) {
      bookDefinitions.add(bookDefinition);
    }
  }

  public void updateBookDefinition(BookDefinition bookDefinition) {
    validateNotNull(bookDefinition, "BookDefinition");
    validateNotEmpty(bookDefinition.getId(), "BookDefinition ID");

    bookDefinitions.removeIf(bd -> bd.getId().equals(bookDefinition.getId()));
    bookDefinitions.add(bookDefinition);
  }

  public BookDefinition getBookDefinitionById(String id) {
    validateNotEmpty(id, "BookDefinition ID");

    return bookDefinitions.stream()
        .filter(bd -> bd.getId().equals(id))
        .findFirst()
        .orElse(null);
  }

  public List<BookDefinition> getAllBookDefinitions() {
    return new ArrayList<>(bookDefinitions);
  }

  public boolean deleteBookDefinition(String id) {
    validateNotEmpty(id, "BookDefinition ID");

    return bookDefinitions.removeIf(bd -> bd.getId().equals(id));
  }

  @Override
  public void save(BookItem item) {
    validateNotNull(item, "BookItem");
    validateNotEmpty(item.getId(), "BookItem ID");
    validateNotEmpty(item.getBookDefId(), "BookDefinition ID");

    if (getBookDefinitionById(item.getBookDefId()) == null) {
      throw new IllegalArgumentException("BookDefinition with ID " + item.getBookDefId() + " does not exist");
    }

    if (!exists(item.getId())) {
      bookItems.add(item);
    }
  }

  @Override
  public void update(BookItem item) {
    validateNotNull(item, "BookItem");
    validateNotEmpty(item.getId(), "BookItem ID");
    validateNotEmpty(item.getBookDefId(), "BookDefinition ID");

    if (getBookDefinitionById(item.getBookDefId()) == null) {
      throw new IllegalArgumentException("BookDefinition with ID " + item.getBookDefId() + " does not exist");
    }

    bookItems.removeIf(bi -> bi.getId().equals(item.getId()));
    bookItems.add(item);
  }

  @Override
  public void delete(String id) {
    validateNotEmpty(id, "BookItem ID");

    bookItems.removeIf(bi -> bi.getId().equals(id));
  }

  @Override
  public BookItem getById(String id) {
    validateNotEmpty(id, "BookItem ID");

    return bookItems.stream()
        .filter(bi -> bi.getId().equals(id))
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<BookItem> getAll() {
    return new ArrayList<>(bookItems);
  }

  public List<BookItem> getByBookDefinitionId(String bookDefId) {
    validateNotEmpty(bookDefId, "BookDefinition ID");

    return bookItems.stream()
        .filter(bi -> bi.getBookDefId().equals(bookDefId))
        .toList();
  }

  public BookItem findByBarcode(String barcode) {
    validateNotEmpty(barcode, "Barcode");

    return bookItems.stream()
        .filter(bi -> bi.getBarcode().equalsIgnoreCase(barcode))
        .findFirst()
        .orElse(null);
  }

  public BookDefinition findByISBN(String isbn) {
    validateNotEmpty(isbn, "ISBN");

    return bookDefinitions.stream()
        .filter(bd -> bd.getIsbn().equals(isbn))
        .findFirst()
        .orElse(null);
  }

  public List<BookDefinition> findByTitle(String title) {
    validateNotEmpty(title, "Title");

    String lower = title.toLowerCase();
    return bookDefinitions.stream()
        .filter(bd -> bd.getTitle().toLowerCase().contains(lower))
        .toList();
  }

  public List<BookDefinition> findByAuthor(String author) {
    validateNotEmpty(author, "Author");

    String lower = author.toLowerCase();
    return bookDefinitions.stream()
        .filter(bd -> bd.getAuthor().toLowerCase().contains(lower))
        .toList();
  }

  @Override
  protected String getId(BookItem item) {
    validateNotNull(item, "BookItem");

    return item.getId();
  }

  @Override
  protected boolean exists(String id) {
    validateNotEmpty(id, "ID");

    return bookItems.stream()
        .anyMatch(bi -> bi.getId().equals(id));
  }
}
