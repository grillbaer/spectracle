package grillbaer.spectracle.model;

public interface Observer<E> {
    void changed(E event);
}
