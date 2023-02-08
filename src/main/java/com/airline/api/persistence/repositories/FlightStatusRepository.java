package com.airline.api.persistence.repositories;

import com.airline.api.persistence.model.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightStatusRepository extends JpaRepository<FlightStatus, Long> {
}
