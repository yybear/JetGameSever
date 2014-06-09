package com.handwin.game;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.group.ChannelGroupException;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.Iterator;

/**
 * User: roger
 * Date: 13-12-16 下午2:42
 */
public interface GameSessionFuture extends Future<Void>, Iterable<ChannelFuture> {

    /**
     * Returns the {@link io.netty.channel.ChannelFuture} of the individual I/O operation which
     * is associated with the specified {@link io.netty.channel.Channel}.
     *
     * @return the matching {@link io.netty.channel.ChannelFuture} if found.
     *         {@code null} otherwise.
     */
    ChannelFuture find(Channel channel);

    /**
     * Returns {@code true} if and only if all I/O operations associated with
     * this future were successful without any failure.
     */
    @Override
    boolean isSuccess();

    @Override
    ChannelGroupException cause();

    /**
     * Returns {@code true} if and only if the I/O operations associated with
     * this future were partially successful with some failure.
     */
    boolean isPartialSuccess();

    /**
     * Returns {@code true} if and only if the I/O operations associated with
     * this future have failed partially with some success.
     */
    boolean isPartialFailure();

    @Override
    GameSessionFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener);


    @Override
    GameSessionFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener);


    @Override
    GameSessionFuture await() throws InterruptedException;

    @Override
    GameSessionFuture awaitUninterruptibly();

    @Override
    GameSessionFuture syncUninterruptibly();

    @Override
    GameSessionFuture sync() throws InterruptedException;

    @Override
    Iterator<ChannelFuture> iterator();
}
