package grillbaer.spectracle.ui.components;

import grillbaer.spectracle.spectrum.Formatting;
import grillbaer.spectracle.spectrum.Spectrum;
import lombok.NonNull;

import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Spectrum visualization is XY graph.
 */
public class SpectrumGraphView extends SpectralXView {
    private static final BasicStroke REFERENCE_LIGHT_GRAPH_STROKE =
            new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1f, new float[]{0f, 8f}, 0f);
    private static final BasicStroke SENSITIVITY_CALIBRATION_GRAPH_STROKE =
            new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1f, new float[]{0f, 3f}, 0f);

    private Spectrum spectrum;

    private Spectrum referenceLightSpectrum;
    private Spectrum sensitivityCorrectionSpectrum;

    public SpectrumGraphView() {
        super(20);
        setHeadRoomHeight(80);
    }

    public void setSpectrum(Spectrum spectrum) {
        this.spectrum = spectrum;
        setCalibration(spectrum.getCalibration());
        repaint();
    }

    public void setReferenceLightSpectrum(Spectrum spectrum) {
        this.referenceLightSpectrum = spectrum;
        repaint();
    }

    public void setSensitivityCorrectionSpectrum(Spectrum spectrum) {
        this.sensitivityCorrectionSpectrum = spectrum;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (this.spectrum != null) {
            return new Dimension(this.spectrum.getLength(), 100 + getHeadRoomHeight());
        } else {
            return new Dimension(300, 100 + getHeadRoomHeight());
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(300, 100 + getHeadRoomHeight());
    }

    @Override
    protected void drawView(Graphics2D g2) {
        clearHeadroomArea(g2, Color.BLACK);
        clearViewArea(g2, Color.BLACK);
        drawXCursors(g2);
        drawSpectrumAreaGraph(g2);
        drawXGridOverlay(g2);
        drawYGridOverlay(g2);
        drawXAxis(g2);
        drawReferenceLightGraph(g2);
        drawSensitivityCorrectionGraph(g2);
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

    private void drawSpectrumAreaGraph(Graphics2D g2) {
        final int len = this.spectrum.getLength();
        int lastX = (int) waveLengthToX(this.spectrum.getNanoMetersAtIndex(0));
        for (int i = 0; i < len; i++) {
            final var nanoMeters = this.spectrum.getNanoMetersAtIndex(i);
            final double value = this.spectrum.getValueAtIndex(i);
            final int x = (int) waveLengthToX(nanoMeters);
            final int y = (int) valueToY(value);
            final var color = Formatting.colorForWaveLength(nanoMeters);
            g2.setColor(color);
            g2.fillRect(Math.min(lastX, x), y, Math.abs(x - lastX), this.viewArea.y + this.viewArea.height - y);
            lastX = x;
        }
    }

    private void drawReferenceLightGraph(Graphics2D g2) {
        if (this.referenceLightSpectrum != null) {
            drawSpectrumLineGraph(this.referenceLightSpectrum, g2, Color.WHITE, REFERENCE_LIGHT_GRAPH_STROKE);
        }
    }

    private void drawSensitivityCorrectionGraph(Graphics2D g2) {
        if (this.sensitivityCorrectionSpectrum != null) {
            drawSpectrumLineGraph(this.sensitivityCorrectionSpectrum, g2, Color.GRAY, SENSITIVITY_CALIBRATION_GRAPH_STROKE);
        }
    }

    private void drawSpectrumLineGraph(@NonNull Spectrum spectrum, @NonNull Graphics2D g2, Color color, Stroke stroke) {
        final int len = spectrum.getLength();
        final var path = new Path2D.Float(Path2D.WIND_EVEN_ODD, len + 1);
        for (int i = 0; i < len; i++) {
            final var nanoMeters = spectrum.getNanoMetersAtIndex(i);
            final double value = spectrum.getValueAtIndex(i);
            final double x = 0.5 + waveLengthToX(nanoMeters);
            final double y = 0.5 + valueToY(value);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }

        final var origHints = g2.getRenderingHints();
        final var origStroke = g2.getStroke();
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(stroke);
        g2.setColor(color);
        g2.draw(path);
        g2.setRenderingHints(origHints);
        g2.setStroke(origStroke);
    }

    protected double valueToY(double value) {
        return viewArea.y + viewArea.height - value * viewArea.height;
    }
}
