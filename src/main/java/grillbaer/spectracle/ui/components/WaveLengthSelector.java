package grillbaer.spectracle.ui.components;

import grillbaer.spectracle.model.Observers;
import grillbaer.spectracle.spectrum.KnownSpectrums;
import grillbaer.spectracle.spectrum.NamedWaveLength;
import grillbaer.spectracle.spectrum.Formatting;
import lombok.Getter;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.regex.Pattern;

public class WaveLengthSelector {
    private final JPanel panel;
    private final JComboBox<NamedWaveLength> comboBox;
    private static final Pattern WAVE_LENGTH_PATTERN = Pattern.compile("\\s*[0-9]+([.,][0-9]*)?\\s*");

    @Getter
    private final Observers<NamedWaveLength> changeObservers = new Observers<>();

    public WaveLengthSelector(String labelText, Color labelColor) {
        this.panel = new JPanel(new FlowLayout());

        this.comboBox = new JComboBox<>(KnownSpectrums.getCommonWaveLengths().toArray(new NamedWaveLength[0]));
        this.comboBox.setEditable(true);
        this.comboBox.setRenderer(new NamedWaveLengthRenderer(true));
        this.comboBox.addActionListener(e -> onChange());
        this.comboBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                onChange();
            }
        });
        ((JTextComponent) this.comboBox.getEditor().getEditorComponent()).getDocument()
                .addUndoableEditListener(es -> onChange());

        if (labelText != null) {
            final var label = new JLabel(labelText);
            if (labelColor != null) {
                label.setForeground(labelColor);
            }
            label.setLabelFor(this.comboBox);
            this.panel.add(label);
        }

        this.panel.add(comboBox);

        final var unitLabel = new JLabel("nm");
        this.panel.add(unitLabel);

        updateDisplay();
    }

    public JComponent getComponent() {
        return this.panel;
    }

    public void addActionListener(ActionListener listener) {
        this.comboBox.addActionListener(listener);
    }

    public void transferFocus() {
        this.comboBox.getEditor().getEditorComponent().transferFocus();
    }

    public NamedWaveLength getValidWaveLength() {
        final var selected = this.comboBox.getSelectedItem();
        if (selected instanceof NamedWaveLength namedWaveLength)
            return namedWaveLength;

        if (this.comboBox.getEditor().getItem() instanceof String editText) {
            final var waveLength = textToWaveLength(editText);
            if (waveLength != null)
                return new NamedWaveLength(null, textToWaveLength(editText));
        }

        return null;
    }

    public void setWaveLength(double nanoMeters) {
        NamedWaveLength matchingNamedWaveLength = null;
        for (int i = 0; i < this.comboBox.getItemCount(); i++) {
            final var candidate = this.comboBox.getItemAt(i);
            if (candidate.getNanoMeters() == nanoMeters) {
                matchingNamedWaveLength = candidate;
            }
        }
        if (matchingNamedWaveLength != null) {
            this.comboBox.setSelectedItem(matchingNamedWaveLength);
        } else {
            this.comboBox.setSelectedItem(Formatting.formatWaveLength(nanoMeters));
        }
    }

    private void onChange() {
        updateDisplay();
        this.changeObservers.fire(getValidWaveLength());
    }

    private void updateDisplay() {
        final var validWaveLength = getValidWaveLength();
        this.comboBox.setBackground(validWaveLength != null ? null : Color.RED.darker());
    }

    private Double textToWaveLength(String text) {
        if (text == null)
            return null;
        if (!WAVE_LENGTH_PATTERN.matcher(text).matches())
            return null;

        var waveLength = Double.parseDouble(text.trim().replace(',', '.'));

        if (waveLength < 200 || waveLength > 1500)
            return null;

        return waveLength;
    }
}
