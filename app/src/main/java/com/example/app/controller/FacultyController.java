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
import java.util.Optional;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    // CREATE FACULTY
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createFacultyProfile(
            @RequestParam("employeeId") String employeeId,
            @RequestParam("name") String name,
            @RequestParam("department") String department, // NEW
            @RequestParam("designation") String designation,
            @RequestParam("experience") String experience,
            @RequestParam("mobileNumber") String mobileNumber,
            @RequestParam("password") String password,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            Faculty faculty = new Faculty();
            faculty.setEmployeeId(employeeId);
            faculty.setName(name);
            faculty.setDepartment(department); // NEW
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

    // UPDATE FACULTY LOGIC
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateFacultyProfile(
            @PathVariable String id,
            @RequestParam(value = "employeeId", required = false) String employeeId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "designation", required = false) String designation,
            @RequestParam(value = "experience", required = false) String experience,
            @RequestParam(value = "mobileNumber", required = false) String mobileNumber,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            Optional<Faculty> optionalFaculty = facultyRepository.findById(id);
            if (optionalFaculty.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Faculty not found."));
            }
            
            Faculty faculty = optionalFaculty.get();

            // Only update fields that are provided in the request
            if (employeeId != null) faculty.setEmployeeId(employeeId);
            if (name != null) faculty.setName(name);
            if (department != null) faculty.setDepartment(department);
            if (designation != null) faculty.setDesignation(designation);
            if (experience != null) faculty.setExperience(experience);
            if (mobileNumber != null) faculty.setMobileNumber(mobileNumber);
            if (password != null) faculty.setPassword(password);

            // Update Cloudinary image if a new one is uploaded
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(imageFile);
                faculty.setImage(imageUrl);
            }

            facultyRepository.save(faculty);
            return ResponseEntity.ok(new ApiResponse(true, "Faculty profile updated successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to update faculty profile: " + e.getMessage()));
        }
    }

    // GET ALL FACULTY
    @GetMapping
    public List<Faculty> getAllFacultyProfiles() {
        return facultyRepository.findAll();
    }
}
