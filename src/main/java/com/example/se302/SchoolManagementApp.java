package com.example.se302;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;


import static com.example.se302.DatabaseHelper.reassignClassroomIfNeeded;

public class SchoolManagementApp extends Application {

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static final String[] TIME_SLOTS = {
            "08:30 - 09:15",
            "09:25 - 10:10",
            "10:20 - 11:05",
            "11:15 - 12:00",
            "12:10 - 12:55",
            "13:05 - 13:50",
            "14:00 - 14:45",
            "14:55 - 15:40",
            "15:50 - 16:35"
    };

    private BorderPane createMainMenu(Stage primaryStage) {

        BorderPane mainMenuLayout = new BorderPane();


        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New");
        MenuItem openItem = new MenuItem("Open");
        MenuItem saveItem = new MenuItem("Save");
        MenuItem saveAsItem = new MenuItem("Save As");
        MenuItem exitItem = new MenuItem("Exit");
        fileMenu.getItems().addAll(newItem, openItem, saveItem, saveAsItem, exitItem);

        Menu studentManagementMenu = new Menu("Student Management");

        MenuItem removeStudentItem = new MenuItem("Remove Student");
        removeStudentItem.setOnAction(e -> {
            Scene removeStudentScene = createRemoveStudentScene(primaryStage);
            primaryStage.setScene(removeStudentScene);
        });



        MenuItem viewWeeklyScheduleItem = new MenuItem("View Student's Weekly Schedule");
        viewWeeklyScheduleItem.setOnAction(e -> {
            Scene weeklyScheduleScene = createWeeklyScheduleScene(primaryStage);
            primaryStage.setScene(weeklyScheduleScene);
        });
        MenuItem addStudentToCourseItem = new MenuItem("Add Student to Course");
        addStudentToCourseItem.setOnAction(e -> {
            Scene addStudentScene = createAddStudentToCourseScene(primaryStage);
            primaryStage.setScene(addStudentScene);
        });

        // studentManagementMenu.getItems().add(addStudentToCourseItem);

        studentManagementMenu.getItems().addAll(addStudentToCourseItem,removeStudentItem,viewWeeklyScheduleItem);


        Menu classroomMenu = new Menu("Classroom");
        MenuItem viewClassroomCapacities = new MenuItem("View Classroom Capacities");
        viewClassroomCapacities.setOnAction(e -> {
            Scene classroomScene = createClassroomCapacityScene(primaryStage);
            primaryStage.setScene(classroomScene);
        });
        //    classroomMenu.getItems().add(viewClassroomCapacities);

        MenuItem assignToClass = new MenuItem("Classroom Assignments List");

        assignToClass.setOnAction(e -> {
            Scene assign = createAssignToClass(primaryStage);
            primaryStage.setScene(assign);
        });




        MenuItem classroomSchedule = new MenuItem("Classroom Schedule");

        classroomSchedule.setOnAction(e -> {
            Scene CSchedule = createClassroomScheduleScene(primaryStage);
            primaryStage.setScene(CSchedule);
        });

        classroomMenu.getItems().addAll(viewClassroomCapacities,assignToClass, classroomSchedule);

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

        MenuItem searchCourseItem = new MenuItem("Search Course Details");
        searchCourseItem.setOnAction(e -> {
            Scene searchCourseScene = createSearchCourseScene(primaryStage); // Search Student için sahne oluştur
            primaryStage.setScene(searchCourseScene);
        });


        searchMenu.getItems().addAll(searchItem, searchStudentItem, searchCourseItem);

        Menu helpMenu = new Menu("Help");
        //MenuItem help = new MenuItem("Help");
        MenuItem howToUseItem = new MenuItem("How to Use");

        howToUseItem.setOnAction(e -> {
            String howToUseMessage = "Welcome to Student Manager!\n\n"
                    + "1. Use 'Student Management' to add, remove, or view student schedules.\n"
                    + "2. Use 'Classroom' to view classroom capacities and assignments.\n"
                    + "3. Use 'Search' to search for lecturers, students, or courses.\n"
                    + "4. Use 'Student Count' to find the number of students in a course.\n";



            showAlert("How to Use Student Manager", howToUseMessage);
        });

        helpMenu.getItems().addAll(howToUseItem);


        Menu studentCountMenu = new Menu("Student Count");
        MenuItem findStudentCountItem = new MenuItem("Find Student Count");
        findStudentCountItem.setOnAction(e -> {
            Scene studentCountScene = createStudentCountScene(primaryStage);
            primaryStage.setScene(studentCountScene);
        });
        studentCountMenu.getItems().add(findStudentCountItem);
        menuBar.getMenus().add(studentCountMenu);


        MenuItem findClassroomsItem = new MenuItem("Find Available Classrooms");
        findClassroomsItem.setOnAction(e -> {
            Scene availableClassroomsScene = createAvailableClassroomsScene(primaryStage);
            primaryStage.setScene(availableClassroomsScene);
        });
        searchMenu.getItems().add(findClassroomsItem);


        // Menüleri ekleyelim
        menuBar.getMenus().addAll(fileMenu, studentManagementMenu,classroomMenu,searchMenu, helpMenu);
        mainMenuLayout.setTop(menuBar);

        // Ana menü sayfasına bir içerik ekleyebiliriz
        Label welcomeLabel = new Label("Welcome to Student Manager!");
        welcomeLabel.setStyle("-fx-font-size: 20px;");
        mainMenuLayout.setCenter(welcomeLabel);

        createImportButton(mainMenuLayout);

        return mainMenuLayout;
    }

