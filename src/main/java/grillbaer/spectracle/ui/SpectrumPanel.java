package grillbaer.spectracle.ui;

import grillbaer.spectracle.Context;
import grillbaer.spectracle.spectrum.NamedWaveLength;
import grillbaer.spectracle.spectrum.WaveLengths;
import grillbaer.spectracle.ui.components.Cursor;
import grillbaer.spectracle.ui.components.SpectrumView;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class SpectrumPanel {
    private final Context context;

    private final JPanel panel;
    private final SpectrumView spectrumView;
    private final CalibrationPanel calibrationPanel;
    private final List<Cursor> commonWaveLengthCursors;

    public SpectrumPanel(@NonNull Context context) {
        this.context = context;

        this.panel = new JPanel(new BorderLayout());
        this.spectrumView = new SpectrumView();

        final var commonWaveLengthsButton = new JToggleButton("Common Wavelengths");
        commonWaveLengthsButton.addActionListener(e -> showKnownWaveLengths(commonWaveLengthsButton.isSelected()));

        this.calibrationPanel = new CalibrationPanel(this.context, this.spectrumView);
        this.calibrationPanel.getComponent().setVisible(true);

        final var controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(this.calibrationPanel.getComponent());
        controlPanel.add(commonWaveLengthsButton);

        this.panel.add(this.spectrumView, BorderLayout.CENTER);
        this.panel.add(controlPanel, BorderLayout.SOUTH);

        this.context.getModel().getSpectrumObservers()
                .add(this.spectrumView::setSpectrum);

        this.commonWaveLengthCursors = NamedWaveLength.COMMON_WAVELENGTHS.stream()
                .map(wl -> new Cursor("common_wl_" + wl.getName(), wl.getNanoMeters(),
                        () -> WaveLengths.format(wl.getNanoMeters()) + " " + wl.getName(),
                        new Color(180, 180, 180, 128))).collect(Collectors.toList());
    }

    public JComponent getComponent() {
        return this.panel;
    }

    private void showKnownWaveLengths(boolean show) {
        for (Cursor cursor : this.commonWaveLengthCursors) {
            if (show) {
                this.spectrumView.putXCursor(cursor);
            } else {
                this.spectrumView.removeXCursor(cursor.getId());
            }
        }
    }
}
