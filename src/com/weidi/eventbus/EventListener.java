package com.weidi.eventbus;

/**
 * Created by root on 17-2-4.
 */

public interface EventListener {

    void onEvent(int what, Object object);

}
