import java.sql.*;
import java.util.*;

public class ATM {

    // Database class to handle database operations
    static class Database {
        private Connection conn;

        public Database() {
            try {
                // Load the MySQL JDBC Driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Establish the connection
                this.conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/atm_db", "root", "root");
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Method to register a new user with initial balance
        public void registerUser(String userId, String pin, double initialBalance) {
            String sql = "INSERT INTO users (userId, pin, balance) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = this.conn.prepareStatement(sql)) {
                stmt.setString(1, userId);
                stmt.setString(2, pin);
                stmt.setDouble(3, initialBalance); // Initial balance
                stmt.executeUpdate();
                System.out.println("User registered successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Method to get user details
        public User getUser(String userId) {
            String sql = "SELECT * FROM users WHERE userId = ?";
            try (PreparedStatement stmt = this.conn.prepareStatement(sql)) {
                stmt.setString(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return new User(rs.getString("userId"), rs.getString("pin"), rs.getDouble("balance"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        // Method to update balance after deposit/withdrawal
        public void updateBalance(String userId, double newBalance) {
            String sql = "UPDATE users SET balance = ? WHERE userId = ?";
            try (PreparedStatement stmt = this.conn.prepareStatement(sql)) {
                stmt.setDouble(1, newBalance);
                stmt.setString(2, userId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // User class to represent user details and operations
    static class User {
        private String userId;
        private String pin;
        private double balance;
        private ArrayList<String> transactionHistory;

        public User(String userId, String pin, double balance) {
            this.userId = userId;
            this.pin = pin;
            this.balance = balance;
            this.transactionHistory = new ArrayList<>();
        }

        public String getUserId() {
            return userId;
        }

        public boolean validatePin(String pin) {
            return this.pin.equals(pin);
        }

        public double getBalance() {
            return balance;
        }

        // Display balance
        public void viewBalance() {
            System.out.println("Current Balance: " + balance);
        }

        // Unlimited deposit
        public void deposit(double amount, Database db) {
            balance += amount;
            transactionHistory.add("Deposit: " + amount);
            db.updateBalance(userId, balance); // Update balance in DB
            System.out.println("Deposit successful.");
        }

        // Withdrawal with a limitation of 10,000
        public void withdraw(double amount, Database db) {
            if (amount > 10000) {
                System.out.println("Withdrawal failed. Maximum withdrawal amount is 10,000.");
            } else if (amount > balance) {
                System.out.println("Insufficient balance.");
            } else {
                balance -= amount;
                transactionHistory.add("Withdraw: " + amount);
                db.updateBalance(userId, balance); // Update balance in DB
                System.out.println("Withdrawal successful.");
            }
        }

        // Prevent user from sending money to themselves
        public void transfer(String recipientId, double amount, Database db) {
            if (recipientId.equals(this.userId)) {
                System.out.println("Transfer failed. You cannot send money to yourself.");
            } else if (amount > balance) {
                System.out.println("Insufficient balance.");
            } else {
                balance -= amount;
                transactionHistory.add("Transfer to " + recipientId + ": " + amount);
                db.updateBalance(userId, balance); // Update balance in DB
                System.out.println("Transfer successful.");
            }
        }

        public void printTransactionHistory() {
            if (transactionHistory.isEmpty()) {
                System.out.println("No transactions found.");
            } else {
                for (String transaction : transactionHistory) {
                    System.out.println(transaction);
                }
            }
        }
    }

    // Main ATM interface class
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Database db = new Database();
        User currentUser = null;

        // Initial menu for registering or logging in
        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    System.out.println("Register new user:");
                    System.out.print("Enter user ID: ");
                    String newUserId = scanner.nextLine();
                    System.out.print("Enter PIN: ");
                    String newPin = scanner.nextLine();
                    db.registerUser(newUserId, newPin, 0); // Initial balance 0
                    break;

                case 2:
                    // User authentication
                    System.out.print("Enter user ID: ");
                    String userId = scanner.nextLine();
                    System.out.print("Enter PIN: ");
                    String pin = scanner.nextLine();
                    currentUser = db.getUser(userId);
                    if (currentUser == null || !currentUser.validatePin(pin)) {
                        System.out.println("Invalid user ID or PIN. Please try again.");
                    } else {
                        loggedIn = true; // Successful login
                    }
                    break;

                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }

        // Main menu after login
        boolean quit = false;
        while (!quit) {
            System.out.println("\n1. Transactions History");
            System.out.println("2. Withdraw");
            System.out.println("3. Deposit");
            System.out.println("4. Transfer");
            System.out.println("5. View Balance");
            System.out.println("6. Quit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    currentUser.printTransactionHistory();
                    break;
                case 2:
                    System.out.print("Enter amount to withdraw: ");
                    double withdrawAmount = scanner.nextDouble();
                    currentUser.withdraw(withdrawAmount, db);
                    break;
                case 3:
                    System.out.print("Enter amount to deposit: ");
                    double depositAmount = scanner.nextDouble();
                    currentUser.deposit(depositAmount, db);
                    break;
                case 4:
                    System.out.print("Enter recipient user ID: ");
                    String recipientId = scanner.nextLine();
                    System.out.print("Enter amount to transfer: ");
                    double transferAmount = scanner.nextDouble();
                    currentUser.transfer(recipientId, transferAmount, db);
                    break;
                case 5:
                    currentUser.viewBalance();
                    break;
                case 6:
                    quit = true;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
        scanner.close();
    }
}
