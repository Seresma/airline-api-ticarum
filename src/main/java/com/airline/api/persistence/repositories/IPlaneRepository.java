package com.airline.api.persistence.repositories;

import com.airline.api.persistence.domain.Plane;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPlaneRepository extends JpaRepository<Plane, Long> {
    Plane findByRegistrationCode(String registrationCode);
}
