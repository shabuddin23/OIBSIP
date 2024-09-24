import java.sql.*;
import java.util.Scanner;
import java.util.Random;

public class OnlineReservationSystem {
    public static final String DB_URL = "jdbc:mysql://localhost:3307/ReservationSystem";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "root"; 

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserService userService = new UserService();
        ReservationService reservationService = new ReservationService();
        CancellationService cancellationService = new CancellationService();

        System.out.println("Welcome to the Online Reservation System");
        System.out.print("Enter Login ID: ");
        String loginId = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        if (userService.login(loginId, password)) {
            System.out.println("Login successful!");
            boolean exit = false;
            while (!exit) {
                System.out.println("1. Make a Reservation");
                System.out.println("2. Cancel a Reservation");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice) {
                    case 1:
                        String pnr = reservationService.makeReservation(scanner);
                        System.out.println("Your PNR number is: " + pnr);
                        break;
                    case 2:
                        cancellationService.cancelReservation(scanner);
                        break;
                    case 3:
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        } else {
            System.out.println("Invalid login credentials.");
        }
        scanner.close();
    }
}

class UserService {
    public boolean login(String loginId, String password) {
        try (Connection connection = DriverManager.getConnection(OnlineReservationSystem.DB_URL, OnlineReservationSystem.DB_USER, OnlineReservationSystem.DB_PASSWORD)) {
            String query = "SELECT * FROM Users WHERE loginId = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, loginId);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}


class ReservationService {
    public String makeReservation(Scanner scanner) {
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        System.out.print("Enter train number: ");
        String trainNumber = scanner.nextLine();
        System.out.print("Enter class type: ");
        String classType = scanner.nextLine();
        System.out.print("Enter date of journey (YYYY-MM-DD): ");
        String dateOfJourney = scanner.nextLine();
        System.out.print("Enter from (place): ");
        String from = scanner.nextLine();
        System.out.print("Enter to (destination): ");
        String to = scanner.nextLine();

        // Generate a unique 4-digit PNR
        String pnr = generateUniquePNR();

        try (Connection connection = DriverManager.getConnection(OnlineReservationSystem.DB_URL, OnlineReservationSystem.DB_USER, OnlineReservationSystem.DB_PASSWORD)) {
            String query = "INSERT INTO Reservations (name, trainNumber, classType, dateOfJourney, fromPlace, toPlace) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, trainNumber);
            preparedStatement.setString(3, classType);
            preparedStatement.setDate(4, Date.valueOf(dateOfJourney));
            preparedStatement.setString(5, from);
            preparedStatement.setString(6, to);
            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int reservationId = generatedKeys.getInt(1);
                String pnrQuery = "INSERT INTO Cancellations (pnrNumber, reservationId) VALUES (?, ?)";
                PreparedStatement pnrStatement = connection.prepareStatement(pnrQuery);
                pnrStatement.setString(1, pnr);
                pnrStatement.setInt(2, reservationId);
                pnrStatement.executeUpdate();
            }
            System.out.println("Reservation made successfully for " + name);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pnr;
    }

    // Method to generate a unique 4-digit PNR number
    private String generateUniquePNR() {
        Random random = new Random();
        String pnr = "";
        boolean isUnique = false;

        try (Connection connection = DriverManager.getConnection(OnlineReservationSystem.DB_URL, OnlineReservationSystem.DB_USER, OnlineReservationSystem.DB_PASSWORD)) {
            while (!isUnique) {
                // Generate a random number between 1000 and 9999
                pnr = String.valueOf(1000 + random.nextInt(9000));

                // Check if this PNR number already exists in the Cancellations table
                String checkQuery = "SELECT COUNT(*) FROM Cancellations WHERE pnrNumber = ?";
                PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
                checkStatement.setString(1, pnr);
                ResultSet resultSet = checkStatement.executeQuery();
                resultSet.next();

                // If the PNR is not found, it is unique
                if (resultSet.getInt(1) == 0) {
                    isUnique = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pnr;
    }
}



class CancellationService {
    public void cancelReservation(Scanner scanner) {
        System.out.print("Enter PNR number: ");
        String pnrNumber = scanner.nextLine();

        try (Connection connection = DriverManager.getConnection(OnlineReservationSystem.DB_URL, OnlineReservationSystem.DB_USER, OnlineReservationSystem.DB_PASSWORD)) {
            // First, find the reservation ID using the PNR number
            String selectQuery = "SELECT reservationId FROM Cancellations WHERE pnrNumber = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
            selectStatement.setString(1, pnrNumber);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                int reservationId = resultSet.getInt("reservationId");

                // Now, delete the entry from the Cancellations table
                String deleteCancellationQuery = "DELETE FROM Cancellations WHERE reservationId = ?";
                PreparedStatement deleteCancellationStatement = connection.prepareStatement(deleteCancellationQuery);
                deleteCancellationStatement.setInt(1, reservationId);
                deleteCancellationStatement.executeUpdate();

                // Then, delete the corresponding entry from the Reservations table
                String deleteReservationQuery = "DELETE FROM Reservations WHERE id = ?";
                PreparedStatement deleteReservationStatement = connection.prepareStatement(deleteReservationQuery);
                deleteReservationStatement.setInt(1, reservationId);
                deleteReservationStatement.executeUpdate();

                System.out.println("Reservation cancelled successfully.");
            } else {
                System.out.println("No reservation found for PNR " + pnrNumber);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
