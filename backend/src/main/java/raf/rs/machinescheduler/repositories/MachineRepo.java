package raf.rs.machinescheduler.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import raf.rs.machinescheduler.model.Machine;

import java.util.List;
import java.util.Optional;

public interface MachineRepo extends JpaRepository<Machine, Long>, JpaSpecificationExecutor<Machine> {

    Optional<Machine> findByIdAndUserId(Long id, Long userId);

    List<Machine> findByUserIdAndActive(Long userId, boolean active);
}
