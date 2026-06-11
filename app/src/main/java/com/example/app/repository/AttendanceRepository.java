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

    // ADD THIS NEW LINE FOR THE ACTIVITY DIARY:
    List<Attendance> findByYearAndDepartmentAndSectionAndSubject(String year, String department, String section, String subject);
}
