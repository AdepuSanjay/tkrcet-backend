package com.example.app.controller;

import com.example.app.dto.ApiResponse;
import com.example.app.model.Year;
import com.example.app.model.Year.Student;
import com.example.app.repository.YearRepository;
import com.example.app.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private YearRepository yearRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping(value = "/{yearId}/departments/{deptName}/sections/{sectionName}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> addStudentToSection(
            @PathVariable String yearId,
            @PathVariable String deptName,
            @PathVariable String sectionName,
            @RequestParam("rollNumber") String rollNumber,
            @RequestParam("name") String name,
            @RequestParam("fatherName") String fatherName,
            @RequestParam("password") String password,
            @RequestParam("mobileNumber") String mobileNumber,
            @RequestParam("fatherMobileNumber") String fatherMobileNumber,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        try {
            Optional<Year> optionalYear = yearRepository.findById(yearId);
            if (optionalYear.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Year record not found."));
            }
            
            Year year = optionalYear.get();
            Student newStudent = new Student();
            newStudent.setRollNumber(rollNumber);
            newStudent.setName(name);
            newStudent.setFatherName(fatherName);
            newStudent.setPassword(password);
            newStudent.setMobileNumber(mobileNumber);
            newStudent.setFatherMobileNumber(fatherMobileNumber);

            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(imageFile);
                newStudent.setImage(imageUrl);
            }

            final boolean[] updated = {false};

            if (year.getDepartments() != null) {
                year.getDepartments().stream()
                    .filter(d -> d.getName().equalsIgnoreCase(deptName))
                    .findFirst()
                    .ifPresent(dept -> {
                        if (dept.getSections() != null) {
                            dept.getSections().stream()
                                .filter(s -> s.getName().equalsIgnoreCase(sectionName))
                                .findFirst()
                                .ifPresent(section -> {
                                    if (section.getStudents() == null) section.setStudents(new ArrayList<>());
                                    section.getStudents().add(newStudent);
                                    updated[0] = true;
                                });
                        }
                    });
            }

            if (!updated[0]) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Invalid path."));

            yearRepository.save(year);
            return ResponseEntity.ok(new ApiResponse(true, "Student added successfully with image."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }
}
