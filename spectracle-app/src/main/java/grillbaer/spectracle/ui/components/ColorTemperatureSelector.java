package grillbaer.spectracle.ui.components;

import grillbaer.spectracle.model.Observers;
import lombok.Getter;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.regex.Pattern;

public class ColorTemperatureSelector {
    private final JPanel panel;
    private final JComboBox<String> comboBox;
    private static final Pattern KELVIN_NAME_PATTERN = Pattern.compile("\\s*([0-9]{3,4})\\s*(K.*)?");

    @Getter
    private final Observers<Integer> changeObservers = new Observers<>();

    public ColorTemperatureSelector(String labelText) {
        this.panel = new JPanel(new FlowLayout());

        this.comboBox = new JComboBox<>(new String[]{"2700 K Incandescent Lamp", "3000 K Halogen Lamp"});
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
            label.setLabelFor(this.comboBox);
            this.panel.add(label);
        }

        this.panel.add(comboBox);

        final var unitLabel = new JLabel("K");
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

    public Integer getValidKelvin() {
        final var selected = this.comboBox.getSelectedItem();
        if (selected instanceof String namedTemperature)
            return textToKelvin(namedTemperature);

        if (this.comboBox.getEditor().getItem() instanceof String editText) {
            return textToKelvin(editText);
        }

        return null;
    }

    public void setTemperature(int kelvin) {
        String matchingNamedTemp = null;
        for (int i = 0; i < this.comboBox.getItemCount(); i++) {
            final var candidate = this.comboBox.getItemAt(i);
            if (candidate.startsWith(kelvin + " ")) {
                matchingNamedTemp = candidate;
            }
        }
        if (matchingNamedTemp != null) {
            this.comboBox.setSelectedItem(matchingNamedTemp);
        } else {
            this.comboBox.setSelectedItem(String.valueOf(kelvin));
        }
    }

    private void onChange() {
        updateDisplay();
        this.changeObservers.fire(getValidKelvin());
    }

    private void updateDisplay() {
        final var validKelvin = getValidKelvin();
        this.comboBox.setBackground(validKelvin != null ? null : Color.RED.darker());
    }

    private Integer textToKelvin(String text) {
        if (text == null)
            return null;
        final var matcher = KELVIN_NAME_PATTERN.matcher(text);
        if (!matcher.matches())
            return null;

        return Integer.parseInt(matcher.group(1));
    }
}
