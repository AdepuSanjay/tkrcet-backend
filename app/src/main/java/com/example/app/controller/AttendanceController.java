package com.example.app.controller;

import com.example.app.dto.ApiResponse;
import com.example.app.model.Attendance;
import com.example.app.model.Student;
import com.example.app.repository.AttendanceRepository;
import com.example.app.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    /**
     * POST: Submit a new attendance sheet
     */
    @PostMapping
    public ResponseEntity<?> submitAttendance(@RequestBody Attendance attendance) {
        // Validation: Prevent duplicate submissions for the same period on the same day
        List<Attendance> existingRecords = attendanceRepository.findByDateAndYearAndDepartmentAndSectionAndPeriod(
                attendance.getDate(),
                attendance.getYear(),
                attendance.getDepartment(),
                attendance.getSection(),
                attendance.getPeriod()
        );

        if (!existingRecords.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Attendance already marked for this period today."));
        }

        attendanceRepository.save(attendance);
        return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Attendance sheet submitted successfully."));
    }

    /**
     * GET: Fetch all attendance history
     */
    @GetMapping
    public List<Attendance> getAllAttendanceRecords() {
        return attendanceRepository.findAll();
    }

    /**
     * GET: Filter attendance records dynamically for a specific class slot
     */
    @GetMapping("/search")
    public ResponseEntity<?> getSpecificAttendance(
            @RequestParam String date,
            @RequestParam String year,
            @RequestParam String department,
            @RequestParam String section,
            @RequestParam Integer period) {

        List<Attendance> records = attendanceRepository.findByDateAndYearAndDepartmentAndSectionAndPeriod(
                date, year, department, section, period
        );

        if (records.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "No logs found for the specified class configuration."));
        }

        // Returns the actual matching attendance document (not wrapped in ApiResponse)
        // so your frontend can easily map through the 'attendance' array to render the UI.
        return ResponseEntity.ok(records.get(0)); 
    }

    /**
     * NEW: Fetch students list belonging to a specific class config for marking sheets
     * URL Example: /api/attendance/students-list?year=B.Tech I&department=CSE&section=A
     */
    @GetMapping("/students-list")
    public ResponseEntity<?> getStudentsForMarking(
            @RequestParam String year,
            @RequestParam String department,
            @RequestParam String section) {
        
        List<Student> students = studentRepository.findByYearAndDepartmentAndSection(year, department, section);
        
        if (students.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "No students registered inside this section combination."));
        }
        
        return ResponseEntity.ok(students);
    }

    /**
     * NEW: Fetch overall cumulative percentage report for all students in a specific section
     * URL Example: /api/attendance/summary?year=B.Tech I&department=CSE&section=A
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getSectionAttendanceSummary(
            @RequestParam String year,
            @RequestParam String department,
            @RequestParam String section) {

        // 1. Fetch total matching recorded sheets for this class combination
        List<Attendance> sheets = attendanceRepository.findByYearAndDepartmentAndSection(year, department, section);
        int totalClassesHeld = sheets.size();

        // 2. Fetch all registered students to map metrics neatly even if they have 0 logs
        List<Student> studentList = studentRepository.findByYearAndDepartmentAndSection(year, department, section);

        // 3. Map compilation structure
        Map<String, Map<String, Object>> analysisMap = new HashMap<>();
        for (Student s : studentList) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("name", s.getName());
            stats.put("rollNumber", s.getRollNumber());
            stats.put("presentCount", 0);
            stats.put("absentCount", 0);
            stats.put("percentage", "0.0%");
            analysisMap.put(s.getRollNumber(), stats);
        }

        // 4. Compute presents and absents from the matrix sheets on MongoDB
        for (Attendance sheet : sheets) {
            if (sheet.getAttendance() != null) {
                for (Attendance.StudentStatus status : sheet.getAttendance()) {
                    if (analysisMap.containsKey(status.getRollNumber())) {
                        Map<String, Object> targetStats = analysisMap.get(status.getRollNumber());
                        if ("present".equalsIgnoreCase(status.getStatus())) {
                            int current = (int) targetStats.get("presentCount");
                            targetStats.put("presentCount", current + 1);
                        } else {
                            int current = (int) targetStats.get("absentCount");
                            targetStats.put("absentCount", current + 1);
                        }
                    }
                }
            }
        }

        // 5. Finalize mathematical evaluation safely
        List<Map<String, Object>> finalResultList = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : analysisMap.entrySet()) {
            Map<String, Object> studentStats = entry.getValue();
            int presents = (int) studentStats.get("presentCount");
            
            if (totalClassesHeld > 0) {
                double computedPct = ((double) presents / totalClassesHeld) * 100;
                studentStats.put("percentage", String.format("%.1f%%", computedPct));
            }
            finalResultList.add(studentStats);
        }

        Map<String, Object> finalResponse = new HashMap<>();
        finalResponse.put("totalClassesHeld", totalClassesHeld);
        finalResponse.put("studentsReport", finalResultList);

        return ResponseEntity.ok(finalResponse);
    }

    /**
     * NEW: Fetch detailed attendance history and percentage for a SINGLE student
     * URL Example: GET /api/attendance/student/100
     */
    @GetMapping("/student/{rollNumber}")
    public ResponseEntity<?> getSingleStudentAttendance(@PathVariable String rollNumber) {
        
        // 1. Verify the student exists and get their class details
        Optional<Student> studentOpt = studentRepository.findByRollNumber(rollNumber);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Student details not found."));
        }
        
        Student student = studentOpt.get();

        // 2. Fetch all attendance sheets for this student's specific class
        List<Attendance> classSheets = attendanceRepository.findByYearAndDepartmentAndSection(
                student.getYear(), student.getDepartment(), student.getSection()
        );

        int totalClasses = 0;
        int presentCount = 0;
        int absentCount = 0;
        
        // This will hold the day-by-day breakdown for the frontend table
        List<Map<String, Object>> detailedHistory = new ArrayList<>();

        // 3. Loop through the class sheets and extract only THIS student's status
        for (Attendance sheet : classSheets) {
            if (sheet.getAttendance() != null) {
                for (Attendance.StudentStatus status : sheet.getAttendance()) {
                    
                    // Match the roll number (case-insensitive to be safe)
                    if (status.getRollNumber().trim().equalsIgnoreCase(rollNumber.trim())) {
                        totalClasses++;
                        
                        // Save the details of this specific class
                        Map<String, Object> recordDetail = new HashMap<>();
                        recordDetail.put("date", sheet.getDate());
                        recordDetail.put("period", sheet.getPeriod());
                        recordDetail.put("subject", sheet.getSubject());
                        recordDetail.put("facultyName", sheet.getFacultyName());
                        recordDetail.put("status", status.getStatus());
                        
                        detailedHistory.add(recordDetail);

                        // Increment counts
                        if ("present".equalsIgnoreCase(status.getStatus())) {
                            presentCount++;
                        } else {
                            absentCount++;
                        }
                        
                        break; // We found the student for this period, move to next sheet
                    }
                }
            }
        }

        // 4. Calculate Percentage safely
        String percentage = "0.0%";
        if (totalClasses > 0) {
            double calc = ((double) presentCount / totalClasses) * 100;
            percentage = String.format("%.1f%%", calc);
        }

        // 5. Package everything beautifully for the React frontend
        Map<String, Object> response = new HashMap<>();
        response.put("studentName", student.getName());
        response.put("rollNumber", student.getRollNumber());
        response.put("totalClasses", totalClasses);
        response.put("presentCount", presentCount);
        response.put("absentCount", absentCount);
        response.put("percentage", percentage);
        response.put("history", detailedHistory);

        return ResponseEntity.ok(response);
    }
}
