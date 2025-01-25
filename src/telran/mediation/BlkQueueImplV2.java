package telran.mediation;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlkQueueImplV2<T> implements BlkQueue<T>{
    private final T[] buffer;
    private int head = 0, tail = 0, size = 0;
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    @SuppressWarnings("unchecked")
    public BlkQueueImplV2 (int capacity) {
        buffer = (T[]) new Object[capacity];
    }

    @Override
    public void push(T message) {
        lock.lock();
        try {
            while (size == buffer.length) {
                notFull.await();
            }
            buffer[tail] = message;
            tail = (tail + 1) % buffer.length;
            size++;
            notEmpty.signal();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T pop() {
        lock.lock();
        try {
            while (size == 0) {
                notEmpty.await();
            }
            T message = buffer[head];
            head = (head + 1) % buffer.length;
            size--;
            notFull.signal();
            return message;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            lock.unlock();
        }
    }
}
