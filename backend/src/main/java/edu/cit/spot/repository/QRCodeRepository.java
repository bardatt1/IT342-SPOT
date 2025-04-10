package edu.cit.spot.repository;

import edu.cit.spot.entity.QRCode;
import edu.cit.spot.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QRCodeRepository extends JpaRepository<QRCode, Long> {
    List<QRCode> findBySessionAndIsActiveTrue(Session session);
    Optional<QRCode> findByUuidAndIsActiveTrue(String uuid);
    List<QRCode> findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime currentTime);
}
