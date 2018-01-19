package com.weidi.eventbus;

import java.util.ArrayList;

/**
 * Created by root on 17-2-4.
 */

public class EventBus {

    private static EventBus mEventBus;

    private ArrayList<EventListener> mEventListenerList = new ArrayList<EventListener>();

    private EventBus() {
    }

    public static EventBus getDefault() {
        if (mEventBus == null) {
            synchronized (EventBus.class) {
                if (mEventBus == null) {
                    mEventBus = new EventBus();
                }
            }
        }
        return mEventBus;
    }

    public void register(EventListener listener) {
        if (mEventListenerList != null && !mEventListenerList.contains(listener)) {
            mEventListenerList.add(listener);
        }
    }

    public void unregister(EventListener listener) {
        if (mEventListenerList != null && mEventListenerList.contains(listener)) {
            mEventListenerList.remove(listener);
        }
    }

    public synchronized void post(int what, Object object) {
        if (mEventListenerList != null && !mEventListenerList.isEmpty()) {
            int count = mEventListenerList.size();
            for (int i = 0; i < count; i++) {
                EventListener listener = mEventListenerList.get(i);
                listener.onEvent(what, object);
            }
        }
    }

}
