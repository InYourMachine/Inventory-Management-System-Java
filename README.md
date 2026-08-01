# Inventory Management System

**Java · Swing · MySQL · JDBC**

A desktop application for managing shop inventory with stock tracking, sales, and low-stock alerts.

---

## Features

- ➕ Add new products
- 📋 View all products in a table
- 🔍 Search by ID or name
- ✏️ Update product details
- 🗑️ Delete products
- 📦 Sell product (reduce stock)
- 📥 Restock product (increase stock)
- ⚠️ Low stock alert (threshold: 5 units)
- 💾 Persistent storage using MySQL

---

## Technologies

| Layer | Technology |
| :--- | :--- |
| **Frontend** | Java Swing |
| **Backend** | Java (OOP) |
| **Database** | MySQL |
| **Connection** | JDBC |

---

## OOP Concepts Used

| Concept | Implementation |
| :--- | :--- |
| **Encapsulation** | `Product` class with private fields and getters/setters |
| **Inheritance** | `GroceryProduct` and `ElectronicProduct` extend `Product` |
| **Polymorphism** | `Product` reference holds child types |
| **Interface** | `StockOperations` defines inventory methods |
| **Exception Handling** | `ProductException` for custom error handling |

---

## System Architecture

```
[InventoryGUI] → [InventoryManager] → [DBConnection] → [MySQL]
```

- **InventoryGUI:** Swing-based user interface
- **InventoryManager:** Business logic (implements `StockOperations`)
- **DBConnection:** JDBC connection utility
- **MySQL:** Persistent data storage

---

## Database Schema

```sql
CREATE DATABASE inventory_db;

USE inventory_db;

CREATE TABLE products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    category VARCHAR(20) NOT NULL,
    expiry_date VARCHAR(20) NULL,
    warranty_months INT NULL
);
```

---

## Setup Instructions

### 1. Prerequisites

- Java 8 or higher
- MySQL Server running
- MySQL JDBC driver

### 2. Database Setup

1. Open MySQL and run the schema from `database/schema.sql`
2. Update credentials in `DBConnection.java`

```java
private static final String URL = "jdbc:mysql://localhost:3306/inventory_db";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

### 3. Running the Application

```bash
cd src
javac *.java
java Main
```

---

## License

This project was developed for educational purposes as part of the OOP course (CS-112L).

## About This Project

This project was developed during my 2nd year of BS Data Science as my Object Oriented Programming (OOP) course project. It demonstrates my understanding of Java, Swing GUI, MySQL, and core OOP concepts including encapsulation, inheritance, polymorphism, interfaces, and exception handling.
