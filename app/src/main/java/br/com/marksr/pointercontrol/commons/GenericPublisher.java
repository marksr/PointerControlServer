package br.com.marksr.pointercontrol.commons;

import java.util.ArrayList;

public abstract class GenericPublisher<Listener, ObjectData> {
    private ArrayList<Listener> listeners = new ArrayList<>();

    public void setOnEvent(Listener listener) {
        if (null != listener && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeOnEvent(Listener listener) {
        listeners.remove(listener);
    }

    public void fire(ObjectData d) {
        for (Listener l: listeners) {
            action(l, d);
        }
    }

    public abstract void action(Listener l, ObjectData d);

    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    public void clear() {
        listeners.clear();
    }
    
    //Inner Interfaces
    
    public interface GenericPublisherInterface<Listener> {
        void setOnEvent(Listener listener);

        void removeOnEvent(Listener listener);
    }
}
