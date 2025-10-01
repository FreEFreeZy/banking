package org.example.banksystem.repository;

import org.example.banksystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью User в базе данных
 * <p>
 * Предоставляет методы для выполнения операций с пользователями системы.
 * Наследует стандартные CRUD операции от JpaRepository.
 * Использует имя пользователя (username) в качестве первичного ключа.
 * </p>
 *
 * @author George
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Находит пользователя по имени пользователя
     *
     * @param username имя пользователя для поиска
     * @return Optional с найденным пользователем или empty если пользователь не найден
     */
    Optional<User> findByUsername(String username);
}