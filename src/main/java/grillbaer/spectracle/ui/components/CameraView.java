package grillbaer.spectracle.ui.components;

import grillbaer.spectracle.camera.Camera;
import grillbaer.spectracle.model.Observer;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

public class CameraView extends JComponent {
    @Getter
    private Camera camera;

    private Double sampleRowRatio;
    private int sampleRows = 3;
    private Color sampleRowColor = new Color(255, 255, 255, 128);
    private Stroke sampleRowStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0f,
            new float[]{2f, 5f}, 0f);

    private final Observer<Camera> cameraObserver = cam -> repaint();

    public void setCamera(Camera camera) {
        if (this.camera != null) {
            this.camera.getFrameGrabbedObservers().remove(this.cameraObserver);
        }

        this.camera = camera;

        if (this.camera != null) {
            this.camera.getFrameGrabbedObservers().add(this.cameraObserver);
        }

        revalidate();
        repaint();
    }

    public void setSampleRowPosRatio(Double sampleRowRatio) {
        this.sampleRowRatio = sampleRowRatio;
        repaint();
    }

    public void setSampleRows(int sampleRowThickness) {
        this.sampleRows = sampleRowThickness;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (camera != null) {
            final var insets = getInsets();
            final var image = camera.getFrameImage();
            if (image != null) {
                final int availableWidth = getWidth() - insets.left - insets.right;
                final int availableHeight = getHeight() - insets.top - insets.bottom;
                final Dimension renderDim = Geometry.scaleToFitWidth(
                        image.getWidth(), image.getHeight(),
                        availableWidth, availableHeight);
                final var imageY0 = insets.top + (availableHeight - renderDim.height) / 2;
                final var imageX0 = insets.left + (availableWidth - renderDim.width) / 2;
                g.drawImage(image, imageX0, imageY0,
                        renderDim.width, renderDim.height, null);
                drawSampleRow((Graphics2D) g, imageX0, imageY0, renderDim.width, renderDim.height);
            }
        }
    }

    private void drawSampleRow(Graphics2D g2, int imageX0, int imageY0, int imageWidth, int imageHeight) {
        if (this.sampleRowRatio != null) {
            final var origColor = g2.getColor();
            final var origStroke = g2.getStroke();
            g2.setColor(this.sampleRowColor);
            g2.setStroke(this.sampleRowStroke);

            final int y = imageY0 + (int) (imageHeight * this.sampleRowRatio);
            final int y0 = y - this.sampleRows / 2;
            final int y1 = y0 + this.sampleRows;
            g2.drawLine(imageX0, y0 - 1, imageX0 + imageWidth, y0 - 1);
            g2.drawLine(imageX0, y1 + 1, imageX0 + imageWidth, y1 + 1);

            g2.setStroke(origStroke);
            g2.setColor(origColor);
        }
    }


    @Override
    public Dimension getPreferredSize() {
        final var insets = getInsets();
        if (camera != null) {
            return new Dimension(insets.left + camera.getCameraProps().getFrameWidth() + insets.right,
                    insets.top + camera.getCameraProps().getFrameHeight() + insets.bottom);
        } else {
            return new Dimension(insets.left + 640 + insets.right,
                    insets.top + 480 + insets.bottom);
        }
    }
}
