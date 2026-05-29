package com.example.app.controller;

import com.example.app.model.Faculty;
import com.example.app.model.Year;
import com.example.app.repository.FacultyRepository;
import com.example.app.repository.YearRepository;
import com.example.app.service.JwtService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private YearRepository yearRepository;

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
            return ResponseEntity.ok(new AuthResponse(token, facultyOpt.get().getRole(), facultyOpt.get().getName(), facultyOpt.get().getImage()));
        }

        // 2. Check if the user is a STUDENT
        List<Year> allYears = yearRepository.findAll();
        for (Year year : allYears) {
            if (year.getDepartments() != null) {
                for (var dept : year.getDepartments()) {
                    if (dept.getSections() != null) {
                        for (var section : dept.getSections()) {
                            if (section.getStudents() != null) {
                                for (var student : section.getStudents()) {
                                    if (student.getRollNumber().equals(loginRequest.getUserId()) &&
                                        student.getPassword().equals(loginRequest.getPassword())) {
                                        
                                        String token = jwtService.generateToken(student.getRollNumber());
                                        return ResponseEntity.ok(new AuthResponse(token, student.getRole(), student.getName(), student.getImage()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
}

@Data
class LoginRequest {
    private String userId; // Can be rollNumber OR employeeId
    private String password;
}

@Data
class AuthResponse {
    private String token;
    private String role;
    private String name;
    private String profileImage;

    public AuthResponse(String token, String role, String name, String profileImage) {
        this.token = token;
        this.role = role;
        this.name = name;
        this.profileImage = profileImage;
    }
}
