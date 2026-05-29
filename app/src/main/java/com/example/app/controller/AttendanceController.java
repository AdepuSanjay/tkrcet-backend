package com.example.app.controller;

import com.example.app.dto.ApiResponse;
import com.example.app.model.Attendance;
import com.example.app.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

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
}
