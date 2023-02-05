package com.airline.api.persistence.repositories;

import com.airline.api.persistence.domain.Airline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAirlineRepository extends JpaRepository<Airline, Long> {
    Airline findByNameIgnoreCase(String name);
}
