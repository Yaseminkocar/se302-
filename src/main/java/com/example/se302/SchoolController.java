package com.example.se302;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;

public class SchoolController {

    @FXML private MenuItem newItem;
    @FXML private MenuItem openItem;
    @FXML private MenuItem saveItem;
    @FXML private MenuItem saveAsItem;
    @FXML private MenuItem exitItem;

    @FXML private MenuItem addStudentItem;
    @FXML private MenuItem removeStudentItem;
    @FXML private MenuItem assignClassItem;
    @FXML private MenuItem viewDetailsItem;

    @FXML private MenuItem helpItem;
    @FXML private MenuItem aboutItem;

    @FXML private Button newButton;
    @FXML private Button openButton;
    @FXML private Button saveButton;
    @FXML private Button addButton;
    @FXML private Button removeButton;
    @FXML private Button assignButton;
    @FXML private Button helpButton;

    // Controller başlatma işlemleri
    public void init() {
        // Butonlara ve menülere işlevsellik ekleyin
        newButton.setOnAction(e -> showNewStudentForm());
        addButton.setOnAction(e -> showAddNewStudentForm());
        // Diğer butonlara ve menülere event handler ekleyebilirsiniz.
    }

    // Yeni öğrenci ekleme formunu gösterme
    private void showNewStudentForm() {
        System.out.println("New Student Form");
    }

    // Öğrenci ekleme işlemi
    private void showAddNewStudentForm() {
        System.out.println("Add New Student Form");
        // Öğrenci ekleme işlevini burada gerçekleştirin
    }
}


