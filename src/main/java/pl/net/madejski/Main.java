package pl.net.madejski;

import sun.security.krb5.SCDynamicStoreConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    private static void initializeSchema(Connection connection) {
        try {
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS Pracownicy (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "imie TEXT," +
                    "nazwisko TEXT," +
                    "kraj TEXT," +
                    "placa INT" +
                    ");");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean insertEmployeeToDatabase(Connection connection, Employee employee) {
        try {
            assert employee != null;
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Pracownicy (`imie`, `nazwisko`, `kraj`, `placa`) " +
                    "VALUES (?, ?, ?, ?)");

            preparedStatement.setString(1, employee.getImie());
            preparedStatement.setString(2, employee.getNazwisko());
            preparedStatement.setString(3, employee.getKraj());
            preparedStatement.setInt(4, employee.getPlaca());

            return preparedStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void populateDatabase(Connection connection) {
        try {
            URL url = new URL("http://szgrabowski.kis.p.lodz.pl/zpo18/dane.txt");
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while((line = bufferedReader.readLine()) != null) {
                String[] columns = line.split("\\s");
                insertEmployeeToDatabase(connection, new Employee(null, columns[0], columns[1], columns[2], Integer.parseInt(columns[3])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean checkIfTableExists(Connection connection, String tableName) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM information_schema.tables WHERE TABLE_NAME=? AND TABLE_SCHEMA='zpo'");

            preparedStatement.setString(1, tableName);

            ResultSet resultSet = preparedStatement.executeQuery();
            boolean result = resultSet.next();
            resultSet.close();

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void prettyPrintMenu() {
        System.out.println("Menu:\n" +
                "1) Add new employee\n" +
                "2) Sort by column\n" +
                "3) Average salary by country\n" +
                "4) Show employees\n");
    }

    private static void addNewEmployee(Connection connection) {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.print("Name: ");
            String name = scanner.nextLine();

            System.out.print("Surame: ");
            String surname = scanner.nextLine();

            System.out.print("Country: ");
            String country = scanner.nextLine();

            System.out.print("Salary: ");
            Integer salary = scanner.nextInt();

            if(insertEmployeeToDatabase(connection, new Employee(null, name, surname, country, salary))) {
                System.out.println("Employee inserted to database successfully.");
            } else {
                System.err.println("Can't insert employee to database!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sortByColumn(Connection connection) {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.print("Column: ");
            String column = scanner.nextLine();

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Pracownicy ORDER BY " + column); // !!! TODO !!! remove sql injection from here somehow

            while (resultSet.next()) {
                System.out.println(Employee.fromResultSet(resultSet));
            }

            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void averageSalaryByCountry(Connection connection) {
        Scanner scanner = new Scanner(System.in);
        try {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT kraj, AVG(placa) AS placa FROM Pracownicy GROUP BY kraj ORDER BY placa;");

            System.out.println("Country | Salary\n" +
                               "--------+-------");

            while (resultSet.next()) {
                System.out.println(resultSet.getString("kraj") + " | " + resultSet.getInt("placa"));
            }

            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showEmployees(Connection connection) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM Pracownicy");
            System.out.println("ID\t | Imie\t | Nazwisko\t | Kraj\t | Placa");
            for(int i=0; i<40; i++) System.out.print("-");
            System.out.println();
            while (resultSet.next()) {
                System.out.println(Employee.fromResultSet(resultSet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            boolean end = false;
            Scanner scanner = new Scanner(System.in);
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/zpo?serverTimezone=UTC", "zpo", "123");
            if(!checkIfTableExists(connection, "Pracownicy")) {
                initializeSchema(connection);
                populateDatabase(connection);
            }

            while(!end) {
                prettyPrintMenu();
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        addNewEmployee(connection);
                        break;
                    case 2:
                        sortByColumn(connection);
                        break;
                    case 3:
                        averageSalaryByCountry(connection);
                        break;
                    case 4:
                        showEmployees(connection);
                        break;
                }
            }
        } catch (InputMismatchException ignored) {}
        catch (SQLException sqle) { System.err.println("Can't connect to database! " + sqle.getMessage()); }
    }
}
