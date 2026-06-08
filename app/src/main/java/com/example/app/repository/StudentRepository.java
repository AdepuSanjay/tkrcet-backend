package com.example.app.repository;

import com.example.app.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends MongoRepository<Student, String> {
    Optional<Student> findByRollNumber(String rollNumber);

    // ADDED: Fetch students registered under a specific class config
    List<Student> findByYearAndDepartmentAndSection(String year, String department, String section);
}
