package com.github.kaivu.infrastructure.services;

import com.github.kaivu.infrastructure.errors.exceptions.EntityNotFoundException;
import io.smallrye.mutiny.Uni;

import java.util.List;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 12/10/24
 * Time: 2:38 PM
 */
public interface BaseWriteService<E, I> {

    /**
     * Persists a single entity to the data store.
     *
     * @param entity The entity to be persisted.
     * @return A {@link Uni} containing the persisted entity.
     */
    Uni<E> persist(E entity);

    /**
     * Persists a list of entities to the data store.
     *
     * @param entities The list of entities to be persisted.
     * @return A {@link Uni} containing the list of persisted entities.
     */
    Uni<List<E>> persist(List<E> entities);

    /**
     * Updates an existing entity in the data store.
     *
     * @param entity The entity to be updated.
     * @return A {@link Uni} containing the updated entity.
     * @throws EntityNotFoundException If the entity to be updated is not found.
     */
    Uni<E> update(E entity) throws EntityNotFoundException;

    /**
     * Updates a list of existing entities in the data store.
     *
     * @param entities The list of entities to be updated.
     * @return A {@link Uni} containing the list of updated entities.
     * @throws EntityNotFoundException If any of the entities to be updated are not found.
     */
    Uni<List<E>> update(List<E> entities) throws EntityNotFoundException;

    /**
     * Deletes an entity from the data store based on its identifier.
     *
     * @param identify The identifier of the entity to be deleted.
     * @return A {@link Uni<Void>} representing the completion of the delete operation.
     * @throws EntityNotFoundException If the entity to be deleted is not found.
     */
    Uni<Void> delete(I identify) throws EntityNotFoundException;
}
