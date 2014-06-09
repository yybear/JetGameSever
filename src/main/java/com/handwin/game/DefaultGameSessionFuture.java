package com.handwin.game;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupException;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.util.concurrent.*;

import java.util.*;

/**
 * User: roger
 * Date: 13-12-16 下午2:42
 */
public class DefaultGameSessionFuture extends DefaultPromise<Void> implements ChannelGroupFuture  {


    private final Map<Channel, ChannelFuture> futures;
    private int successCount;
    private int failureCount;

    private final ChannelFutureListener childListener = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            boolean success = future.isSuccess();
            boolean callSetDone;
            synchronized (DefaultGameSessionFuture.this) {
                if (success) {
                    successCount ++;
                } else {
                    failureCount ++;
                }

                callSetDone = successCount + failureCount == futures.size();
                assert successCount + failureCount <= futures.size();
            }

            if (callSetDone) {
                if (failureCount > 0) {
                    List<Map.Entry<Channel, Throwable>> failed =
                            new ArrayList<Map.Entry<Channel, Throwable>>(failureCount);
                    for (ChannelFuture f: futures.values()) {
                        if (!f.isSuccess()) {
                            failed.add(new DefaultEntry<Channel, Throwable>(f.channel(), f.cause()));
                        }
                    }
                    setFailure0(new ChannelGroupException(failed));
                } else {
                    setSuccess0();
                }
            }
        }
    };



    public DefaultGameSessionFuture(Map<Channel, ChannelFuture> futures, EventExecutor executor) {
        super(executor);
        this.futures = Collections.unmodifiableMap(futures);
        for (ChannelFuture f: this.futures.values()) {
            f.addListener(childListener);
        }

        // Done on arrival?
        if (this.futures.isEmpty()) {
            setSuccess0();
        }
    }

    @Override
    public ChannelGroup group() {
        return null;
    }

    @Override
    public ChannelFuture find(Channel channel) {
        return futures.get(channel);
    }

    @Override
    public Iterator<ChannelFuture> iterator() {
        return futures.values().iterator();
    }

    @Override
    public synchronized boolean isPartialSuccess() {
        return successCount != 0 && successCount != futures.size();
    }

    @Override
    public synchronized boolean isPartialFailure() {
        return failureCount != 0 && failureCount != futures.size();
    }

    @Override
    public DefaultGameSessionFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.addListener(listener);
        return this;
    }

    @Override
    public DefaultGameSessionFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        super.addListeners(listeners);
        return this;
    }

    @Override
    public DefaultGameSessionFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.removeListener(listener);
        return this;
    }

    @Override
    public DefaultGameSessionFuture removeListeners(
            GenericFutureListener<? extends Future<? super Void>>... listeners) {
        super.removeListeners(listeners);
        return this;
    }

    @Override
    public DefaultGameSessionFuture await() throws InterruptedException {
        super.await();
        return this;
    }

    @Override
    public DefaultGameSessionFuture awaitUninterruptibly() {
        super.awaitUninterruptibly();
        return this;
    }

    @Override
    public DefaultGameSessionFuture syncUninterruptibly() {
        super.syncUninterruptibly();
        return this;
    }

    @Override
    public DefaultGameSessionFuture sync() throws InterruptedException {
        super.sync();
        return this;
    }

    @Override
    public ChannelGroupException cause() {
        return (ChannelGroupException) super.cause();
    }

    private void setSuccess0() {
        super.setSuccess(null);
    }

    private void setFailure0(ChannelGroupException cause) {
        super.setFailure(cause);
    }

    @Override
    public DefaultGameSessionFuture setSuccess(Void result) {
        throw new IllegalStateException();
    }

    @Override
    public boolean trySuccess(Void result) {
        throw new IllegalStateException();
    }

    @Override
    public DefaultGameSessionFuture setFailure(Throwable cause) {
        throw new IllegalStateException();
    }

    @Override
    public boolean tryFailure(Throwable cause) {
        throw new IllegalStateException();
    }

    @Override
    protected void checkDeadLock() {
        EventExecutor e = executor();
        if (e != null && e != ImmediateEventExecutor.INSTANCE && e.inEventLoop()) {
            throw new BlockingOperationException();
        }
    }

    private static final class DefaultEntry<K, V> implements Map.Entry<K, V> {
        private final K key;
        private final V value;

        public DefaultEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("read-only");
        }
    }
}
