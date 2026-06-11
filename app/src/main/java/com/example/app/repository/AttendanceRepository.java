package com.example.app.repository;

import com.example.app.model.Attendance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends MongoRepository<Attendance, String> {

    List<Attendance> findByDateAndYearAndDepartmentAndSectionAndPeriod(
            String date, String year, String department, String section, Integer period
    );

    List<Attendance> findByYearAndDepartmentAndSection(String year, String department, String section);

    // FIXED: Now strictly filters by the specific faculty member's name too!
    List<Attendance> findByYearAndDepartmentAndSectionAndSubjectAndFacultyName(
            String year, String department, String section, String subject, String facultyName
    );
}
