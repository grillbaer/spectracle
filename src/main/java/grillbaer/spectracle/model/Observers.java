package grillbaer.spectracle.model;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Observers<E> {
    private static final Logger LOG = LoggerFactory.getLogger(Observers.class);

    private final List<Observer<E>> observers = new CopyOnWriteArrayList<>();

    public synchronized void add(@NonNull Observer<E> observer) {
        this.observers.add(observer);
    }

    public synchronized void remove(@NonNull Observer<E> observer) {
        this.observers.remove(observer);
    }

    public synchronized void fire(E event) {
        for (Observer<E> observer : this.observers) {
            try {
                observer.changed(event);
            } catch (Exception e) {
                LOG.error("Exception from observer {} of class {}", observer, observer.getClass().getName(), e);
            }
        }
    }
}
