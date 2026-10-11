package raf.rs.machinescheduler.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import raf.rs.machinescheduler.model.ErrorMessage;

import java.util.List;

public interface ErrorMessageRepo extends JpaRepository<ErrorMessage, Long> {
    List<ErrorMessage> findByUserId(Long userId);
}
