package com.github.kaivu.domain.repositories.ext;

import com.github.kaivu.core.entities.EntityDevice;
import com.github.kaivu.domain.repositories.IEntityDeviceRepository;
import com.github.kaivu.infrastructure.common.PageableRequest;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 12/13/24
 * Time: 1:07 PM
 */
@ApplicationScoped
public class EntityDeviceRepository implements IEntityDeviceRepository {

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
        return sessionFactory.withTransaction(
                (session, tx) -> session.persist(entity).replaceWith(entity));
    }

    @Override
    public Uni<List<EntityDevice>> persist(List<EntityDevice> entities) {
        return sessionFactory.withTransaction(
                (session, tx) -> session.mergeAll(entities.toArray()).replaceWith(entities));
    }

    @Override
    public Uni<EntityDevice> update(EntityDevice entity) {
        return sessionFactory.withTransaction(
                (session, tx) -> session.merge(entity).replaceWith(entity));
    }

    @Override
    public Uni<List<EntityDevice>> update(List<EntityDevice> entities) {
        return sessionFactory.withTransaction(
                (session, tx) -> session.mergeAll(entities.toArray()).replaceWith(entities));
    }

    @Override
    public Uni<Void> delete(EntityDevice entity) {

        return sessionFactory.withTransaction(
                (session, tx) -> session.remove(entity).replaceWithVoid());
    }

    @Override
    public Uni<List<EntityDevice>> findAll(PageableRequest pageable) {

        String selectQuery = "SELECT ed FROM EntityDevice ed WHERE 1=1 ";
        StringBuilder filtersQuery = new StringBuilder();
        Optional.ofNullable(pageable.getKeyword())
                .ifPresent(keyword -> filtersQuery.append("AND ed.name LIKE :keyword "));
        return sessionFactory.withTransaction((session, tx) -> {
            Mutiny.SelectionQuery<EntityDevice> sessionQuery =
                    session.createQuery(selectQuery + filtersQuery, EntityDevice.class);
            Optional.ofNullable(pageable.getKeyword())
                    .ifPresent(keyword -> sessionQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%"));
            return sessionQuery
                    .setFirstResult(pageable.getOffset())
                    .setMaxResults(pageable.getSize())
                    .setOrder(List.copyOf(pageable.toOrders("ed", EntityDevice.class)))
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
