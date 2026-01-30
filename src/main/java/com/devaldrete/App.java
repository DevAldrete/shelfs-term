package com.devaldrete;

import java.util.HashMap;

import com.devaldrete.domain.Library;

public class App {

  private static Library library = new Library("1", "Tecmilenio Library", "Av. Tecmilenio");

  public static void main(String[] args) {
    IO.println("Welcome to the Shelfs");
    IO.println("The lightweight library management system");

    IO.println("\nEnjoy your stay!");

    while (true) {
      IO.println("\n=== Menu: Managing Shelfs ===\n");
      IO.println("1. Quick Overview");
      IO.println("2. Manage Books");
      IO.println("3. Manage Users");
      IO.println("4. Manage Loans");
      IO.println("5. Exit");

      String option = IO.readln("\nSelect an option (1-5): ");

      switch (option) {
        case "1":
          IO.println("\nQuick Overview selected.");
          IO.println("\n=== Quick Overview ===\n");
          HashMap<String, Integer> overview = library.quickOverview();
          overview.forEach((k, v) -> {
            IO.println(k + ": " + v);
          });
          break;
        case "2":
          IO.println("\nManage Books selected.");
          library.manageBooks();
          break;
        case "3":
          IO.println("\nManage Users selected.");
          library.manageUsers();
          break;
        case "4":
          IO.println("\nManage Loans selected.");
          library.manageLoans();
          break;
        case "5":
          IO.println("\nExiting the application. Goodbye!");
          System.exit(0);
        default:
          IO.println("Invalid option. Please select a valid option (1-5).");
          break;
      }
    }

  }

}
