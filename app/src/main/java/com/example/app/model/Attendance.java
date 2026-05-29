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
@Document(collection = "AttendanceData")
public class Attendance {
    @Id
    private String id;
    private String date;         // e.g., "2026-05-27"
    private Integer period;      // e.g., 1
    private String subject;
    private String topic;
    private String facultyName;
    private String phoneNumber;
    private String remarks;
    private String year;         // e.g., "1st Year"
    private String department;   // e.g., "CSE"
    private String section;      // e.g., "A"
    private List<StudentStatus> attendance;

    // --- NESTED STUDENT STATUS OBJECT ---
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentStatus {
        private String rollNumber;
        private String name;
        private String status;   // "present" or "absent"
    }
}
