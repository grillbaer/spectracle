package grillbaer.spectracle.ui;

import grillbaer.spectracle.Context;
import grillbaer.spectracle.ui.components.SpectrumView;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;

public class SpectrumPanel {
    private final Context context;

    private final JPanel panel;
    private final SpectrumView spectrumView;
    private final CalibrationPanel calibrationPanel;

    public SpectrumPanel(@NonNull Context context) {
        this.context = context;

        this.panel = new JPanel(new BorderLayout());
        this.spectrumView = new SpectrumView();

        this.calibrationPanel = new CalibrationPanel(this.context, this.spectrumView);
        this.calibrationPanel.getComponent().setVisible(true);

        final var controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(this.calibrationPanel.getComponent());

        this.panel.add(this.spectrumView, BorderLayout.CENTER);
        this.panel.add(controlPanel, BorderLayout.SOUTH);

        this.context.getModel().getSpectrumObservers()
                .add(this.spectrumView::setSpectrum);
    }

    public JComponent getComponent() {
        return this.panel;
    }
}
