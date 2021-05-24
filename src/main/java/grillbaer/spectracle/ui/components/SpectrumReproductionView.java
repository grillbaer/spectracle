package grillbaer.spectracle.ui.components;

import grillbaer.spectracle.spectrum.Spectrum;
import grillbaer.spectracle.spectrum.Formatting;

import java.awt.*;

/**
 * Spectrum visualization as line view.
 */
public class SpectrumReproductionView extends SpectralXView {
    private Spectrum spectrum;

    public SpectrumReproductionView() {
        super(0);
    }

    public void setSpectrum(Spectrum spectrum) {
        this.spectrum = spectrum;
        setCalibration(spectrum.getCalibration());
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (this.spectrum != null) {
            return new Dimension(this.spectrum.getLength(), 30);
        } else {
            return new Dimension(300, 30);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(300, 10);
    }

    @Override
    protected void drawView(Graphics2D g2) {
        clearViewArea(g2, Color.BLACK);
        drawSpectralLines(g2);
        drawXGridOverlay(g2);
        drawXCursors(g2);
    }

    private void drawSpectralLines(Graphics2D g2) {
        final int len = this.spectrum.getLength();
        int lastX = (int) waveLengthToX(this.spectrum.getNanoMetersAtIndex(0));
        for (int i = 0; i < len; i++) {
            final var nanoMeters = this.spectrum.getNanoMetersAtIndex(i);
            final double value = this.spectrum.getValueAtIndex(i);
            final int x = (int) waveLengthToX(nanoMeters);

            final var color = valueToColor(nanoMeters, value);
            g2.setColor(color);
            g2.fillRect(Math.min(lastX, x), this.viewArea.y + this.viewArea.height / 2, Math.abs(x - lastX), this.viewArea.height / 2);

            final var gray = valueToGray(value);
            g2.setColor(gray);
            g2.fillRect(Math.min(lastX, x), this.viewArea.y, Math.abs(x - lastX), this.viewArea.height / 2);

            lastX = x;
        }
    }

    protected Color valueToColor(double nanoMeters, double value) {
        final var color = Formatting.colorForWaveLength(nanoMeters);

        return new Color(
                (int) (value * color.getRed()),
                (int) (value * color.getGreen()),
                (int) (value * color.getBlue()));
    }

    protected Color valueToGray(double value) {
        return new Color((float) value, (float) value, (float) value);
    }
}
