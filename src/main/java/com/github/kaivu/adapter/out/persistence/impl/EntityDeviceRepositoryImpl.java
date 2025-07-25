package com.github.kaivu.adapter.out.persistence.impl;

import com.github.kaivu.adapter.in.rest.dto.request.PageableRequest;
import com.github.kaivu.adapter.out.persistence.EntityDeviceRepository;
import com.github.kaivu.domain.EntityDevice;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of EntityDeviceRepository using Hibernate Reactive
 */
@Slf4j
@ApplicationScoped
public class EntityDeviceRepositoryImpl implements EntityDeviceRepository {

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @Override
    public Uni<Optional<EntityDevice>> findById(UUID identity) {
        return sessionFactory.withTransaction(
                (session, tx) -> session.find(EntityDevice.class, identity).map(Optional::ofNullable));
    }

    @Override
    public Uni<List<EntityDevice>> findByIds(List<UUID> identities) {
        return sessionFactory.withTransaction((session, tx) -> session.createQuery(
                        "FROM EntityDevice ed WHERE ed.id IN :identities", EntityDevice.class)
                .setParameter("identities", identities)
                .getResultList());
    }

    @Override
    public Uni<EntityDevice> persist(EntityDevice entity) {
        return sessionFactory.withTransaction((session, tx) ->
                session.persist(entity).chain(session::flush).replaceWith(entity));
    }

    @Override
    public Uni<List<EntityDevice>> persist(List<EntityDevice> entities) {
        return sessionFactory.withTransaction((session, tx) -> {
            Uni<Void> chain = Uni.createFrom().voidItem();
            for (EntityDevice entity : entities) {
                chain = chain.chain(() -> session.persist(entity));
            }
            return chain.chain(session::flush).replaceWith(entities);
        });
    }

    @Override
    public Uni<EntityDevice> update(EntityDevice entity) {
        return sessionFactory.withTransaction((session, tx) ->
                session.merge(entity).chain(updated -> session.flush().replaceWith(updated)));
    }

    @Override
    public Uni<List<EntityDevice>> update(List<EntityDevice> entities) {
        return sessionFactory.withTransaction((session, tx) -> {
            Uni<Void> chain = Uni.createFrom().voidItem();
            for (EntityDevice entity : entities) {
                chain = chain.chain(() -> session.merge(entity).replaceWithVoid());
            }
            return chain.chain(session::flush).replaceWith(entities);
        });
    }

    @Override
    public Uni<Void> delete(EntityDevice entity) {
        return sessionFactory.withTransaction((session, tx) -> {
            // First merge the entity to ensure it's managed, then remove it
            return session.merge(entity)
                    .chain(session::remove)
                    .chain(session::flush)
                    .replaceWithVoid();
        });
    }

    @Override
    public Uni<List<EntityDevice>> findAll(PageableRequest pageable) {
        String selectQuery = "SELECT ed FROM EntityDevice ed WHERE 1=1 ";
        StringBuilder filtersQuery = new StringBuilder();
        Optional.ofNullable(pageable.getKeyword())
                .ifPresent(keyword -> filtersQuery.append("AND ed.name LIKE :keyword "));

        // Build dynamic ORDER BY clause using query alias
        String orderByClause = " ORDER BY " + pageable.buildOrderByClause("ed", "createdDate");

        return sessionFactory.withTransaction((session, tx) -> {
            Mutiny.SelectionQuery<EntityDevice> sessionQuery =
                    session.createQuery(selectQuery + filtersQuery + orderByClause, EntityDevice.class);
            Optional.ofNullable(pageable.getKeyword())
                    .ifPresent(keyword -> sessionQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%"));

            // Apply pagination
            return sessionQuery
                    .setFirstResult(pageable.getOffset())
                    .setMaxResults(pageable.getSize())
                    .getResultList();
        });
    }

    @Override
    public Uni<Long> countAll(PageableRequest pageable) {
        String countQuery = "SELECT COUNT(ed) FROM EntityDevice ed WHERE 1=1 ";
        StringBuilder filtersQuery = new StringBuilder();
        Optional.ofNullable(pageable.getKeyword())
                .ifPresent(keyword -> filtersQuery.append("AND ed.name LIKE :keyword "));

        return sessionFactory.withTransaction((session, tx) -> {
            Mutiny.SelectionQuery<Long> sessionCount = session.createQuery(countQuery + filtersQuery, Long.class);
            Optional.ofNullable(pageable.getKeyword())
                    .ifPresent(keyword -> sessionCount.setParameter("keyword", "%" + keyword.toLowerCase() + "%"));
            return sessionCount.getSingleResult();
        });
    }
}
