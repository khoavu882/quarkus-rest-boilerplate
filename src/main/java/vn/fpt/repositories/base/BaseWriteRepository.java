package vn.fpt.repositories.base;

import io.smallrye.mutiny.Uni;

import java.util.List;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 12/10/24
 * Time: 2:53 PM
 */
public interface BaseWriteRepository<E> {

    Uni<E> persist(E entity);

    Uni<List<E>> persist(List<E> entities);

    Uni<E> update(E entity);

    Uni<List<E>> update(List<E> entities);

    Uni<Void> delete(E entity);
}
