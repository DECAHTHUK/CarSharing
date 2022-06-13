package Projects;

import org.h2.tools.DeleteDbFiles;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class CarSharing {
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:./src/carsharing/db/";
    static String DB_NAME = "dbName";
    // DB
    static DBImp database;
    //  Database credentials
    static final String USER = "sa";
    static final String PASS = "";

    public static void mainMenu() throws SQLException {
        System.out.println("1. Log in as a manager\n" +
                "2. Log in as a customer\n" +
                "3. Create a customer\n" +
                "0. Exit");
        int a = 0;
        try {
            a = new Scanner(System.in).nextInt();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        switch (a) {
            case 1:
                managerMenu();
                break;
            case 2:
                customerChooseMenu(database.getCustomerDao().getAllCustomers());
                break;
            case 3:
                System.out.println("Enter the customer name:");
                database.getCustomerDao().addCustomer(new Scanner(System.in).nextLine());
                System.out.println("The customer was added!");
                mainMenu();
                break;
            case 0:
                break;
            default:
                System.out.println("Wrong command!");
                mainMenu();
                break;
        }
    }

    public static void managerMenu() throws SQLException {
        System.out.println("\n1. Company list\n" +
                "2. Create a company\n" +
                "0. Back");
        int a = 0;
        try {
            a = new Scanner(System.in).nextInt();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        switch (a) {
            case 1:
                companyChooseMenu(database.getCompanyDao().getAllCompanies());
                break;
            case 2:
                System.out.println("Enter the company name:");
                String name = new Scanner(System.in).nextLine();
                database.getCompanyDao().addCompany(name);
                System.out.println("The company was created!");
                managerMenu();
                break;
            case 0:
                mainMenu();
                break;
        }
    }

    public static void companyChooseMenu(List<Company> out) throws SQLException {
        if (out.isEmpty()) {
            System.out.println("The company list is empty!");
            managerMenu();
        } else {
            System.out.println("Choose a company:");
            int cnt = 1;
            for (Company comp : out) {
                System.out.println(cnt++ + ". " + comp.getName());
            }
            System.out.println("0. Back");
        }
        int a = 0;
        try {
            a = new Scanner(System.in).nextInt();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if (a > out.size()) {
            System.out.println("Illegal comp number");
            managerMenu();
        } else if (a == 0) {
            managerMenu();
        } else {
            companyMenu(out.get(a - 1));
        }
    }

    public static void customerChooseMenu(List<Customer> out) throws SQLException {
        if (out.isEmpty()) {
            System.out.println("The customer list is empty!");
            mainMenu();
        } else {
            System.out.println("Choose a customer:");
            int cnt = 1;
            for (Customer customer : out) {
                System.out.println(cnt++ + ". " + customer.getName());
            }
            System.out.println("0. Back");
        }
        int a = 0;
        try {
            a = new Scanner(System.in).nextInt();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if (a > out.size()) {
            System.out.println("Illegal customer number");
            mainMenu();
        } else if (a == 0) {
            mainMenu();
        } else {
            customerMenu(out.get(a - 1));
        }
    }

    public static void customerMenu(Customer in) throws SQLException {
        System.out.println("1. Rent a car\n" +
                "2. Return a rented car\n" +
                "3. My rented car\n" +
                "0. Back");
        int a = 0;
        try {
            a = new Scanner(System.in).nextInt();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        switch (a) {
            case 1:
                rentACar(in, database.getCompanyDao().getAllCompanies());
                break;
            case 2:
                if (in.getRentedCarId() == 0) {
                    System.out.println("You didn't rent a car!");
                } else {
                    database.getCustomerDao().alterCustomer(in.getId(), 0);
                    in.setRentedCarId(0);
                    System.out.println("You've returned a rented car!");
                }
                customerMenu(in);
                break;
            case 3:
                if (in.getRentedCarId() == 0) {
                    System.out.println("You didn't rent a car!");
                } else {
                    List<Car> cars = database.getCarDao().getAllCars(-1);
                    System.out.println("Your rented car:");
                    cars = cars.stream().filter(t -> t.getId() == in.getRentedCarId()).collect(Collectors.toList());
                    cars.forEach(t -> System.out.println(t.getName()));
                    Car our = cars.get(0);
                    System.out.println("Company:");
                    List<Company> companies = database.getCompanyDao().getAllCompanies();
                    companies.stream().filter(t -> t.getId() == our.getCompany_id()).forEach(t -> System.out.println(t.getName()));
                }
                customerMenu(in);
                break;
            case 0:
                mainMenu();
                break;
            default:
                System.out.println("Wrong command!");
                mainMenu();
                break;
        }
    }

    public static void rentACar(Customer in, List<Company> out) throws SQLException {
        if (out.isEmpty()) {
            System.out.println("The company list is empty!");
            customerMenu(in);
        } else if (in.getRentedCarId() != 0) {
            System.out.println("You've already rented a car!");
            customerMenu(in);
        } else {
            System.out.println("Choose a company:");
            int cnt = 1;
            for (Company comp : out) {
                System.out.println(cnt++ + ". " + comp.getName());
            }
            System.out.println("0. Back");
        }
        int a = 0;
        try {
            a = new Scanner(System.in).nextInt();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if (a > out.size()) {
            System.out.println("Illegal comp number");
            customerMenu(in);
        } else if (a == 0) {
            customerMenu(in);
        } else {
            if (database.getCarDao().getAllCars(out.get(a - 1).getId()).size() == 0) {
                System.out.println("No available cars in the " + out.get(a).getName() + " company");
                rentACar(in, out);
            }
            carChoose(in, database.getCarDao().getAllCars(out.get(a - 1).getId()));
        }
    }

    /*public static boolean checkIfCompCarsAvailable(List<Car> cars, List<Customer> customers) {
        return cars.stream().map(Car::getId)
                .filter(t -> !customers.stream().map(Customer::getRentedCarId)
                        .collect(Collectors.toList()).contains(t)).count() != 0;
    }*/

    public static void carChoose(Customer in, List<Car> cars) throws SQLException {
        System.out.println("Choose a car:");
        cars = cars.stream().filter(t -> {
            try {
                return checkIfCarAvailable(t, database.getCustomerDao().getAllCustomers());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }).collect(Collectors.toList());
        int cnt = 1;
        for (Car name : cars) {
            System.out.printf("%d. %s\n", cnt++, name.getName());
        }
        System.out.println("0.Back");
        int a = 0;
        try {
            a = new Scanner(System.in).nextInt();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        if (a > cars.size()) {
            System.out.println("Illegal car number");
            carChoose(in, cars);
        } else if (a == 0) {
            rentACar(in, database.getCompanyDao().getAllCompanies());
        } else {
            database.getCustomerDao().alterCustomer(in.getId(), cars.get(a - 1).getId());
            in.setRentedCarId(cars.get(a - 1).getId());
            System.out.println("You rented '" + cars.get(a - 1).getName() + "'");
            customerMenu(in);
        }
    }

    public static boolean checkIfCarAvailable(Car car, List<Customer> customers) {
        for(Customer customer : customers) {
            if (customer.getRentedCarId() == car.getId()) {
                return false;
            }
        }
        return true;
    }

    public static void printCars(List<Car> a) {
        System.out.println("Car list:");
        if (a.size() == 0) {
            System.out.println("The car list is empty!");
        }
        else {
            int cnt = 1;
            for (Car name : a) {
                System.out.printf("%d. %s\n", cnt++, name.getName());
            }
        }
    }

    public static void companyMenu(Company in) throws SQLException {
        System.out.println("'" + in.getName() + "' company:\n" +
                "1. Car list\n" +
                "2. Create a car\n" +
                "0. Back");
        int a = 0;
        try {
            a = new Scanner(System.in).nextInt();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        switch (a) {
            case 1:
                printCars(database.getCarDao().getAllCars(in.getId()));
                companyMenu(in);
                break;
            case 2:
                System.out.println("Enter the car name:");
                String name = new Scanner(System.in).nextLine();
                database.getCarDao().addCar(name, in.getId());
                System.out.println("The car was added!");
                companyMenu(in);
                break;
            case 0:
                managerMenu();
                break;
            default:
                System.out.println("Wrong command!");
                mainMenu();
                break;
        }
    }

    public static void main(String[] args) throws SQLException {
        DeleteDbFiles.execute("dir", "db", true);
        //File[] dir = new File("./src/carsharing/db").listFiles();
        //if (dir != null) Stream.of(dir).forEach(File::delete);
        if (args.length > 0 && args[0].equals("-databaseFileName") && args.length > 1) {
            DB_NAME = args[1];
        }
        Connection conn = null;
        Statement stmt = null;
        try{
            // STEP 1: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            // STEP 2: Open a connection
            conn = DriverManager.getConnection(DB_URL + DB_NAME);
            conn.setAutoCommit(true);

            // STEP 3: Execute a query
            stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS COMPANY " +
                    "(ID INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "NAME VARCHAR(255) UNIQUE NOT NULL); " +

                    "CREATE TABLE IF NOT EXISTS CAR " +
                    "(ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "NAME VARCHAR(40) UNIQUE NOT NULL, " +
                    "COMPANY_ID INT NOT NULL, " +
                    "CONSTRAINT idshnik FOREIGN KEY (COMPANY_ID) " +
                    "REFERENCES COMPANY(ID)); " +

                    "CREATE TABLE IF NOT EXISTS CUSTOMER " +
                    "(ID INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "NAME VARCHAR(255) UNIQUE NOT NULL, " +
                    "RENTED_CAR_ID INTEGER DEFAULT NULL, " +
                    "FOREIGN KEY (RENTED_CAR_ID) REFERENCES CAR(ID));";
            stmt.executeUpdate(sql);
            stmt.close();
            /*stmt = conn.createStatement();
            sql =  "CREATE TABLE IF NOT EXISTS CAR" +
                    "(ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "NAME VARCHAR(40) NOT NULL UNIQUE, " +
                    "COMPANY_ID INT NOT NULL, " +
                    "CONSTRAINT idshnik FOREIGN KEY (COMPANY_ID) " +
                    "REFERENCES COMPANY(ID));";
            stmt.executeUpdate(sql);
            stmt.close();*/
        } catch(SQLException se) {
            // Handle errors for JDBC
            se.printStackTrace();
        } catch(Exception e) {
            // Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            // finally block used to close resources
            try {
                if(stmt!=null) stmt.close();
            } catch(SQLException se2) {
            } // nothing we can do
        } // end try
        database = new DBImp(new CompanyDaoImp(conn), new CarDaoImp(conn), new CustomerDaoImp(conn));
        mainMenu();
        conn.close();
    }
    public static class Company {
        private int id;
        private String name;

        Company(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public interface CompanyDao {
        public List<Company> getAllCompanies() throws SQLException;
        public void addCompany(String name) throws SQLException;
    }

    public static class CompanyDaoImp implements CompanyDao {
        private Connection conn = null;

        public CompanyDaoImp(Connection conn) {
            this.conn = conn;
        }

        @Override
        public List<Company> getAllCompanies() throws SQLException {
            Statement stmt = conn.createStatement();
            String sql = "SELECT *" +
                    "FROM COMPANY " +
                    "ORDER BY ID;";
            ResultSet rs = stmt.executeQuery(sql);
            List<Company> out = new LinkedList<>();
            while(rs.next()) {
                out.add(new Company((rs.getInt("ID")), rs.getString("NAME")));
            }
            stmt.close();
            return out;
        }
        @Override
        public void addCompany(String name) throws SQLException {
            Statement stmt = conn.createStatement();
            String sql = "INSERT INTO COMPANY (NAME)\n" +
                    "VALUES ('" + name + "')";
            stmt.executeUpdate(sql);
            stmt.close();
        }
    }

    public static class Car {
        private int id;
        private String name;
        private int company_id;

        Car (int id, String name, int comp_id) {
            this.id = id;
            this.name = name;
            this.company_id = comp_id;
        }

        public int getId() { return id; }

        public void setId(int id) { this.id = id; }

        public String getName() { return name; }

        public void setName(String name) { this.name = name; }

        public int getCompany_id() { return company_id; }

        public void setCompany_id(int company_id) { this.company_id = company_id; }
    }

    public interface CarDao {
        public void addCar(String name, int compId) throws SQLException;
        public List<Car> getAllCars(int compId) throws SQLException;
    }

    public static class CarDaoImp implements CarDao {
        private Connection conn = null;

        public CarDaoImp(Connection conn) {
            this.conn = conn;
        }

        @Override
        public void addCar(String name, int compId) throws SQLException {
            Statement stmt = conn.createStatement();
            String sql = "INSERT INTO CAR(NAME, COMPANY_ID)\n" +
                    "VALUES ('" + name + "', " + compId +")";
            stmt.executeUpdate(sql);
            stmt.close();
        }

        @Override
        public List<Car> getAllCars(int compId) throws SQLException {
            Statement stmt = conn.createStatement();
            String sql;
            if (compId != -1) {
                sql = "SELECT * " +
                        "FROM CAR " +
                        "WHERE COMPANY_ID = " + compId +
                        " ORDER BY ID;";
            }
            else {
                sql = "SELECT * " +
                        "FROM CAR; ";
            }
            ResultSet rs = stmt.executeQuery(sql);
            List<Car> out = new LinkedList<>();
            while(rs.next()) {
                out.add(new Car((rs.getInt("ID")), rs.getString("NAME"),
                        rs.getInt("COMPANY_ID")));
            }
            stmt.close();
            return out;
        }
    }

    public static class Customer {
        private int id;
        private String name;
        private int rentedCarId = 0;

        public Customer(int id, String name, int rentedCarId) {
            this.id = id;
            this.name = name;
            this.rentedCarId = rentedCarId;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getRentedCarId() {
            return rentedCarId;
        }

        public void setRentedCarId(int rentedCarId) {
            this.rentedCarId = rentedCarId;
        }
    }

    public interface customerDao {
        public void addCustomer(String name) throws SQLException;
        public List<Customer> getAllCustomers() throws SQLException;
        public void alterCustomer(int id, int carId) throws SQLException;
    }

    public static class CustomerDaoImp implements customerDao{
        private Connection conn = null;
        public CustomerDaoImp(Connection conn) {this.conn = conn;}

        @Override
        public void addCustomer(String name) throws SQLException {
            Statement stmt = conn.createStatement();
            String sql = "INSERT INTO CUSTOMER(NAME)\n" +
                    "VALUES ('" + name + "')";
            stmt.executeUpdate(sql);
            stmt.close();
        }

        @Override
        public List<Customer> getAllCustomers() throws SQLException {
            Statement stmt = conn.createStatement();
            String sql = "SELECT * " +
                    "FROM CUSTOMER; ";
            ResultSet rs = stmt.executeQuery(sql);
            List<Customer> out = new LinkedList<>();
            while(rs.next()) {
                out.add(new Customer(rs.getInt("ID"), rs.getString("NAME"), rs.getInt("RENTED_CAR_ID")));
            }
            stmt.close();
            return out;
        }

        @Override
        public void alterCustomer(int id, int carId) throws SQLException {
            Statement stmt = conn.createStatement();
            String sql;
            if (carId != 0) {
                sql = "UPDATE CUSTOMER " +
                        "SET RENTED_CAR_ID = " + carId +
                        " WHERE ID = " + id + " ;";
            } else {
                sql = "UPDATE CUSTOMER " +
                        "SET RENTED_CAR_ID = NULL " +
                        "WHERE ID = " + id + " ;";
            }
            stmt.executeUpdate(sql);
            stmt.close();
        }
    }

    public static class DBImp {
        private CompanyDaoImp companyDao;
        private CarDaoImp carDao;
        private CustomerDaoImp customerDao;

        public DBImp(CompanyDaoImp companyDao, CarDaoImp carDao, CustomerDaoImp customerDao) {
            this.companyDao = companyDao;
            this.carDao = carDao;
            this.customerDao = customerDao;
        }

        public CompanyDaoImp getCompanyDao() {
            return companyDao;
        }

        public void setCompanyDao(CompanyDaoImp companyDao) {
            this.companyDao = companyDao;
        }

        public CarDaoImp getCarDao() {
            return carDao;
        }

        public void setCarDao(CarDaoImp carDao) {
            this.carDao = carDao;
        }

        public CustomerDaoImp getCustomerDao() {
            return customerDao;
        }

        public void setCustomerDao(CustomerDaoImp customerDao) {
            this.customerDao = customerDao;
        }
    }
}