package com.example.app.repository;

import com.example.app.model.Year;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YearRepository extends MongoRepository<Year, String> {
    
    // Spring Boot automatically implements this based on the method name
    // Useful if you want to find specific data by the year string (e.g., "1st Year")
    Year findByYear(String year); 
}
