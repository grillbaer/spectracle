package grillbaer.spectracle;

import com.formdev.flatlaf.FlatDarkLaf;
import grillbaer.spectracle.camera.Camera;
import grillbaer.spectracle.ui.MainPanel;
import nu.pattern.OpenCV;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Application entry point.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        OpenCV.loadLocally();

        final var context = new Context();
        context.getModel().setCamera(new Camera(0));
        context.getModel().grabSingleFrame();
        context.getModel().setCameraPaused(false);

        SwingUtilities.invokeAndWait(() -> {
            FlatDarkLaf.install();
            final var mainPanel = new MainPanel(context);
            final var frame = new JFrame("Spectracle");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.getContentPane().add(mainPanel.getComponent(), BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);
        });
    }
}
