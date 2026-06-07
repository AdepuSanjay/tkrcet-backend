package com.example.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "StudentData")
public class Student {
    @Id
    private String id;
    private String rollNumber;
    private String name;
    private String fatherName;
    private String password;
    private String role = "student";
    private String image; 
    private String mobileNumber;
    private String fatherMobileNumber;
    private String year;
    private String department;
    private String section;    
}
