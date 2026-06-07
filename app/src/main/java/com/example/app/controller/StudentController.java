package com.example.app.controller;

import com.example.app.dto.ApiResponse;
import com.example.app.model.Student;
import com.example.app.model.Year;
import com.example.app.model.Year.SectionTimetable;
import com.example.app.repository.StudentRepository;
import com.example.app.repository.YearRepository;
import com.example.app.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private YearRepository yearRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    // --- 1. DASHBOARD FETCH API ---
    @GetMapping("/{rollNumber}/dashboard")
    public ResponseEntity<?> getStudentDashboard(@PathVariable String rollNumber) {
        
        // 1. Instantly find the student using the new standalone repository
        Optional<Student> studentOpt = studentRepository.findByRollNumber(rollNumber);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Student details not found."));
        }

        Student student = studentOpt.get();
        List<SectionTimetable> studentTimetable = new ArrayList<>();

        // 2. Fetch only their specific timetable from the SectionData collection
        List<Year> allYears = yearRepository.findAll();
        for (Year y : allYears) {
            if (y.getYear().equalsIgnoreCase(student.getYear()) && y.getDepartments() != null) {
                for (Year.Department d : y.getDepartments()) {
                    if (d.getName().equalsIgnoreCase(student.getDepartment()) && d.getSections() != null) {
                        for (Year.Section s : d.getSections()) {
                            if (s.getName().equalsIgnoreCase(student.getSection()) && s.getTimetable() != null) {
                                studentTimetable = s.getTimetable();
                            }
                        }
                    }
                }
            }
        }

        // 3. Package it securely for the React frontend
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("student", student);
        responseData.put("academicYear", student.getYear());
        responseData.put("department", student.getDepartment());
        responseData.put("sectionName", student.getSection());
        responseData.put("timetable", studentTimetable);

        return ResponseEntity.ok(responseData);
    }

    // --- 2. CREATE STUDENT API ---
    // Keeping your URL structure the same so your frontend form doesn't break!
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
            // Check if roll number already exists
            if (studentRepository.findByRollNumber(rollNumber).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Roll Number already exists!"));
            }

            Optional<Year> optionalYear = yearRepository.findById(yearId);
            if (optionalYear.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Year record not found."));
            }
            
            Student newStudent = new Student();
            newStudent.setRollNumber(rollNumber);
            newStudent.setName(name);
            newStudent.setFatherName(fatherName);
            newStudent.setPassword(password);
            newStudent.setMobileNumber(mobileNumber);
            newStudent.setFatherMobileNumber(fatherMobileNumber);
            
            // Map the student to their class based on the URL path
            newStudent.setYear(optionalYear.get().getYear());
            newStudent.setDepartment(deptName);
            newStudent.setSection(sectionName);

            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(imageFile);
                newStudent.setImage(imageUrl);
            }

            // Save directly to the standalone Student collection!
            studentRepository.save(newStudent);
            
            return ResponseEntity.ok(new ApiResponse(true, "Student added successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }
}
