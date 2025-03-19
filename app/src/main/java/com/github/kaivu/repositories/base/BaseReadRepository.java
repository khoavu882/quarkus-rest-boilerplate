package com.github.kaivu.repositories.base;

import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 12/10/24
 * Time: 2:55 PM
 */
public interface BaseReadRepository<E, I> {

    Uni<Optional<E>> findById(I identity);

    Uni<List<E>> findByIds(List<I> identities);
}
