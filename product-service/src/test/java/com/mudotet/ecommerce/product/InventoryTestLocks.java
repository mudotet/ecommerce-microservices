package com.mudotet.ecommerce.product;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Keep ownership and contention real without requiring Redis for automated tests.
final class InventoryTestLocks {
  static RedissonClient client() {
    var client = mock(RedissonClient.class);
    var locks = new ConcurrentHashMap<String, RLock>();
    when(client.getLock(anyString())).thenAnswer(call ->
        locks.computeIfAbsent(call.getArgument(0), key -> lock()));
    return client;
  }

  private static RLock lock() {
    var mutex = new ReentrantLock();
    var lock = mock(RLock.class);
    try {
      when(lock.tryLock(anyLong(), any(TimeUnit.class))).thenAnswer(call ->
          mutex.tryLock(call.getArgument(0), call.getArgument(1)));
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
    when(lock.isHeldByCurrentThread()).thenAnswer(call -> mutex.isHeldByCurrentThread());
    doAnswer(call -> { mutex.unlock(); return null; }).when(lock).unlock();
    return lock;
  }
}
