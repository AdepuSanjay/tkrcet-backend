package com.example.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "FacultyData")
public class Faculty {
    @Id
    private String id;
    private String employeeId;
    private String name;
    private String department; // NEW: Added department field
    private String designation;
    private String experience;
    private String mobileNumber;
    private String password;
    private String role = "teacher"; 
    private String image; // Field to capture Cloudinary secure URL asset
    private List<FacultySlot> personalTimetable;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacultySlot {
        private String day;
        private Integer periodNumber;
        private String subject;
        private String yearId;
        private String deptName;
        private String sectionName;
    }
}
