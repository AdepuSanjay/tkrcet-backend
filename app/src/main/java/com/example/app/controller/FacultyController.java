package com.example.app.controller;

import com.example.app.dto.ApiResponse;
import com.example.app.model.Faculty;
import com.example.app.repository.FacultyRepository;
import com.example.app.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createFacultyProfile(
            @RequestParam("employeeId") String employeeId,
            @RequestParam("name") String name,
            @RequestParam("designation") String designation,
            @RequestParam("experience") String experience,
            @RequestParam("mobileNumber") String mobileNumber,
            @RequestParam("password") String password,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            Faculty faculty = new Faculty();
            faculty.setEmployeeId(employeeId);
            faculty.setName(name);
            faculty.setDesignation(designation);
            faculty.setExperience(experience);
            faculty.setMobileNumber(mobileNumber);
            faculty.setPassword(password);
            faculty.setPersonalTimetable(new ArrayList<>());

            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(imageFile);
                faculty.setImage(imageUrl);
            }

            return ResponseEntity.ok(facultyRepository.save(faculty));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to create faculty profile: " + e.getMessage()));
        }
    }

    @GetMapping
    public List<Faculty> getAllFacultyProfiles() {
        return facultyRepository.findAll();
    }
}
