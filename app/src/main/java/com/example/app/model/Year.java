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
@Document(collection = "SectionData")
public class Year {
    @Id
    private String id;
    private String year;
    private List<Department> departments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Department {
        private String name;
        private List<Section> sections;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Section {
        private String name;
        private List<SectionTimetable> timetable;
        private List<Student> students;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionTimetable {
        private String day;
        private List<Period> periods;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Period {
        private Integer periodNumber;
        private String subject;
        private String facultyId; 
        private String facultyName;
        private String phoneNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Student {
        private String rollNumber;
        private String name;
        private String fatherName;
        private String password;
        private String role = "student";
        private String image; // Stores Cloudinary URL directly
        private String mobileNumber;
        private String fatherMobileNumber;
    }
}
