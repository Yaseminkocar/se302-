package com.example.se302;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SchoolManagementApp extends Application {

    // İlk ekran: Ana sayfa
    private BorderPane createMainMenu(Stage primaryStage) {

        BorderPane mainMenuLayout = new BorderPane();

        // Menü çubuğu
        MenuBar menuBar = new MenuBar();

        // "File" menüsü
        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New");
        MenuItem openItem = new MenuItem("Open");
        MenuItem saveItem = new MenuItem("Save");
        MenuItem saveAsItem = new MenuItem("Save As");
        MenuItem exitItem = new MenuItem("Exit");
        fileMenu.getItems().addAll(newItem, openItem, saveItem, saveAsItem, exitItem);

        // "Student Management" menüsü
        Menu studentManagementMenu = new Menu("Student Management");
        MenuItem addStudentItem = new MenuItem("Add New Student");
        MenuItem removeStudentItem = new MenuItem("Remove Student");
        MenuItem assignToClass = new MenuItem("Assign to Class");
        studentManagementMenu.getItems().addAll(addStudentItem, removeStudentItem, assignToClass);

        // "Classroom" menüsü
        Menu classroomMenu = new Menu("Classroom");
        MenuItem viewClassroomSchedule = new MenuItem("View Classroom Schedule");
        classroomMenu.getItems().add(viewClassroomSchedule);


// Classroom menüsüne tıklanınca ders programı görüntülenecek
        viewClassroomSchedule.setOnAction(e -> {
            Scene classroomScheduleScene = createClassroomScheduleScene(primaryStage);
            primaryStage.setScene(classroomScheduleScene);
        });


        Menu searchMenu = new Menu("Search");
        MenuItem searchItem = new MenuItem("Search Lecturer");


        searchItem.setOnAction(e -> {
            Scene searchScene = createSearchScene(primaryStage);
            primaryStage.setScene(searchScene);
        });


        MenuItem searchStudentItem = new MenuItem("Search Student");
        searchStudentItem.setOnAction(e -> {
            Scene searchStudentScene = createSearchStudentScene(primaryStage); // Search Student için sahne oluştur
            primaryStage.setScene(searchStudentScene);
        });


        searchMenu.getItems().addAll(searchItem, searchStudentItem);
        Menu helpMenu = new Menu ("Help");
        MenuItem help = new MenuItem("Help");
        helpMenu.getItems().addAll(help);



        // "Add New Student" butonuna tıklanınca yeni ekrana geçilecek
        addStudentItem.setOnAction(e -> {
            Scene addStudentScene = createAddStudentScene(primaryStage);
            primaryStage.setScene(addStudentScene);
        });
        /*
         */


        // Menüleri ekleyelim
        menuBar.getMenus().addAll(fileMenu, studentManagementMenu,classroomMenu,searchMenu, helpMenu);
        mainMenuLayout.setTop(menuBar);

        // Ana menü sayfasına bir içerik ekleyebiliriz
        Label welcomeLabel = new Label("Welcome to Student Manager!");
        welcomeLabel.setStyle("-fx-font-size: 20px;");
        mainMenuLayout.setCenter(welcomeLabel);

        return mainMenuLayout;
    }

    private Scene createSearchScene(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 10;");

        Label searchLabel = new Label("Enter a lecturer name to search their courses:");
        TextField searchField = new TextField();
        Button searchButton = new Button("Search");
        ListView<String> resultsList = new ListView<>();

        searchButton.setOnAction(e -> {
            String lecturerName = searchField.getText().trim();
            if (!lecturerName.isEmpty()) {
                // Veritabanında hoca adına göre dersleri ara
                List<String> results = DatabaseHelper.searchCoursesByLecturer(lecturerName);
                resultsList.getItems().clear();
                if (!results.isEmpty()) {
                    resultsList.getItems().addAll(results);
                } else {
                    resultsList.getItems().add("No courses found for lecturer: " + lecturerName);
                }
            } else {
                resultsList.getItems().clear();
                resultsList.getItems().add("Please enter a lecturer name to search.");
            }
        });

        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> {
            Scene mainMenuScene = new Scene(createMainMenu(primaryStage), 800, 600);
            primaryStage.setScene(mainMenuScene);
        });

        layout.getChildren().addAll(searchLabel, searchField, searchButton, resultsList, backButton);

        return new Scene(layout, 800, 600);
    }

    private Scene createSearchStudentScene(Stage primaryStage) {

        VBox layout = new VBox(10);  // 10px aralıkla VBox yerleşimi
        layout.setPadding(new Insets(10));

        // Öğrenci adı girebilmek için TextField
        TextField studentNameField = new TextField();
        studentNameField.setPromptText("Enter student name");

        // Arama butonu
        Button searchButton = new Button("Search");
        Label resultLabel = new Label();

        searchButton.setOnAction(e -> {
            String studentName = studentNameField.getText();
            List<String> courses = DatabaseHelper.searchCoursesByStudent(studentName); // Veritabanından dersleri al
            if (courses.isEmpty()) {
                resultLabel.setText("No courses found for student: " + studentName);
            } else {
                resultLabel.setText("Courses for " + studentName + ": " + String.join(", ", courses));
            }
        });

        // Ana menüye dönüş butonu
        Button backButton = new Button("Back");
        // backButton.setOnAction(e -> primaryStage.setScene(createMainScene(primaryStage))); // Ana menüye dön

        layout.getChildren().addAll(studentNameField, searchButton, resultLabel, backButton);

        return new Scene(layout, 400, 300);
    }




    public static List<String> searchCoursesByStudent(String studentName) {
        List<String> results = new ArrayList<>();
        String query = "SELECT courses.course_name " +
                "FROM courses " +
                "INNER JOIN course_students ON courses.id = course_students.course_id " +
                "INNER JOIN students ON students.id = course_students.student_id " +
                "WHERE students.student_name LIKE ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            // Bağlantıyı aç
            connection = DriverManager.getConnection(DB_PATH);

            // Sorguyu hazırlayın
            statement = connection.prepareStatement(query);
            statement.setString(1, "%" + studentName + "%");

            // Sorguyu çalıştır ve sonuçları al
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                results.add(resultSet.getString("course_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Kaynakları manuel olarak kapat
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return results;
    }


    private Scene createAddStudentScene(Stage primaryStage) {
        // Layout
        VBox layout = new VBox(10);

        // Öğrenci ekleme formu
        Label nameLabel = new Label("Student Name:");
        TextField nameField = new TextField();

        Label idLabel = new Label("Student ID:");
        TextField idField = new TextField();

        Button addButton = new Button("Add Student");
        addButton.setOnAction(e -> {
            // Yeni öğrenci ekleme işlemi burada gerçekleştirilebilir
            String name = nameField.getText();
            String id = idField.getText();
            System.out.println("Added Student: " + name + " (ID: " + id + ")");
            nameField.clear();
            idField.clear();
        });

        // Ana menüye dönmek için buton
        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> {
            Scene mainMenuScene = new Scene(createMainMenu(primaryStage), 800, 600);
            primaryStage.setScene(mainMenuScene);
        });

        layout.getChildren().addAll(nameLabel, nameField, idLabel, idField, addButton, backButton);

        // Sahne oluşturma
        Scene scene = new Scene(layout, 800, 600);
        return scene;
    }


  /*  private Scene createClassroomScheduleScene(Stage primaryStage) {
        // Classroom seçim ComboBox
        Label classroomLabel = new Label("Choose a Classroom:");
        ComboBox<String> classroomComboBox = new ComboBox<>();
        classroomComboBox.getItems().addAll("M203", "M404", "C207", "C209", "ML103", "MB158");

        // Hafta içi günleri
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        String[] timeSlots = {"9:00 AM", "11:00 AM", "1:00 PM", "3:00 PM"};

        // Ders programı için TableView
        TableView<CourseSchedule> tableView = new TableView<>();

        // Time sütunu
        TableColumn<CourseSchedule, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        // Hafta içi sütunları
        TableColumn<CourseSchedule, String> mondayColumn = new TableColumn<>("Monday");
        mondayColumn.setCellValueFactory(new PropertyValueFactory<>("monday"));

        TableColumn<CourseSchedule, String> tuesdayColumn = new TableColumn<>("Tuesday");
        tuesdayColumn.setCellValueFactory(new PropertyValueFactory<>("tuesday"));

        TableColumn<CourseSchedule, String> wednesdayColumn = new TableColumn<>("Wednesday");
        wednesdayColumn.setCellValueFactory(new PropertyValueFactory<>("wednesday"));

        TableColumn<CourseSchedule, String> thursdayColumn = new TableColumn<>("Thursday");
        thursdayColumn.setCellValueFactory(new PropertyValueFactory<>("thursday"));

        TableColumn<CourseSchedule, String> fridayColumn = new TableColumn<>("Friday");
        fridayColumn.setCellValueFactory(new PropertyValueFactory<>("friday"));

        tableView.getColumns().addAll(timeColumn, mondayColumn, tuesdayColumn, wednesdayColumn, thursdayColumn, fridayColumn);

        // Derslerin doldurulması için örnek veri
        ObservableList<CourseSchedule> data = FXCollections.observableArrayList(
                new CourseSchedule("9:00 AM", "Math 101", "SE 202", "", "", "CE 323"),
                new CourseSchedule("11:00 AM", "Physics 101", "MATH250", "SE 202", "", "EEE 242")
        );

        tableView.setItems(data);

        // Classroom seçildiğinde programın görünümünü güncelleme
        classroomComboBox.setOnAction(e -> {
            // Classroom seçimi yapıldığında ders programını güncellemek için kod eklenebilir
            // Bu örnekte basit bir veri kümesi kullanılıyor
        });

        // Layout
        VBox layout = new VBox(10);
        layout.getChildren().addAll(classroomLabel, classroomComboBox, tableView);

        // Geri dönmek için "Back to Main Menu" butonu
        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> {
            Scene mainMenuScene = new Scene(createMainMenu(primaryStage), 800, 600);
            primaryStage.setScene(mainMenuScene);
        });
        layout.getChildren().add(backButton);

        // Yeni sahneyi oluştur
        Scene scene = new Scene(layout, 800, 600);
        return scene;
    }

    private ObservableList<CourseSchedule> fetchCourseData() {
        ObservableList<CourseSchedule> data = FXCollections.observableArrayList();

        String query = "SELECT time_to_start AS time, course_name, lecturer FROM courses";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String time = resultSet.getString("time");
                String courseName = resultSet.getString("course_name");
                String lecturer = resultSet.getString("lecturer");

                data.add(new CourseSchedule(time, courseName, lecturer, "", "", ""));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    } */


    private Scene createClassroomScheduleScene(Stage primaryStage) {
        // Classroom seçim ComboBox
        Label classroomLabel = new Label("Choose a Classroom:");
        ComboBox<String> classroomComboBox = new ComboBox<>();

        // Classroom bilgilerini veritabanından doldur
        classroomComboBox.getItems().addAll(DatabaseHelper.getClassroomsFromDatabase());

        // Ders programı için TableView
        TableView<CourseSchedule> tableView = new TableView<>();

        // Time sütunu
        TableColumn<CourseSchedule, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        // Hafta içi sütunları
        TableColumn<CourseSchedule, String> mondayColumn = new TableColumn<>("Monday");
        mondayColumn.setCellValueFactory(new PropertyValueFactory<>("monday"));

        TableColumn<CourseSchedule, String> tuesdayColumn = new TableColumn<>("Tuesday");
        tuesdayColumn.setCellValueFactory(new PropertyValueFactory<>("tuesday"));

        TableColumn<CourseSchedule, String> wednesdayColumn = new TableColumn<>("Wednesday");
        wednesdayColumn.setCellValueFactory(new PropertyValueFactory<>("wednesday"));

        TableColumn<CourseSchedule, String> thursdayColumn = new TableColumn<>("Thursday");
        thursdayColumn.setCellValueFactory(new PropertyValueFactory<>("thursday"));

        TableColumn<CourseSchedule, String> fridayColumn = new TableColumn<>("Friday");
        fridayColumn.setCellValueFactory(new PropertyValueFactory<>("friday"));

        tableView.getColumns().addAll(timeColumn, mondayColumn, tuesdayColumn, wednesdayColumn, thursdayColumn, fridayColumn);

        // Classroom seçildiğinde programın görünümünü güncelleme
        classroomComboBox.setOnAction(e -> {
            String selectedClassroom = classroomComboBox.getValue();
            if (selectedClassroom != null) {
                // Veritabanından gelen verileri bağla
                tableView.setItems(DatabaseHelper.fetchCourseData(selectedClassroom));
            }
        });

        // Layout
        VBox layout = new VBox(10);
        layout.getChildren().addAll(classroomLabel, classroomComboBox, tableView);

        // Geri dönmek için "Back to Main Menu" butonu
        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> {
            Scene mainMenuScene = new Scene(createMainMenu(primaryStage), 800, 600);
            primaryStage.setScene(mainMenuScene);
        });
        layout.getChildren().add(backButton);

        // Yeni sahneyi oluştur
        Scene scene = new Scene(layout, 800, 600);
        return scene;
    }




    @Override
    public void start(Stage primaryStage) {


        try {
            // FXML dosyasını yükle
            FXMLLoader loader = new FXMLLoader(getClass().getResource("school-view.fxml"));
            BorderPane root = loader.load();


            // Sahneyi oluştur
            Scene scene = new Scene(createMainMenu(primaryStage), 800, 600);
            // scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            primaryStage.setTitle("Student Manager");
            primaryStage.setScene(scene);
            primaryStage.show();

            // FXML Controller'a erişim
            SchoolManagementApp controller = loader.getController();
            controller.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static final String DB_PATH = "jdbc:sqlite:" + System.getProperty("user.dir") + "\\TimetableManagement.db";


    public static void main(String[] args) throws SQLException {


        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:C:\\database\\TimetableManagement.db");
             Statement statement = connection.createStatement()) {

            System.out.println("Courses:");
            ResultSet courses = statement.executeQuery(
                    "SELECT DISTINCT course_name, time_to_start, duration, lecturer FROM courses;"
            );

            while (courses.next()) {
                System.out.println(
                        "Course Name: " + courses.getString("course_name") +
                                ", Time: " + courses.getString("time_to_start") +
                                ", Duration: " + courses.getInt("duration") +
                                ", Lecturer: " + courses.getString("lecturer")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:C:\\database\\TimetableManagement.db")) {

            Statement statement = connection.createStatement();

            // Ders Kodlarını Getir
            System.out.println("Course Names:");
            ResultSet courseCodes = statement.executeQuery("""
        SELECT DISTINCT student_name AS code_or_name
        FROM students
        WHERE student_name GLOB '[A-Z]*[0-9]*'
        ORDER BY student_name;
    """);

            while (courseCodes.next()) {
                System.out.println("Course Code: " + courseCodes.getString("code_or_name"));
            }

            // Öğrenci İsimlerini Getir
            System.out.println("\nStudent Names:");
            ResultSet studentNames = statement.executeQuery("""
        SELECT DISTINCT student_name AS code_or_name
        FROM students
        WHERE student_name NOT GLOB '[A-Z]*[0-9]*'
        
    """);

            while (studentNames.next()) {
                System.out.println("Student Name: " + studentNames.getString("code_or_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:C:\\database\\TimetableManagement.db");
             Statement statement = connection.createStatement()) {

            System.out.println("\nCourse-Student Relationships:");

            ResultSet courseStudents = statement.executeQuery("""
        SELECT 
            students.student_name,
            GROUP_CONCAT(DISTINCT courses.course_name) AS courses
        FROM 
            course_students
        INNER JOIN 
            students ON course_students.student_id = students.id
        INNER JOIN 
            courses ON course_students.course_id = courses.id
        GROUP BY 
            students.student_name
        ORDER BY 
            students.student_name;
    """);

            while (courseStudents.next()) {
                String studentName = courseStudents.getString("student_name");
                String courses = courseStudents.getString("courses");

                System.out.println("Student Name: " + studentName + ", Courses: " + courses);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        //  DatabaseSetup.setupDatabase();
        DatabaseHelper.removeDuplicates();

        // CSV dosyasını veritabanına aktar
        CSVToDatabase.importCSV("C:\\database\\Courses (2).csv");


        System.out.println("DB_PATH: " + DB_PATH);

        // JavaFX uygulamasını başlat
        launch(args);

    }

}