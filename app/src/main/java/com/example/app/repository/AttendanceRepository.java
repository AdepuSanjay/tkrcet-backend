package com.example.app.repository;

import com.example.app.model.Attendance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends MongoRepository<Attendance, String> {
    
    // Custom finder to check or fetch existing logs for a specific class slot
    List<Attendance> findByDateAndYearAndDepartmentAndSectionAndPeriod(
            String date, String year, String department, String section, Integer period
    );
}
