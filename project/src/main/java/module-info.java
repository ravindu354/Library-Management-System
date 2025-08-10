module library.management.system {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    
    opens com.library to javafx.graphics;
    opens com.library.controllers to javafx.fxml;
    opens com.library.models to javafx.base;
    
    exports com.library;
    exports com.library.controllers;
    exports com.library.models;
    exports com.library.utils;
}