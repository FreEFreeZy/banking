package org.example.banksystem.service;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceInterface<REPO extends JpaRepository<ENTITY, ID>, ENTITY, ID> {

    REPO getRepo();

    default ENTITY get(ID id) {
        return getRepo().getReferenceById(id);
    }

    default List<ENTITY> getAll() {
        return getRepo().findAll();
    }

    default void save(ENTITY entity) {
        getRepo().save(entity);
    }

    default void delete(ID id) {
        getRepo().deleteById(id);
    }

    default boolean exists(ID id) {
        return getRepo().existsById(id);
    }
}
