package com.uas.translations;

import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

/**
 * Created by UAS on 09.01.2016.
 */
public class EventBusProvider {

    private static class SingletonHolder {
        public static final EventBusProvider INSTANCE = new EventBusProvider();
    }

    public static EventBus getInstance()  {
        return SingletonHolder.INSTANCE.mEventBus;
    }


    private EventBus mEventBus;

    private EventBusProvider() {
        mEventBus = EventBus.builder()
                .eventInheritance(false)
                .executorService(Executors.newSingleThreadExecutor())
                .logNoSubscriberMessages(false)
                .sendNoSubscriberEvent(false)
                .build();
    }

}