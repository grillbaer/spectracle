package grillbaer.spectracle.ui.components;

import java.awt.*;

public final class Geometry {
    private Geometry() {
        // no instances
    }

    public static Dimension scaleToFit(int imageWidth, int imageHeight, int availableWidth, int availableHeight) {
        final var renderDim = new Dimension(imageWidth, imageHeight);
        if (renderDim.width > availableWidth) {
            renderDim.width = availableWidth;
            renderDim.height = (int) ((long) imageHeight * availableWidth / imageWidth);
        }
        if (renderDim.height > availableHeight) {
            renderDim.height = availableHeight;
            renderDim.width = (int) ((long) imageWidth * availableHeight / imageHeight);
        }

        return renderDim;
    }

    public static Dimension scaleToFitWidth(int imageWidth, int imageHeight, int availableWidth) {
        final var renderDim = new Dimension(imageWidth, imageHeight);
        renderDim.width = availableWidth;
        renderDim.height = (int) ((long) imageHeight * availableWidth / imageWidth);

        return renderDim;
    }

}
