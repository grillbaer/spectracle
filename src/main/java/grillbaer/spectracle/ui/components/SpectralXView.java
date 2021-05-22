package grillbaer.spectracle.ui.components;

import grillbaer.spectracle.spectrum.Calibration;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Basic view for spectral data over its X-axis with support for grid, cursors, labels etc.
 */
public abstract class SpectralXView extends JComponent {
    protected Calibration calibration;
    protected Rectangle viewArea;

    protected int xAxisHeight;
    private final Map<String, Cursor> xCursorsById = new TreeMap<>();

    protected SpectralXView(int xAxisHeight) {
        this.xAxisHeight = xAxisHeight;
    }

    protected SpectralXView() {
        final var mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    public void setCalibration(Calibration calibration) {
        if (!Objects.equals(this.calibration, calibration)) {
            this.calibration = calibration;
            revalidate();
            repaint();
        }
    }

    public Calibration getCalibration() {
        return this.calibration;
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
        return new Dimension(100, 10);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(10, 10);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.viewArea = null;
    }

    protected Rectangle calcViewArea() {
        final var insets = getInsets();
        final int x = insets.left;
        final int y = insets.top;
        final int w = getWidth() - insets.left - insets.right;
        final int h = getHeight() - insets.top - insets.bottom - xAxisHeight;

        return new Rectangle(x, y, w, h);
    }

    private void updateViewArea() {
        if (this.viewArea == null) {
            this.viewArea = calcViewArea();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.calibration != null) {
            updateViewArea();
            final var g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            drawView(g2);
        }
    }

    /**
     * Draw custom content.
     */
    protected abstract void drawView(Graphics2D g2);

    protected void clearViewArea(Graphics2D g2, Color color) {
        g2.setColor(color);
        g2.fill(this.viewArea);
    }

    protected void drawXGridOverlay(Graphics2D g2) {
        g2.setColor(new Color(128, 128, 128, 40));
        drawXGrid(g2, 10.);
        g2.setColor(new Color(128, 128, 128, 50));
        drawXGrid(g2, 50.);
        g2.setColor(new Color(128, 128, 128, 90));
        drawXGrid(g2, 100.);
    }

    protected void drawXGrid(Graphics2D g2, double stepNanoMeters) {
        double nanoMeters = Math.ceil(this.calibration.getMinNanoMeters() / stepNanoMeters) * stepNanoMeters;
        while (nanoMeters <= this.calibration.getMaxNanoMeters()) {
            final var x = (int) waveLengthToX(nanoMeters);
            g2.drawLine(x, this.viewArea.y, x, this.viewArea.y + this.viewArea.height);
            nanoMeters += stepNanoMeters;
        }
    }

    protected void drawXAxis(Graphics2D g2) {
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g2.setColor(getForeground());
        final var stepNanoMeters = 50.;
        double nanoMeters = Math.ceil(this.calibration.getMinNanoMeters() / stepNanoMeters) * stepNanoMeters;
        final var yBaseline = this.viewArea.y + this.viewArea.height + g2.getFontMetrics().getAscent();
        while (nanoMeters <= this.calibration.getMaxNanoMeters()) {
            final var x = (int) waveLengthToX(nanoMeters);
            final var label = String.format("%.0f", nanoMeters);
            final var labelWidth = g2.getFontMetrics().stringWidth(label);
            if (x - labelWidth / 2 > this.viewArea.x + 2
                    && x + labelWidth / 2 < this.viewArea.x + this.viewArea.width - 2) {
                g2.drawString(label, x - labelWidth / 2, yBaseline);
            }
            nanoMeters += stepNanoMeters;
        }
    }

    protected void drawXCursors(Graphics2D g2) {
        this.xCursorsById.values().forEach(c -> drawXCursor(g2, c));
    }

    protected void drawXCursor(Graphics2D g2, Cursor cursor) {
        final var x = (int) waveLengthToX(cursor.getValue());
        cursor.draw(g2, x, this.viewArea.y, x, this.viewArea.y + this.viewArea.height);
    }

    protected double waveLengthToX(double nanoMeters) {
        return viewArea.x + Math.abs(nanoMeters - calibration.getBeginNanoMeters()) / calibration.getNanoMeterRange() * viewArea.width;
    }

    protected double xToWaveLength(double x) {
        if (calibration.getBeginNanoMeters() < calibration.getEndNanoMeters()) {
            return calibration.getBeginNanoMeters() + (x - viewArea.x) / viewArea.width * calibration.getNanoMeterRange();
        } else {
            return calibration.getBeginNanoMeters() - (x - viewArea.x) / viewArea.width * calibration.getNanoMeterRange();
        }
    }

    private class MouseHandler extends MouseAdapter {
        private Cursor draggingXCursor;

        @Override
        public void mousePressed(MouseEvent e) {
            if (!getBounds().contains(e.getPoint()))
                return;

            updateViewArea();
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
                repaint();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!getBounds().contains(e.getPoint()))
                return;

            updateViewArea();
            if (this.draggingXCursor != null) {
                this.draggingXCursor.setValue(xToWaveLength(e.getX()));
                repaint();
            }
        }
    }
}