    private static void showUpperCaseAlert() {
        String message = "Attention!\n\n"
                + "When entering a student's name, please use ALL UPPERCASE LETTERS.\n"
                + "Example: ALİVELİ\n"
                + "Also when searching lecturers please use first letters as UPPERCASE.\n"
                + "Example: İlker Korkmaz\n\n"
                + "This is required for consistency in the system.";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Uppercase Input Required");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait(); // Kullanıcı uyarıyı kapatana kadar bekler
    }





    private Scene createRemoveStudentScene(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label studentLabel = new Label("Student Name:");
        TextField studentField = new TextField();

        Label courseLabel = new Label("Course Name:");
        TextField courseField = new TextField();

        Button removeButton = new Button("Remove Student from Course");
        Label resultLabel = new Label();

        removeButton.setOnAction(e -> {
            String studentName = studentField.getText().trim();
            String courseName = courseField.getText().trim();

            if (!studentName.isEmpty() && !courseName.isEmpty()) {
                boolean success = DatabaseHelper.removeStudentFromCourse(studentName, courseName);
                if (success) {
                    resultLabel.setText("Student removed from the course successfully!");
                } else {
                    resultLabel.setText("Error: Student not found in the specified course.");
                }
            } else {
                resultLabel.setText("Please enter both student name and course name.");
            }
        });

        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> primaryStage.setScene(new Scene(createMainMenu(primaryStage), 800, 600)));

        layout.getChildren().addAll(studentLabel, studentField, courseLabel, courseField, removeButton, resultLabel, backButton);

