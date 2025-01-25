package telran.mediation;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlkQueueImpl<T> implements BlkQueue<T> {
    private LinkedList<T> messages;
    private final int FIFO_LENGTH;
    private Lock mutex = new ReentrantLock();
    private Condition consumerPopWaitCondition = mutex.newCondition();
    private Condition producerPushWaitCondition = mutex.newCondition();

    public BlkQueueImpl(int maxSize) {
        this.FIFO_LENGTH = maxSize;
        this.messages = new LinkedList<>();
//        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void push(T message) {
        mutex.lock();
        try {
            while (this.messages.size() == FIFO_LENGTH) {
                try {
                    producerPushWaitCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.messages.addLast(message);
            consumerPopWaitCondition.signal();
        } finally {
            mutex.unlock();
        }

    }

    @Override
    public T pop() {
        mutex.lock();
        try {
//            while (this.messages.size() == 0) {
            while (this.messages.isEmpty()) {
                try {
                    consumerPopWaitCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T res = this.messages.pollFirst();
            producerPushWaitCondition.signal();
            return res;
        } finally {
            mutex.unlock();
        }

    }
}
