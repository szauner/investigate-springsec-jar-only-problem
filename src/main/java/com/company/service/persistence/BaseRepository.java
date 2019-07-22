package com.company.service.persistence;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

// Ensure that Spring Data JPA does not try to create an implementation for this base repository interface.
@NoRepositoryBean
interface BaseRepository<T, ID extends Serializable> extends Repository<T, ID> {

    public void delete(T entity);

    public Optional<T> findById(ID id);

    public T save(T entity);
}