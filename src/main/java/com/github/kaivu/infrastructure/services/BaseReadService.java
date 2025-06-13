package com.github.kaivu.infrastructure.services;

import com.github.kaivu.infrastructure.errors.exceptions.EntityNotFoundException;
import io.smallrye.mutiny.Uni;

import java.util.Optional;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 12/10/24
 * Time: 2:38 PM
 */
public interface BaseReadService<E, I> {

    /**
     * +  * Finds an entity by its unique identifier.
     * +  *
     * +  * This method retrieves a single entity from the data source based on the provided unique identifier.
     * +  *
     * +  * @param id The unique identifier of the entity to be retrieved.
     * +  * @return A {@link Uni<Optional<E>>} representing the asynchronous operation to retrieve the entity. The returned {@link Optional} will contain the entity if found, or an empty {@link Optional} if the entity is not found.
     * +  */
    Uni<Optional<E>> findById(I id);

    /**
     * +  * Retrieves an entity by its identifier.
     * +  *
     * +  * @param identify The identifier of the entity to be retrieved.
     * +  * @return A {@link Uni<E>} representing the asynchronous operation to retrieve the entity.
     * +  * @throws EntityNotFoundException if the entity is not found.
     * +  */
    Uni<E> getById(I identify) throws EntityNotFoundException;
}
