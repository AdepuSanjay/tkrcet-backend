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

    // ADDED: Fetch all history sheets for an entire class room configuration
    List<Attendance> findByYearAndDepartmentAndSection(String year, String department, String section);
}
