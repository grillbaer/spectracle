package grillbaer.spectracle.ui.components;

import grillbaer.spectracle.spectrum.Spectrum;
import grillbaer.spectracle.spectrum.Viewing;

import java.awt.*;

/**
 * Spectrum visualization is XY graph.
 */
public class SpectrumGraphView extends SpectralXView {
    private Spectrum spectrum;

    public SpectrumGraphView() {
        super(20);
    }

    public void setSpectrum(Spectrum spectrum) {
        this.spectrum = spectrum;
        setCalibration(spectrum.getCalibration());
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (this.spectrum != null) {
            return new Dimension(this.spectrum.getLength(), 100);
        } else {
            return new Dimension(300, 100);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(300, 100);
    }

    @Override
    protected void drawView(Graphics2D g2) {
        clearViewArea(g2, Color.BLACK);
        drawSpectrumGraph(g2);
        drawXGridOverlay(g2);
        drawYGridOverlay(g2);
        drawXAxis(g2);
        drawXCursors(g2);
    }

    private void drawYGridOverlay(Graphics2D g2) {
        g2.setColor(new Color(128, 128, 128, 40));
        drawYGrid(g2, 0.1);
        g2.setColor(new Color(128, 128, 128, 90));
        drawYGrid(g2, 0.5);
    }

    private void drawYGrid(Graphics2D g2, double stepValue) {
        final int steps = (int) (1. / stepValue);
        for (int i = 0; i <= steps; i++) {
            final var y = (int) valueToY(i * stepValue);
            g2.drawLine(this.viewArea.x, y, this.viewArea.x + this.viewArea.width, y);
        }
    }

    private void drawSpectrumGraph(Graphics2D g2) {
        final int len = this.spectrum.getLength();
        int lastX = (int) waveLengthToX(this.spectrum.getNanoMetersAtIndex(0));
        for (int i = 0; i < len; i++) {
            final var nanoMeters = this.spectrum.getNanoMetersAtIndex(i);
            final double value = this.spectrum.getValueAtIndex(i);
            final int x = (int) waveLengthToX(nanoMeters);
            final int y = (int) valueToY(value);
            final var color = Viewing.colorForWaveLength(nanoMeters);
            g2.setColor(color);
            g2.fillRect(Math.min(lastX, x), y, Math.abs(x - lastX), this.viewArea.y + this.viewArea.height - y);
            lastX = x;
        }
    }

    protected double valueToY(double value) {
        return viewArea.y + viewArea.height - value * viewArea.height;
    }
}
