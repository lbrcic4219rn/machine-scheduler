package raf.rs.machinescheduler.services;

import raf.rs.machinescheduler.model.User;

import java.util.List;
import java.util.Optional;

public interface IService {
    void save(User user);

    List<User> findAll();

    Optional<User> findById(Long id);

    void delete(Long id);

    Optional<User> findByEmail(String email);
}
