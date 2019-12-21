package com.github.lhotari.mockito.threadsafe.mockmaker;

import org.mockito.internal.creation.bytebuddy.ByteBuddyMockMaker;
import org.mockito.invocation.*;
import org.mockito.mock.MockCreationSettings;
import org.mockito.plugins.MockMaker;

import java.util.Objects;

/**
 * Synchronizes all mock invocations using the mock object as the lock object
 *
 * This makes using Mockito in multi-thread scenarios thread-safe as long as the mocking happens
 * under the same object monitor lock
 */
public class ThreadsafeMockMaker implements MockMaker {
    private final MockMaker delegate;

    public ThreadsafeMockMaker() {
        this.delegate = new ByteBuddyMockMaker();
    }

    @Override
    public <T> T createMock(MockCreationSettings<T> settings, MockHandler handler) {
        return delegate.createMock(settings, handler);
    }

    @Override
    public MockHandler getHandler(Object mock) {
        MockHandler handler = delegate.getHandler(mock);
        if (handler == null) {
            return null;
        }
        return new SynchronizedMockHandler(handler);
    }

    @Override
    public void resetMock(Object mock, MockHandler newHandler, MockCreationSettings settings) {
        delegate.resetMock(mock, newHandler, settings);
    }

    @Override
    public TypeMockability isTypeMockable(Class<?> type) {
        return delegate.isTypeMockable(type);
    }

    private static class SynchronizedMockHandler implements MockHandler {
        private final MockHandler handlerDelegate;

        public SynchronizedMockHandler(MockHandler handlerDelegate) {
            Objects.requireNonNull(handlerDelegate, "handler delegate must not be null");
            this.handlerDelegate = handlerDelegate;
        }

        @Override
        public Object handle(Invocation invocation) throws Throwable {
            synchronized (invocation.getMock()) {
                return handlerDelegate.handle(invocation);
            }
        }

        @Override
        public MockCreationSettings getMockSettings() {
            return handlerDelegate.getMockSettings();
        }

        @Override
        public InvocationContainer getInvocationContainer() {
            return handlerDelegate.getInvocationContainer();
        }
    }
}
