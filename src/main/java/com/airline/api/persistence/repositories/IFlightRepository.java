package com.airline.api.persistence.repositories;

import com.airline.api.persistence.domain.StatusEnum;
import com.airline.api.persistence.domain.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IFlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByStatus(StatusEnum statusEnum);
}
