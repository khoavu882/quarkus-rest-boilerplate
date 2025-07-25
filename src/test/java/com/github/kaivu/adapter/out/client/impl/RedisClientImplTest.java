package com.github.kaivu.adapter.out.client.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.github.kaivu.adapter.out.client.RedisClient;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;

/**
 * Test class for RedisClientImpl
 */
@QuarkusTest
class RedisClientImplTest {

    @Mock
    private ReactiveRedisDataSource reactiveDataSource;

    @Mock
    private ReactiveValueCommands<String, String> stringCommands;

    @Mock
    private ReactiveValueCommands<String, Integer> integerCommands;

    private RedisClientImpl redisClient;

    private final String testKey = "test-key";
    private final String testValue = "test-value";
    private final Integer testIntValue = 42;
    private final long ttlSeconds = 3600L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        redisClient = new RedisClientImpl(reactiveDataSource);
    }

    @Test
    void get_ShouldReturnValue_WhenKeyExists() {
        // Given
        when(reactiveDataSource.value(String.class)).thenReturn(stringCommands);
        when(stringCommands.get(testKey)).thenReturn(Uni.createFrom().item(testValue));

        // When & Then
        redisClient
                .get(testKey, String.class)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofSeconds(1))
                .assertItem(testValue);

        verify(reactiveDataSource).value(String.class);
        verify(stringCommands).get(testKey);
    }

    @Test
    void get_ShouldReturnNull_WhenKeyNotExists() {
        // Given
        when(reactiveDataSource.value(String.class)).thenReturn(stringCommands);
        when(stringCommands.get(testKey)).thenReturn(Uni.createFrom().nullItem());

        // When & Then
        redisClient
                .get(testKey, String.class)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofSeconds(1))
                .assertItem((String) null);

        verify(reactiveDataSource).value(String.class);
        verify(stringCommands).get(testKey);
    }

    @Test
    void get_ShouldWorkWithDifferentTypes() {
        // Given
        when(reactiveDataSource.value(Integer.class)).thenReturn(integerCommands);
        when(integerCommands.get(testKey)).thenReturn(Uni.createFrom().item(testIntValue));

        // When & Then
        redisClient
                .get(testKey, Integer.class)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofSeconds(1))
                .assertItem(testIntValue);

        verify(reactiveDataSource).value(Integer.class);
        verify(integerCommands).get(testKey);
    }

    @Test
    void set_ShouldCacheValue_WithTTL() {
        // Given
        when(reactiveDataSource.value(String.class)).thenReturn(stringCommands);
        when(stringCommands.setex(testKey, ttlSeconds, testValue))
                .thenReturn(Uni.createFrom().voidItem());

        // When & Then
        redisClient
                .set(testKey, testValue, ttlSeconds)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofSeconds(1))
                .assertCompleted();

        verify(reactiveDataSource).value(String.class);
        verify(stringCommands).setex(testKey, ttlSeconds, testValue);
    }

    @Test
    void set_ShouldWorkWithDifferentTypes() {
        // Given
        when(reactiveDataSource.value(Integer.class)).thenReturn(integerCommands);
        when(integerCommands.setex(testKey, ttlSeconds, testIntValue))
                .thenReturn(Uni.createFrom().voidItem());

        // When & Then
        redisClient
                .set(testKey, testIntValue, ttlSeconds)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofSeconds(1))
                .assertCompleted();

        verify(reactiveDataSource).value(Integer.class);
        verify(integerCommands).setex(testKey, ttlSeconds, testIntValue);
    }

    @Test
    void delete_ShouldRemoveKey_WhenKeyExists() {
        // Given
        when(reactiveDataSource.value(String.class)).thenReturn(stringCommands);
        when(stringCommands.getdel(testKey)).thenReturn(Uni.createFrom().item(testValue));

        // When & Then
        redisClient
                .delete(testKey)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofSeconds(1))
                .assertCompleted();

        verify(reactiveDataSource).value(String.class);
        verify(stringCommands).getdel(testKey);
    }

    @Test
    void delete_ShouldComplete_WhenKeyNotExists() {
        // Given
        when(reactiveDataSource.value(String.class)).thenReturn(stringCommands);
        when(stringCommands.getdel(testKey)).thenReturn(Uni.createFrom().nullItem());

        // When & Then
        redisClient
                .delete(testKey)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofSeconds(1))
                .assertCompleted();

        verify(reactiveDataSource).value(String.class);
        verify(stringCommands).getdel(testKey);
    }

    @Test
    void exists_ShouldReturnTrue_WhenKeyExists() {
        // Given
        when(reactiveDataSource.value(String.class)).thenReturn(stringCommands);
        when(stringCommands.get(testKey)).thenReturn(Uni.createFrom().item(testValue));

        // When & Then
        redisClient
                .exists(testKey)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofSeconds(1))
                .assertItem(true);

        verify(reactiveDataSource).value(String.class);
        verify(stringCommands).get(testKey);
    }

    @Test
    void exists_ShouldReturnFalse_WhenKeyNotExists() {
        // Given
        when(reactiveDataSource.value(String.class)).thenReturn(stringCommands);
        when(stringCommands.get(testKey)).thenReturn(Uni.createFrom().nullItem());

        // When & Then
        redisClient
                .exists(testKey)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofSeconds(1))
                .assertItem(false);

        verify(reactiveDataSource).value(String.class);
        verify(stringCommands).get(testKey);
    }

    @Test
    void constructor_ShouldCreateInstance_WithReactiveDataSource() {
        // Given & When
        RedisClientImpl client = new RedisClientImpl(reactiveDataSource);

        // Then
        assertNotNull(client);
        assertTrue(client instanceof RedisClient);
    }

    @Test
    void get_ShouldHandleError_WhenRedisOperationFails() {
        // Given
        RuntimeException redisError = new RuntimeException("Redis connection error");
        when(reactiveDataSource.value(String.class)).thenReturn(stringCommands);
        when(stringCommands.get(testKey)).thenReturn(Uni.createFrom().failure(redisError));

        // When & Then
        redisClient
                .get(testKey, String.class)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitFailure(Duration.ofSeconds(1))
                .assertFailedWith(RuntimeException.class, "Redis connection error");

        verify(reactiveDataSource).value(String.class);
        verify(stringCommands).get(testKey);
    }

    @Test
    void set_ShouldHandleError_WhenRedisOperationFails() {
        // Given
        RuntimeException redisError = new RuntimeException("Redis write error");
        when(reactiveDataSource.value(String.class)).thenReturn(stringCommands);
        when(stringCommands.setex(testKey, ttlSeconds, testValue))
                .thenReturn(Uni.createFrom().failure(redisError));

        // When & Then
        redisClient
                .set(testKey, testValue, ttlSeconds)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitFailure(Duration.ofSeconds(1))
                .assertFailedWith(RuntimeException.class, "Redis write error");

        verify(reactiveDataSource).value(String.class);
        verify(stringCommands).setex(testKey, ttlSeconds, testValue);
    }

    @Test
    void delete_ShouldHandleError_WhenRedisOperationFails() {
        // Given
        RuntimeException redisError = new RuntimeException("Redis delete error");
        when(reactiveDataSource.value(String.class)).thenReturn(stringCommands);
        when(stringCommands.getdel(testKey)).thenReturn(Uni.createFrom().failure(redisError));

        // When & Then
        redisClient
                .delete(testKey)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitFailure(Duration.ofSeconds(1))
                .assertFailedWith(RuntimeException.class, "Redis delete error");

        verify(reactiveDataSource).value(String.class);
        verify(stringCommands).getdel(testKey);
    }

    @Test
    void exists_ShouldHandleError_WhenRedisOperationFails() {
        // Given
        RuntimeException redisError = new RuntimeException("Redis exists error");
        when(reactiveDataSource.value(String.class)).thenReturn(stringCommands);
        when(stringCommands.get(testKey)).thenReturn(Uni.createFrom().failure(redisError));

        // When & Then
        redisClient
                .exists(testKey)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitFailure(Duration.ofSeconds(1))
                .assertFailedWith(RuntimeException.class, "Redis exists error");

        verify(reactiveDataSource).value(String.class);
        verify(stringCommands).get(testKey);
    }
}
