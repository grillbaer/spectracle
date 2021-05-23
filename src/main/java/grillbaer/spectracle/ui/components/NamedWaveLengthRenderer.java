package grillbaer.spectracle.ui.components;

import grillbaer.spectracle.spectrum.NamedWaveLength;
import grillbaer.spectracle.spectrum.Viewing;
import lombok.AllArgsConstructor;

import javax.swing.*;
import java.awt.*;

@AllArgsConstructor
public class NamedWaveLengthRenderer extends DefaultListCellRenderer {

    private boolean withName;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        final var label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof NamedWaveLength namedWaveLength) {
            label.setText(namedWaveLength.getWaveLengthNameString(false));
            label.setIcon(new ColorIcon(Viewing.colorForWaveLength((float) namedWaveLength.getNanoMeters())));
        }

        return label;
    }
}
