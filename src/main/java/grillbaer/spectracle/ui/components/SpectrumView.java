package grillbaer.spectracle.ui.components;

import grillbaer.spectracle.spectrum.SpectralColors;
import grillbaer.spectracle.spectrum.Spectrum;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

public class SpectrumView extends JComponent {
    private Spectrum spectrum;
    private Rectangle graphArea;

    private final int xAxisHeight = 20;

    private final Map<String, Cursor> xCursorsById = new TreeMap<>();

    public SpectrumView() {
        final var mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    public void setSpectrum(Spectrum spectrum) {
        this.spectrum = spectrum;
        revalidate();
        repaint();
    }

    public void putXCursor(@NonNull Cursor cursor) {
        this.xCursorsById.put(cursor.getId(), cursor);
        repaint();
    }

    public Cursor getXCursor(String id) {
        return this.xCursorsById.get(id);
    }

    public Cursor removeXCursor(String id) {
        final var cursor = this.xCursorsById.remove(id);
        repaint();

        return cursor;
    }

    @Override
    public Dimension getPreferredSize() {
        if (this.spectrum != null) {
            return new Dimension(this.spectrum.getLength(), 200 + xAxisHeight);
        } else {
            return new Dimension(400, 200 + xAxisHeight);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.graphArea = null;
    }

    protected Rectangle calcGraphArea() {
        final var insets = getInsets();
        final int x = insets.left;
        final int y = insets.top;
        final int w = getWidth() - insets.left - insets.right;
        final int h = getHeight() - insets.top - insets.bottom - xAxisHeight;

        return new Rectangle(x, y, w, h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.spectrum != null) {
            updateGraphArea();
            final var g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            clearGraphArea(g2);
            drawSpectrumGraph(g2);
            drawXGridOverlay(g2);
            drawYGridOverlay(g2);
            drawXAxis(g2);
            drawXCursors(g2);
        }
    }

    private void updateGraphArea() {
        if (this.graphArea == null) {
            this.graphArea = calcGraphArea();
        }
    }

    private void clearGraphArea(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fill(this.graphArea);
    }

    private void drawXGridOverlay(Graphics2D g2) {
        g2.setColor(new Color(128, 128, 128, 40));
        drawXGrid(g2, 10.);
        g2.setColor(new Color(128, 128, 128, 50));
        drawXGrid(g2, 50.);
        g2.setColor(new Color(128, 128, 128, 90));
        drawXGrid(g2, 100.);
    }

    private void drawXGrid(Graphics2D g2, double stepNanoMeters) {
        double nanoMeters = Math.ceil(this.spectrum.getMinNanoMeters() / stepNanoMeters) * stepNanoMeters;
        while (nanoMeters <= this.spectrum.getMaxNanoMeters()) {
            final var x = (int) waveLengthToX(nanoMeters);
            g2.drawLine(x, this.graphArea.y, x, this.graphArea.y + this.graphArea.height);
            nanoMeters += stepNanoMeters;
        }
    }

    private void drawXAxis(Graphics2D g2) {
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g2.setColor(getForeground());
        final var stepNanoMeters = 50.;
        double nanoMeters = Math.ceil(this.spectrum.getMinNanoMeters() / stepNanoMeters) * stepNanoMeters;
        final var yBaseline = this.graphArea.y + this.graphArea.height + g2.getFontMetrics().getAscent();
        while (nanoMeters <= this.spectrum.getMaxNanoMeters()) {
            final var x = (int) waveLengthToX(nanoMeters);
            final var label = String.format("%.0f", nanoMeters);
            final var labelWidth = g2.getFontMetrics().stringWidth(label);
            if (x - labelWidth / 2 > this.graphArea.x + 2
                    && x + labelWidth / 2 < this.graphArea.x + this.graphArea.width - 2) {
                g2.drawString(label, x - labelWidth / 2, yBaseline);
            }
            nanoMeters += stepNanoMeters;
        }
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
            g2.drawLine(this.graphArea.x, y, this.graphArea.x + this.graphArea.width, y);
        }
    }

    private void drawSpectrumGraph(Graphics2D g2) {
        final int len = this.spectrum.getLength();
        int lastX = this.graphArea.x;
        for (int i = 0; i < len; i++) {
            final var nanoMeters = this.spectrum.getNanoMetersAtIndex(i);
            final float value = this.spectrum.getValueAtIndex(i);
            final int x = (int) waveLengthToX(nanoMeters);
            final int y = (int) valueToY(value);
            final var color = SpectralColors.getColorForWaveLength((float) nanoMeters);
            g2.setColor(color);
            g2.fillRect(lastX, y, x - lastX + 1, this.graphArea.y + this.graphArea.height - y);
            lastX = x;
        }
    }

    private void drawXCursors(Graphics2D g2) {
        this.xCursorsById.values().forEach(c -> drawXCursor(g2, c));
    }

    private void drawXCursor(Graphics2D g2, Cursor cursor) {
        final var x = (int) waveLengthToX(cursor.getValue());
        cursor.draw(g2, x, this.graphArea.y, x, this.graphArea.y + this.graphArea.height);
    }

    protected double waveLengthToX(double nanoMeters) {
        return graphArea.x + (nanoMeters - spectrum.getMinNanoMeters()) / spectrum.getNanoMeterRange() * graphArea.width;
    }

    protected double xToWaveLength(double x) {
        return (x - graphArea.x) / graphArea.width * spectrum.getNanoMeterRange() + spectrum.getMinNanoMeters();
    }

    protected double valueToY(double value) {
        return graphArea.y + graphArea.height - value * graphArea.height;
    }

    public Spectrum getSpectrum() {
        return this.spectrum;
    }

    private class MouseHandler extends MouseAdapter {
        private Cursor draggingXCursor;

        @Override
        public void mousePressed(MouseEvent e) {
            if (!getBounds().contains(e.getPoint()))
                return;

            updateGraphArea();
            this.draggingXCursor = findNearestDraggableXCursor(e.getX());
            if (this.draggingXCursor != null) {
                this.draggingXCursor.setDragging(true);
                repaint();
            }
        }

        private Cursor findNearestDraggableXCursor(int x) {
            Cursor nearestCursor = null;
            double nearestDistance = Double.POSITIVE_INFINITY;
            for (Cursor cursor : xCursorsById.values()) {
                if (cursor.isDraggable()) {
                    final var cursorDistance = Math.abs(waveLengthToX(cursor.getValue()) - x);
                    if (cursorDistance <= nearestDistance) {
                        nearestCursor = cursor;
                        nearestDistance = cursorDistance;
                    }
                }
            }
            if (nearestDistance < 15) {
                return nearestCursor;
            } else {
                return null;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (this.draggingXCursor != null) {
                this.draggingXCursor.setDragging(false);
                this.draggingXCursor = null;
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!getBounds().contains(e.getPoint()))
                return;

            updateGraphArea();
            if (this.draggingXCursor != null) {
                this.draggingXCursor.setValue(xToWaveLength(e.getX()));
            }
        }
    }
}
