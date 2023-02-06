package com.airline.api.persistence.repositories;

import com.airline.api.persistence.domain.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IFlightRepository extends JpaRepository<Flight, Long> {
}
