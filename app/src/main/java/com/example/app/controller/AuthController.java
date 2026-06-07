package com.example.app.controller;

import com.example.app.model.Faculty;
import com.example.app.model.Student;
import com.example.app.repository.FacultyRepository;
import com.example.app.repository.StudentRepository;
import com.example.app.service.JwtService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        
        // 1. Check if the user is a FACULTY member
        Optional<Faculty> facultyOpt = facultyRepository.findByEmployeeId(loginRequest.getUserId());
        if (facultyOpt.isPresent() && facultyOpt.get().getPassword().equals(loginRequest.getPassword())) {
            String token = jwtService.generateToken(facultyOpt.get().getEmployeeId());
            // Pass the official Employee ID from the database
            return ResponseEntity.ok(new AuthResponse(token, facultyOpt.get().getRole(), facultyOpt.get().getName(), facultyOpt.get().getImage(), facultyOpt.get().getEmployeeId()));
        }

        // 2. Check if the user is a STUDENT
        Optional<Student> studentOpt = studentRepository.findByRollNumber(loginRequest.getUserId());
        if (studentOpt.isPresent() && studentOpt.get().getPassword().equals(loginRequest.getPassword())) {
            String token = jwtService.generateToken(studentOpt.get().getRollNumber());
            // Pass the official Roll Number from the database
            return ResponseEntity.ok(new AuthResponse(token, studentOpt.get().getRole(), studentOpt.get().getName(), studentOpt.get().getImage(), studentOpt.get().getRollNumber()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
}

@Data
class LoginRequest {
    private String userId; 
    private String password;
}

@Data
class AuthResponse {
    private String token;
    private String role;
    private String name;
    private String profileImage;
    private String userId; // NEW: Added to ensure frontend gets the exact DB ID

    public AuthResponse(String token, String role, String name, String profileImage, String userId) {
        this.token = token;
        this.role = role;
        this.name = name;
        this.profileImage = profileImage;
        this.userId = userId;
    }
}
