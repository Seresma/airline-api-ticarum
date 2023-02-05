package com.airline.api.persistence.repositories;

import com.airline.api.persistence.domain.FlightStatusEnum;
import com.airline.api.persistence.domain.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IFlightRepository extends JpaRepository<Flight, Long> {
}
