# Java Banking System

A desktop-based banking application developed using Core Java (Swing) and MySQL.
The system supports basic banking operations with database integration through JDBC and stored procedures.

---

## Features

* User authentication (login system)
* Create account (Savings / Current)
* Deposit and withdraw money
* Fund transfer between accounts
* View transaction history
* Database connectivity using MySQL

---

## Technologies Used

* Java (Core Java)
* Java Swing
* MySQL
* JDBC 

---

## Project Structure

```id="p1o2z3"
Account.java
BankService.java
DBConnection.java
BankingApp.java
TestApp.java

lib/
  mysql-connector-j-9.6.0.jar
```

---

## Prerequisites

* Java JDK installed
* MySQL server running
* MySQL Connector J (JAR file)

---

## How to Run

1. Compile:

```id="a9b8c7"
javac -cp ".;lib/mysql-connector-j-9.6.0.jar" *.java
```

2. Run:

```id="d4e5f6"
java -cp ".;lib/mysql-connector-j-9.6.0.jar" BankingApp
```

---


