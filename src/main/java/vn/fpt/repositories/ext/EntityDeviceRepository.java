package vn.fpt.repositories.ext;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import vn.fpt.models.EntityDevice;
import vn.fpt.repositories.IEntityDeviceRepository;

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
}
