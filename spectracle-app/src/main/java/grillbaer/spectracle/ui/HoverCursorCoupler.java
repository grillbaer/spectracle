package grillbaer.spectracle.ui;

import grillbaer.spectracle.model.Model;
import grillbaer.spectracle.ui.components.SpectralXView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class HoverCursorCoupler {
    private final @NonNull Model model;
    private final @NonNull SpectralXView view;

    public void start() {
        this.view.getHoverCursorWaveLengthObservers().add(wl -> viewToModel());
        this.model.getHoverCursorWaveLengthObservers().add(wl -> modelToView());
        modelToView();
    }

    private void modelToView() {
        this.view.setHoverCursorWaveLength(this.model.getHoverCursorWaveLength());
    }

    private void viewToModel() {
        this.model.setHoverCursorWaveLength(this.view.getHoverCursorWaveLength());
    }
}
