package com.rentguard.user.repository;

import com.rentguard.user.model.Owner;
import com.rentguard.user.model.User;
import com.rentguard.user.model.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {
    Optional<Owner> findByUser(User user);
    Optional<Owner> findByUserId(Long userId);
    List<Owner> findByVerificationStatus(VerificationStatus status);
    long countByVerificationStatus(VerificationStatus status);
}
