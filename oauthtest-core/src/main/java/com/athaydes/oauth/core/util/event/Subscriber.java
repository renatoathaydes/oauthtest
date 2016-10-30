package com.athaydes.oauth.core.util.event;

@FunctionalInterface
public interface Subscriber<E extends Event> {

    void handle( E event );

}
