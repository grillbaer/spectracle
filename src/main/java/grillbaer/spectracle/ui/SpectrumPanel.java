package grillbaer.spectracle.ui;

import grillbaer.spectracle.Context;
import grillbaer.spectracle.spectrum.KnownSpectrums;
import grillbaer.spectracle.spectrum.Viewing;
import grillbaer.spectracle.ui.components.Cursor;
import grillbaer.spectracle.ui.components.SpectrumGraphView;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class SpectrumPanel {
    private final Context context;

    private final JPanel panel;
    private final SpectrumGraphView spectrumGraphView;
    private final WaveLengthCalibrationPanel waveLengthCalibrationPanel;
    private final SensitivityCalibrationPanel sensitivityCalibrationPanel;
    private final List<Cursor> commonWaveLengthCursors;

    public SpectrumPanel(@NonNull Context context) {
        this.context = context;

        this.panel = new JPanel(new BorderLayout());
        this.spectrumGraphView = new SpectrumGraphView();

        final var commonWaveLengthsButton = new JToggleButton("Common Wavelengths");
        commonWaveLengthsButton.addActionListener(e -> showKnownWaveLengths(commonWaveLengthsButton.isSelected()));

        this.waveLengthCalibrationPanel = new WaveLengthCalibrationPanel(this.context, this.spectrumGraphView);
        this.sensitivityCalibrationPanel = new SensitivityCalibrationPanel(this.context, this.spectrumGraphView);

        final var controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(this.waveLengthCalibrationPanel.getComponent());
        controlPanel.add(this.sensitivityCalibrationPanel.getComponent());
        controlPanel.add(commonWaveLengthsButton);

        this.panel.add(this.spectrumGraphView, BorderLayout.CENTER);
        this.panel.add(controlPanel, BorderLayout.SOUTH);

        this.context.getModel().getSpectrumObservers()
                .add(this.spectrumGraphView::setSpectrum);

        this.commonWaveLengthCursors = KnownSpectrums.COMMON_WAVELENGTHS.stream()
                .map(wl -> new Cursor("common_wl_" + wl.getName(), wl.getNanoMeters(),
                        () -> Viewing.formatWaveLength(wl.getNanoMeters()) + " " + wl.getName(),
                        new Color(180, 180, 180, 128))).collect(Collectors.toList());
    }

    public JComponent getComponent() {
        return this.panel;
    }

    private void showKnownWaveLengths(boolean show) {
        for (Cursor cursor : this.commonWaveLengthCursors) {
            if (show) {
                this.spectrumGraphView.putXCursor(cursor);
            } else {
                this.spectrumGraphView.removeXCursor(cursor.getId());
            }
        }
    }
}
