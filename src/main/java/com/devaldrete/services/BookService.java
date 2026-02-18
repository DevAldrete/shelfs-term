package com.devaldrete.services;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.devaldrete.domain.BookDefinition;
import com.devaldrete.domain.BookItem;
import com.devaldrete.domain.Status;
import com.devaldrete.repositories.BookRepository;
import com.devaldrete.utils.BarcodeGenerator;

public class BookService {

  private BookRepository bookRepository;

  public BookService(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public BookDefinition addBook(String isbn, String title, String author, String publisher) {
    BookDefinition existing = bookRepository.findByISBN(isbn);

    if (existing != null) {
      addBookItem(existing.getId());
      return existing;
    }

    String id = UUID.randomUUID().toString();
    BookDefinition newDef = new BookDefinition(id, title, author, isbn, publisher);
    bookRepository.saveBookDefinition(newDef);
    addBookItem(newDef.getId());

    return newDef;
  }

  public BookItem addBookItem(String bookDefId) {
    String id = UUID.randomUUID().toString();
    String barcode = BarcodeGenerator.generateBarcode();
    BookItem item = new BookItem(id, barcode, bookDefId, Status.AVAILABLE, Instant.now());
    bookRepository.save(item);
    return item;
  }

  public boolean removeBookItem(String barcode) {
    BookItem item = bookRepository.findByBarcode(barcode);
    if (item == null) {
      return false;
    }
    bookRepository.delete(item.getId());
    return true;
  }

  public List<BookDefinition> getAllDefinitions() {
    return bookRepository.getAllBookDefinitions();
  }

  public List<BookItem> getAvailableCopies(String isbn) {
    BookDefinition def = bookRepository.findByISBN(isbn);
    if (def == null) {
      return List.of();
    }
    return bookRepository.getByBookDefinitionId(def.getId()).stream()
        .filter(b -> b.getStatus() == Status.AVAILABLE)
        .toList();
  }

  public BookDefinition findByISBN(String isbn) {
    return bookRepository.findByISBN(isbn);
  }

  public BookItem findByBarcode(String barcode) {
    return bookRepository.findByBarcode(barcode);
  }

  public boolean updateDefinition(String isbn, String title, String author, String publisher) {
    BookDefinition current = bookRepository.findByISBN(isbn);
    if (current == null) {
      return false;
    }
    current.setTitle(title);
    current.setAuthor(author);
    current.setPublisher(publisher);
    bookRepository.updateBookDefinition(current);
    return true;
  }

  public void updateBookItem(BookItem item) {
    bookRepository.update(item);
  }

  public int countItems() {
    return bookRepository.getAll().size();
  }

  public int countDefinitions() {
    return bookRepository.getAllBookDefinitions().size();
  }
}
