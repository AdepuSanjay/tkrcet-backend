package com.example.app.controller;

import com.example.app.dto.ApiResponse;
import com.example.app.model.Year;
import com.example.app.model.Year.SectionTimetable;
import com.example.app.model.Faculty;
import com.example.app.model.Faculty.FacultySlot;
import com.example.app.repository.YearRepository;
import com.example.app.repository.FacultyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/section-data")
public class YearController {

    @Autowired
    private YearRepository yearRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @GetMapping
    public List<Year> getAllData() {
        return yearRepository.findAll();
    }

    @PostMapping
    public Year createData(@RequestBody Year year) {
        return yearRepository.save(year);
    }

    @PutMapping("/{yearId}/departments/{deptName}/sections/{sectionName}/timetable")
    public ResponseEntity<?> updateSectionTimetable(
            @PathVariable String yearId, @PathVariable String deptName,
            @PathVariable String sectionName, @RequestBody List<SectionTimetable> newTimetable) {

        Optional<Year> optionalYear = yearRepository.findById(yearId);
        if (optionalYear.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Year not found."));

        Year year = optionalYear.get();
        final boolean[] updated = {false};

        if (year.getDepartments() != null) {
            year.getDepartments().stream()
                .filter(d -> d.getName().equalsIgnoreCase(deptName) && d.getSections() != null)
                .flatMap(d -> d.getSections().stream())
                .filter(s -> s.getName().equalsIgnoreCase(sectionName))
                .findFirst()
                .ifPresent(section -> {
                    section.setTimetable(newTimetable);
                    updated[0] = true;
                });
        }

        if (!updated[0]) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Path not found."));

        yearRepository.save(year);
        return ResponseEntity.ok(new ApiResponse(true, "Timetable assigned successfully."));
    }

    @PutMapping("/{yearId}/departments/{deptName}/sections/{sectionName}/timetable/{day}/periods/{periodNumber}/assign-faculty")
    public ResponseEntity<?> assignFacultyToPeriod(
            @PathVariable String yearId, @PathVariable String deptName, @PathVariable String sectionName,
            @PathVariable String day, @PathVariable Integer periodNumber,
            @RequestParam String employeeId, @RequestParam String subjectName) {

        Optional<Faculty> optionalFaculty = facultyRepository.findByEmployeeId(employeeId);
        if (optionalFaculty.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Faculty not found."));
        Faculty faculty = optionalFaculty.get();

        if (faculty.getPersonalTimetable() != null) {
            boolean isClashing = faculty.getPersonalTimetable().stream()
                    .anyMatch(slot -> slot.getDay().equalsIgnoreCase(day) && slot.getPeriodNumber().equals(periodNumber));
            
            if (isClashing) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Clash Alert!"));
        } else {
            faculty.setPersonalTimetable(new ArrayList<>());
        }

        Optional<Year> optionalYear = yearRepository.findById(yearId);
        if (optionalYear.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Year not found."));
        Year year = optionalYear.get();

        final boolean[] yearUpdated = {false};

        if (year.getDepartments() != null) {
            year.getDepartments().stream()
                .filter(d -> d.getName().equalsIgnoreCase(deptName) && d.getSections() != null)
                .flatMap(d -> d.getSections().stream())
                .filter(s -> s.getName().equalsIgnoreCase(sectionName) && s.getTimetable() != null)
                .flatMap(s -> s.getTimetable().stream())
                .filter(t -> t.getDay().equalsIgnoreCase(day) && t.getPeriods() != null)
                .flatMap(t -> t.getPeriods().stream())
                .filter(p -> p.getPeriodNumber().equals(periodNumber))
                .findFirst()
                .ifPresent(period -> {
                    period.setSubject(subjectName);
                    period.setFacultyId(faculty.getId());
                    period.setFacultyName(faculty.getName());
                    period.setPhoneNumber(faculty.getMobileNumber());
                    yearUpdated[0] = true;
                });
        }

        if (!yearUpdated[0]) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Path invalid."));

        FacultySlot newSlot = new FacultySlot(day, periodNumber, subjectName, year.getYear(), deptName, sectionName);
        faculty.getPersonalTimetable().add(newSlot);
        
        facultyRepository.save(faculty);
        yearRepository.save(year);

        return ResponseEntity.ok(new ApiResponse(true, "Faculty assigned safely."));
    }
}
