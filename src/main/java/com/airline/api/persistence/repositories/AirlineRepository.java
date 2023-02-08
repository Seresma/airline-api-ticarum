package com.airline.api.persistence.repositories;

import com.airline.api.persistence.model.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirlineRepository extends JpaRepository<Airline, Long> {
    Airline findByNameIgnoreCase(String name);
}