        return new Scene(layout, 800, 600);
    }

    private Scene createAssignToClass(Stage primaryStage) {
        // Layout ve TableView oluşturma
        VBox layout = new VBox(10);
        TableView<String> tableView = new TableView<>();

        TableColumn<String, String> column = new TableColumn<>("Assigned Classes");
        column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()));

        tableView.getColumns().add(column);
        tableView.getItems().addAll(assignAllCoursesToClassrooms());

        // Geri butonu
        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> primaryStage.setScene(new Scene(createMainMenu(primaryStage), 800, 600)));

        layout.getChildren().addAll(new Label("Assigned Classes"), tableView, backButton);
        return new Scene(layout, 800, 600);
    }


    private Scene createClassroomScheduleScene(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        // Classroom input
        Label classroomLabel = new Label("Enter a classroom name:");
        TextField classroomField = new TextField();
        Button findButton = new Button("Show Weekly Schedule");

        // TableView setup
        TableView<Map<String, String>> tableView = new TableView<>();
        TableColumn<Map<String, String>, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("time")));

        TableColumn<Map<String, String>, String> mondayColumn = createDayColumn("Monday");
        TableColumn<Map<String, String>, String> tuesdayColumn = createDayColumn("Tuesday");
        TableColumn<Map<String, String>, String> wednesdayColumn = createDayColumn("Wednesday");
        TableColumn<Map<String, String>, String> thursdayColumn = createDayColumn("Thursday");
        TableColumn<Map<String, String>, String> fridayColumn = createDayColumn("Friday");

        tableView.getColumns().addAll(timeColumn, mondayColumn, tuesdayColumn, wednesdayColumn, thursdayColumn, fridayColumn);



        findButton.setOnAction(e -> {
            String classroomName = classroomField.getText().trim();
            if (!classroomName.isEmpty()) {
                Map<String, Map<String, String>> schedule = assignWeeklyScheduleToClassrooms();

                /*ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
                for (String time : TIME_SLOTS) {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("time", time);
                    for (String day : schedule.keySet()) {
                        row.put(day, schedule.get(day).get(time));
                    }
                    data.add(row);
                }
                tableView.setItems(data);
                 */

                ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
                Map<String, Map<String, String>> weeklySchedule = assignWeeklyScheduleToClassrooms();

                for (String timeSlot : TIME_SLOTS) {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("time", timeSlot);

                    for (String courseName : weeklySchedule.getOrDefault(timeSlot, new HashMap<>()).keySet()) {
                        String classroomInfo = weeklySchedule.get(timeSlot).get(courseName);
                        row.put(courseName, classroomInfo != null ? classroomInfo : "");
                    }
                    data.add(row);
                }
                tableView.setItems(data);


            }
        });

        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> primaryStage.setScene(new Scene(createMainMenu(primaryStage), 800, 600)));

        layout.getChildren().addAll(classroomLabel, classroomField, findButton, tableView, backButton);
        return new Scene(layout, 800, 600);

    }

    private void createImportButton(BorderPane mainMenuLayout) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Button importButton = new Button("Import Data");
        Label resultLabel = new Label();

        importButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select CSV File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                String fileName = selectedFile.getName();
                if (fileName.equalsIgnoreCase("courses.csv")) {
                    try {
                        clearTable("courses");
                        CSVToDatabase.importCSV(selectedFile.getAbsolutePath());
                        resultLabel.setText("Courses data successfully imported.");
                    } catch (Exception ex) {
                        resultLabel.setText("Error importing courses: " + ex.getMessage());
                    }
                } else if (fileName.equalsIgnoreCase("classroomcapacity.csv")) {
                    try {
                        clearTable("classroom_capacity");
                        SecondDatabase.importClassroomCapacity(selectedFile.getAbsolutePath());
                        resultLabel.setText("Classroom capacity data successfully imported.");
                    } catch (Exception ex) {
                        resultLabel.setText("Error importing classroom capacity: " + ex.getMessage());
                    }
                } else {
                    resultLabel.setText("Only 'courses.csv' and 'classroomcapacity.csv' files are allowed.");
                }
            } else {
                resultLabel.setText("No file selected.");
            }
        });

        layout.getChildren().addAll(importButton, resultLabel);
        mainMenuLayout.setBottom(layout);
    }

    private void clearTable(String tableName) {
        // İlgili veritabanını seç
        String dbPath = tableName.equalsIgnoreCase("classroom_capacity")
                ? "jdbc:sqlite:database/ClassroomCapacity.db"
                : "jdbc:sqlite:database/TimetableManagement.db";

        // Tablodaki tüm verileri silmek için SQL sorgusu
        String deleteQuery = "DELETE FROM " + tableName;

        try (Connection connection = DriverManager.getConnection(dbPath);
             Statement statement = connection.createStatement()) {
            // SQL sorgusunu çalıştır
            int rowsAffected = statement.executeUpdate(deleteQuery);
            System.out.println("Table '" + tableName + "' cleared. Rows affected: " + rowsAffected);
        } catch (SQLException e) {
            // Hata durumunda mesaj yazdır
            System.err.println("Error clearing table '" + tableName + "': " + e.getMessage());
            e.printStackTrace();
        }
    }



    private Scene createWeeklyScheduleScene(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        // Öğrenci adı girişi
        Label studentLabel = new Label("Enter a student name:");
        TextField studentField = new TextField();
        Button findButton = new Button("Show Weekly Schedule");

        // TableView oluştur
        TableView<Map<String, String>> tableView = new TableView<>();
        TableColumn<Map<String, String>, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().get("time"))
        );

        TableColumn<Map<String, String>, String> mondayColumn = createDayColumn("Monday");
        TableColumn<Map<String, String>, String> tuesdayColumn = createDayColumn("Tuesday");
        TableColumn<Map<String, String>, String> wednesdayColumn = createDayColumn("Wednesday");
        TableColumn<Map<String, String>, String> thursdayColumn = createDayColumn("Thursday");
        TableColumn<Map<String, String>, String> fridayColumn = createDayColumn("Friday");

        tableView.getColumns().addAll(timeColumn, mondayColumn, tuesdayColumn, wednesdayColumn, thursdayColumn, fridayColumn);

        findButton.setOnAction(e -> {
            String studentName = studentField.getText().trim();
            if (!studentName.isEmpty()) {
                Map<String, Map<String, String>> schedule = DatabaseHelper.getWeeklyScheduleForStudentWithTimes(studentName);


                ObservableList<Map<String, String>> data = FXCollections.observableArrayList();

                for (String time : TIME_SLOTS) {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("time", time);
                    for (String day : schedule.keySet()) {
                        row.put(day, schedule.get(day).get(time));

                    }
                    data.add(row);
                }
                tableView.setItems(data);
            }
        });

        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> primaryStage.setScene(new Scene(createMainMenu(primaryStage), 800, 600)));

        layout.getChildren().addAll(studentLabel, studentField, findButton, tableView, backButton);
        return new Scene(layout, 800, 600);
    }

    private TableColumn<Map<String, String>, String> createDayColumn(String day) {
        TableColumn<Map<String, String>, String> column = new TableColumn<>(day);
        column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(day)));
        return column;
    }



    private Scene createAvailableClassroomsScene(Stage primaryStage) {
        // Layout oluştur
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        // Ders adı girişi için Label ve TextField
        Label courseLabel = new Label("Enter a course name:");
        TextField courseField = new TextField();

        // Uygun sınıfları bulmak için Button
        Button findButton = new Button("Find Available Classrooms");

        // Sonuçları göstermek için ListView
        ListView<String> resultList = new ListView<>();

        // Dersin mevcut kapasitesini göstermek için Label
        Label courseCapacityLabel = new Label("Course capacity: Not available");

        // Arama işlemi
        findButton.setOnAction(e -> {
            String courseName = courseField.getText().trim();
            if (!courseName.isEmpty()) {
                // Ders kapasitesini al ve göster
                int studentCount = DatabaseHelper.getStudentCountForCourse(courseName);
                if (studentCount > 0) {
                    courseCapacityLabel.setText("Current Course capacity: " + studentCount + " students");
                } else {
                    courseCapacityLabel.setText("Course capacity: Not found");
                }

                // Uygun sınıfları al ve listele
                List<String> classrooms = DatabaseHelper.getAvailableClassrooms(courseName);
                resultList.getItems().clear();
                if (!classrooms.isEmpty()) {
                    resultList.getItems().addAll(classrooms);
                } else {
                    resultList.getItems().add("No available classrooms found for course: " + courseName);
                }
            } else {
                // Ders adı girilmemişse uyarı mesajı göster
                courseCapacityLabel.setText("Course capacity: Not available");
                resultList.getItems().clear();
                resultList.getItems().add("Please enter a course name.");
            }
        });

        // Geri dönmek için Back Button
        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> {
            Scene mainMenuScene = new Scene(createMainMenu(primaryStage), 800, 600);
            primaryStage.setScene(mainMenuScene);
        });

        // Layout içine bileşenleri ekle
        layout.getChildren().addAll(courseLabel, courseField, findButton, courseCapacityLabel, resultList, backButton);

        // Scene oluştur ve döndür
        return new Scene(layout, 800, 600);
    }



    private Scene createStudentCountScene(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label courseLabel = new Label("Enter course name:");
        TextField courseField = new TextField();
        Button findButton = new Button("Find Student Count");
        Label resultLabel = new Label();

        findButton.setOnAction(e -> {
            String courseName = courseField.getText().trim();
            if (!courseName.isEmpty()) {
                int studentCount = DatabaseHelper.getStudentCountForCourse(courseName);
                resultLabel.setText("Course: " + courseName + " has " + studentCount + " students.");
            } else {
                resultLabel.setText("Please enter a course name.");
            }
        });

        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> primaryStage.setScene(new Scene(createMainMenu(primaryStage), 800, 600)));

        layout.getChildren().addAll(courseLabel, courseField, findButton, resultLabel, backButton);

        return new Scene(layout, 800, 600);
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

    private Scene createAddStudentToCourseScene(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label studentLabel = new Label("Student Name:");
        TextField studentField = new TextField();

        Label courseLabel = new Label("Course Name:");
        TextField courseField = new TextField();

        Label timeLabel = new Label("Course Start Time:");
        TextField timeField = new TextField(); // Zaman bilgisi de gerekmekte

        Button addButton = new Button("Add Student to Course");
        Label resultLabel = new Label();

        addButton.setOnAction(e -> {
            String studentName = studentField.getText().trim();
            String courseName = courseField.getText().trim();
            String timeToStart = timeField.getText().trim();

            // 1. Input Kontrolü
            if (studentName.isEmpty() || courseName.isEmpty() || timeToStart.isEmpty()) {
                showAlert("Input Error", "Please fill out all fields (student, course, and time).");
                return;
            }

            // 2. Kurs var mı kontrolü
            if (!DatabaseHelper.courseExists(courseName, timeToStart)) {
                showAlert("Not Found", "No such course found at the specified time.");
                return;
            }

            // 3. Öğrenci var mı kontrolü - Bu metodu DatabaseHelper içine eklemeniz gerekir.
            if (!DatabaseHelper.studentExists(studentName)) {
                showAlert("Not Found", "Student not found. Please ensure the name is correct or register the student first.");
                return;
            }

            // 4. Öğrenciyi derse eklemeyi dene
            boolean success = DatabaseHelper.checkAndAddStudentToCourse(studentName, courseName);

            if (success) {
                showAlert("Success", "Student added to the course successfully!");
            } else {
                // Eklenemediyse zaman çakışması ya da kapasite sorunu olabilir.
                int currentStudentCount = DatabaseHelper.getStudentCountForCourse(courseName);

                // Varsayımsal bir fonksiyon. Course'un şu an atandığı classroom'u bulup kapasitesini getirir.
                int classroomCapacity = DatabaseHelper.getClassroomCapacity(courseName);

                if (classroomCapacity != -1 && currentStudentCount >= classroomCapacity) {
                    // Kapasite yetersiz. Re-assign dene.
                    Map<String, Set<String>> schedule = new HashMap<>();
                    int newStudentCount = currentStudentCount + 1;
                    boolean reassigned = reassignClassroomIfNeeded(courseName, newStudentCount, timeToStart, schedule);

                    if (reassigned) {
                        // Yeniden dene
                        boolean secondTry = DatabaseHelper.checkAndAddStudentToCourse(studentName, courseName);
                        if (secondTry) {
                            showAlert("Success", "Classroom capacity updated and student added successfully!");
                        } else {
                            showAlert("Error", "Failed to add student even after reassigning classrooms. Please try again.");
                        }
                    } else {
                        showAlert("Error", "No suitable classroom found to accommodate increased capacity.");
                    }
                } else {
                    // Kapasite sorunu değilse, bu büyük ihtimalle zaman çakışması veya başka bir hata.
                    showAlert("Error", "Failed to add student to the course. Possibly a scheduling conflict or another issue.");
                }
            }
        });

        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> {
            Scene mainMenuScene = new Scene(createMainMenu(primaryStage), 800, 600);
            primaryStage.setScene(mainMenuScene);
        });

        layout.getChildren().addAll(studentLabel, studentField, courseLabel, courseField, timeLabel, timeField, addButton, resultLabel, backButton);

        return new Scene(layout, 800, 600);
    }


    private Scene createSearchStudentScene(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 10;");

        Label searchLabel = new Label("Enter a lecturer name to search their courses:");
        TextField searchField = new TextField();
        Button searchButton = new Button("Search");
        ListView<String> resultsList = new ListView<>();

        searchButton.setOnAction(e -> {
            String studentName = searchField.getText().trim();
            if (!studentName.isEmpty()) {
                // Veritabanında hoca adına göre dersleri ara
                List<String> results = DatabaseHelper.searchCoursesByStudent(studentName);
                resultsList.getItems().clear();
                if (!results.isEmpty()) {
                    resultsList.getItems().addAll(results);
                } else {
                    resultsList.getItems().add("No courses found for lecturer: " + studentName);
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
    private Scene createSearchCourseScene(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label searchLabel = new Label("Enter a course name:");
        TextField searchField = new TextField();
        Button searchButton = new Button("Search");

        TableView<Map<String, Object>> tableView = new TableView<>();

        TableColumn<Map<String, Object>, String> lecturerColumn = new TableColumn<>("Lecturer");
        lecturerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("Lecturer").toString()));

        TableColumn<Map<String, Object>, String> durationColumn = new TableColumn<>("Duration");
        durationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("Duration").toString()));

        TableColumn<Map<String, Object>, String> studentCountColumn = new TableColumn<>("Student Count");
        studentCountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("Student Count").toString()));

        TableColumn<Map<String, Object>, String> viewListColumn = new TableColumn<>("Student List");
        viewListColumn.setCellFactory(col -> new TableCell<>() {
            final Button viewButton = new Button("View");

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                    viewButton.setOnAction(e -> {
                        Map<String, Object> rowData = getTableView().getItems().get(getIndex());
                        List<String> studentList = (List<String>) rowData.get("Student List");
                        showStudentListPopup(studentList);
                    });
                }
            }
        });

        tableView.getColumns().addAll(lecturerColumn, durationColumn, studentCountColumn, viewListColumn);

        searchButton.setOnAction(e -> {
            String courseName = searchField.getText().trim();
            if (!courseName.isEmpty()) {
                tableView.getItems().clear();

                Map<String, Object> results = DatabaseHelper.searchCourseDetails(courseName);
                if (results.containsKey("Message")) {
                    showAlert("Not Found", results.get("Message").toString());
                } else if (results.containsKey("Error")) {
                    showAlert("Error", results.get("Error").toString());
                } else {
                    Map<String, Object> rowData = new HashMap<>();
                    rowData.put("Lecturer", results.get("Lecturer"));
                    rowData.put("Duration", results.get("Duration") + " hrs");
                    rowData.put("Student Count", results.get("Student Count"));
                    rowData.put("Student List", results.get("Student List"));
                    tableView.getItems().add(rowData);
                }
            } else {
                showAlert("Input Error", "Please enter a course name.");
            }
        });

        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> primaryStage.setScene(new Scene(createMainMenu(primaryStage), 800, 600)));

        layout.getChildren().addAll(searchLabel, searchField, searchButton, tableView, backButton);

        return new Scene(layout, 800, 600);
    }



    private void showStudentListPopup(List<String> studentList) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Student List");

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(studentList);

        VBox layout = new VBox(10, new Label("Students:"), listView);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 300, 400);
        popupStage.setScene(scene);
        popupStage.show();
    }





    private Scene createClassroomCapacityScene(Stage primaryStage) {
        // Layout ve TableView oluşturma
        VBox layout = new VBox(10);
        TableView<String> tableView = new TableView<>();

        TableColumn<String, String> column = new TableColumn<>("Classroom Capacities");
        column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()));

        tableView.getColumns().add(column);
        tableView.getItems().addAll(DatabaseHelper.getClassroomCapacities());

        // Geri butonu
        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> primaryStage.setScene(new Scene(createMainMenu(primaryStage), 800, 600)));

        layout.getChildren().addAll(new Label("Classroom Capacities"), tableView, backButton);
        return new Scene(layout, 800, 600);
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



    private static final String DB_PATH = "jdbc:sqlite:database/TimetableManagement.db";

    private static final String CLASSROOM_DB_PATH = "jdbc:sqlite:database/ClassroomCapacity.db";

    public static List<String> assignAllCoursesToClassrooms() {
        List<String> assignments = new ArrayList<>();
        Map<String, Set<String>> schedule = new HashMap<>();

        try (Connection timetableConnection = DriverManager.getConnection(DB_PATH)) {

            String courseQuery = """
                SELECT courses.course_name,
                               COUNT(DISTINCT students.id) AS student_count,
                               courses.time_to_start
                        FROM course_students
                        INNER JOIN courses ON course_students.course_id = courses.id
                        INNER JOIN students ON course_students.student_id = students.id
                        GROUP BY courses.course_name, courses.time_to_start;
            """;

            try (PreparedStatement courseStatement = timetableConnection.prepareStatement(courseQuery);
                 ResultSet courseResultSet = courseStatement.executeQuery()) {

                while (courseResultSet.next()) {
                    String courseName = courseResultSet.getString("course_name");
                    int studentCount = courseResultSet.getInt("student_count");
                    String timeToStart = courseResultSet.getString("time_to_start");

                    String classroomAssignment = findBestClassroom(courseName, studentCount, timeToStart, schedule);
                    assignments.add(classroomAssignment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return assignments;
    }

    public static String findBestClassroom(String courseName, int studentCount, String timeToStart, Map<String, Set<String>> schedule) {
        try (Connection classroomConnection = DriverManager.getConnection(CLASSROOM_DB_PATH)) {

            String classroomQuery = """
                SELECT Classroom, Capacity 
                FROM classroom_capacity 
                WHERE Capacity >= ? 
                ORDER BY Capacity ASC;
            """;

            try (PreparedStatement classroomStatement = classroomConnection.prepareStatement(classroomQuery)) {
                classroomStatement.setInt(1, studentCount);

                try (ResultSet classroomResultSet = classroomStatement.executeQuery()) {
                    while (classroomResultSet.next()) {
                        String classroom = classroomResultSet.getString("Classroom");
                        int capacity = classroomResultSet.getInt("Capacity");

                        String dayTimeKey = timeToStart;

                        if (!schedule.containsKey(classroom)) {
                            schedule.put(classroom, new HashSet<>());
                        }

                        if (!schedule.get(classroom).contains(dayTimeKey)) {
                            schedule.get(classroom).add(dayTimeKey);
                            return "Course: " + courseName + ", Students: " + studentCount +
                                    ", Assigned Classroom: " + classroom + ", Capacity: " + capacity +
                                    ", Time: " + timeToStart;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Course: " + courseName + ", Students: " + studentCount +
                ", No suitable classroom found for Time: " + timeToStart;
    }

    public static Map<String, Map<String, String>> assignWeeklyScheduleToClassrooms() {
        Map<String, Map<String, String>> weeklySchedule = new HashMap<>();
        Map<String, Set<String>> schedule = new HashMap<>();

        try (Connection timetableConnection = DriverManager.getConnection(DB_PATH)) {

            String courseQuery = """
                SELECT courses.course_name,
                       COUNT(DISTINCT students.id) AS student_count,
                       courses.time_to_start
                FROM course_students
                INNER JOIN courses ON course_students.course_id = courses.id
                INNER JOIN students ON course_students.student_id = students.id
                GROUP BY courses.course_name, courses.time_to_start;
        """;

            try (PreparedStatement courseStatement = timetableConnection.prepareStatement(courseQuery);
                 ResultSet courseResultSet = courseStatement.executeQuery()) {

                while (courseResultSet.next()) {
                    String courseName = courseResultSet.getString("course_name");
                    int studentCount = courseResultSet.getInt("student_count");
                    String timeToStart = courseResultSet.getString("time_to_start");

                    String classroomAssignment = findBestClassroomForSchedule(courseName, studentCount, timeToStart, schedule);

                    // Classroom bazında haftalık program oluşturuluyor
                    if (!weeklySchedule.containsKey(classroomAssignment)) {
                        weeklySchedule.put(classroomAssignment, new HashMap<>());
                    }
                    weeklySchedule.get(classroomAssignment).put(courseName + " - " + timeToStart,
                            "Students: " + studentCount);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return weeklySchedule;
    }


    /*public static Map<String, Map<String, String>> assignWeeklyScheduleToClassrooms() {
        Map<String, Map<String, String>> weeklySchedule = new HashMap<>();
        Map<String, Set<String>> schedule = new HashMap<>();

        try (Connection timetableConnection = DriverManager.getConnection(TIMETABLE_DB_PATH)) {

            // 'day' sütunu kaldırıldı
            String courseQuery = """
                SELECT courses.course_name,
                       COUNT(DISTINCT students.id) AS student_count,
                       courses.time_to_start
                FROM course_students
                INNER JOIN courses ON course_students.course_id = courses.id
                INNER JOIN students ON course_students.student_id = students.id
                GROUP BY courses.course_name, courses.time_to_start;
        """;

            try (PreparedStatement courseStatement = timetableConnection.prepareStatement(courseQuery);
                 ResultSet courseResultSet = courseStatement.executeQuery()) {

                while (courseResultSet.next()) {
                    String courseName = courseResultSet.getString("course_name");
                    int studentCount = courseResultSet.getInt("student_count");
                    String timeToStart = courseResultSet.getString("time_to_start");

                    // 'day' parametresi kaldırıldı
                    String classroomAssignment = findBestClassroomForSchedule(courseName, studentCount, timeToStart, schedule);

                    // 'day' kullanılmadığı için sadece 'timeToStart' ile işlem yapıyoruz
                    if (!weeklySchedule.containsKey(timeToStart)) {
                        weeklySchedule.put(timeToStart, new HashMap<>());
                    }
                    weeklySchedule.get(timeToStart).put(courseName, classroomAssignment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return weeklySchedule;
    }




    public static String findBestClassroomForSchedule(String courseName, int studentCount, String timeToStart, Map<String, Set<String>> schedule) {
        try (Connection classroomConnection = DriverManager.getConnection(CLASSROOM_DB_PATH)) {

            String classroomQuery = """
        SELECT Classroom, Capacity
        FROM classroom_capacity
        WHERE Capacity >= ?
        ORDER BY Capacity ASC;
        """;

            try (PreparedStatement classroomStatement = classroomConnection.prepareStatement(classroomQuery)) {
                classroomStatement.setInt(1, studentCount);

                try (ResultSet classroomResultSet = classroomStatement.executeQuery()) {
                    while (classroomResultSet.next()) {
                        String classroom = classroomResultSet.getString("Classroom");
                        int capacity = classroomResultSet.getInt("Capacity");

                        // Sadece timeToStart kullanılarak key oluşturuluyor
                        String timeKey = timeToStart;

                        if (!schedule.containsKey(classroom)) {
                            schedule.put(classroom, new HashSet<>());
                        }

                        if (!schedule.get(classroom).contains(timeKey)) {
                            schedule.get(classroom).add(timeKey);
                            return "Course: " + courseName + ", Assigned Classroom: " + classroom +
                                    ", Capacity: " + capacity + ", Students: " + studentCount;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "No suitable classroom found for Course: " + courseName + ", Time: " + timeToStart;
    }

     */

    public static String findBestClassroomForSchedule(String courseName, int studentCount, String timeToStart, Map<String, Set<String>> schedule) {
        try (Connection classroomConnection = DriverManager.getConnection(CLASSROOM_DB_PATH)) {

            String classroomQuery = """
        SELECT Classroom, Capacity 
        FROM classroom_capacity 
        WHERE Capacity >= ? 
        ORDER BY Capacity ASC;
        """;

            try (PreparedStatement classroomStatement = classroomConnection.prepareStatement(classroomQuery)) {
                classroomStatement.setInt(1, studentCount);

                try (ResultSet classroomResultSet = classroomStatement.executeQuery()) {
                    while (classroomResultSet.next()) {
                        String classroom = classroomResultSet.getString("Classroom");
                        int capacity = classroomResultSet.getInt("Capacity");

                        // Classroom key ve zaman dilimi ile key oluşturuluyor
                        String timeKey = timeToStart;

                        if (!schedule.containsKey(classroom)) {
                            schedule.put(classroom, new HashSet<>());
                        }

                        if (!schedule.get(classroom).contains(timeKey)) {
                            schedule.get(classroom).add(timeKey);
                            return classroom;  // Sınıfın adı döndürülüyor
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "No suitable classroom found";  // Uygun bir sınıf bulunamadığında
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

            showUpperCaseAlert();
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

    public static void main(String[] args) throws SQLException {


        try (Connection connection = DriverManager.getConnection(DB_PATH);
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

        try (Connection connection = DriverManager.getConnection(DB_PATH)) {

            Statement statement = connection.createStatement();

            // Ders Kodlarını Getir
            System.out.println("Course Names:");
            ResultSet courseCodes = statement.executeQuery("""
        SELECT DISTINCT student_name AS code_or_name
        FROM students
        WHERE student_name GLOB '[A-Z][0-9]'
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
        WHERE student_name NOT GLOB '[A-Z][0-9]'
        
    """);

            while (studentNames.next()) {
                System.out.println("Student Name: " + studentNames.getString("code_or_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection connection = DriverManager.getConnection(DB_PATH);
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
            students.student_name
    """);

            while (courseStudents.next()) {
                String studentName = courseStudents.getString("student_name");
                String courses = courseStudents.getString("courses");

                System.out.println("Student Name: " + studentName + ", Courses: " + courses);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


          DatabaseSetup.setupDatabase();
        //  DatabaseHelper.removeDuplicates();

         //CSV dosyasını veritabanına aktar
        CSVToDatabase.importCSV("csv/ClassroomCapacity.csv");


        System.out.println("DB_PATH: " + DB_PATH);
        SecondDatabase.createDatabaseDirectory(); //bunu bi defa çalıştırıp yoruma alın
        SecondDatabase.importClassroomCapacity("csv/ClassroomCapacity.csv");


        List<String> assignments = assignAllCoursesToClassrooms();
        assignments.forEach(System.out::println);

      //  DatabaseHelper.cleanDatabase();



        // JavaFX uygulamasını başlat
        launch(args);

    }

}