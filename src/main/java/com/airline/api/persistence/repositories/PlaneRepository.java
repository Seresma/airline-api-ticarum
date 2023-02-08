package com.airline.api.persistence.repositories;

import com.airline.api.persistence.model.Plane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaneRepository extends JpaRepository<Plane, Long> {
    Plane findByRegistrationCode(String registrationCode);
}
