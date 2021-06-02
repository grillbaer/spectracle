package grillbaer.spectracle.ui.components;

import grillbaer.spectracle.Context;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class Dialogs {

    private Dialogs() {
        // no instances
    }

    /**
     * Shows a file save dialog, ensures one of the allowed extensions and prompts for file overwrite if necessary.
     *
     * @param contentName content name to display in dialog title and for pre-selecting a directory
     * @param fileName    file name to pre-select or <code>null</code>
     * @return selected file or <code>null</code>
     */
    public static Path showSaveFileDialog(Context context, Component parent, @NonNull String contentName,
                                          @NonNull List<ExtensionFilter> extensions, String fileName) {
        initFx();

        final var fileChooser = new FileChooser();
        extensions.forEach(ext -> fileChooser.getExtensionFilters().add(ext));
        fileChooser.setTitle("Save " + contentName + " ...");
        fileChooser.setInitialDirectory(getLastUsedDirectory(context, contentName));
        if (fileName != null) {
            fileChooser.setInitialFileName(fileName);
        }

        final File selectedFile = showFxDialog(parent, () -> fileChooser.showSaveDialog(null));
        if (selectedFile == null)
            return null;

        setLastUsedDirectory(context, contentName, selectedFile.getParentFile());

        return selectedFile.toPath();
    }

    /**
     * Shows a file open dialog.
     *
     * @param contentName content name to display in dialog title and for pre-selecting a directory
     * @param fileName    file name to pre-select or <code>null</code>
     * @return selected file or <code>null</code>
     */
    public static Path showOpenFileDialog(Context context, Component parent, @NonNull String contentName,
                                          @NonNull List<ExtensionFilter> extensions, String fileName) {
        initFx();

        final var fileChooser = new FileChooser();
        extensions.forEach(ext -> fileChooser.getExtensionFilters().add(ext));
        fileChooser.getExtensionFilters().add(new ExtensionFilter("All Files", "*.*"));
        fileChooser.setTitle("Open " + contentName + " ...");
        fileChooser.setInitialDirectory(getLastUsedDirectory(context, contentName));
        if (fileName != null) {
            fileChooser.setInitialFileName(fileName);
        }

        final File selectedFile = showFxDialog(parent, () -> fileChooser.showOpenDialog(null));
        if (selectedFile == null)
            return null;

        setLastUsedDirectory(context, contentName, selectedFile.getParentFile());

        return selectedFile.toPath();
    }

    private static void initFx() {
        new JFXPanel();
    }

    private static <T> T showFxDialog(Component parent, @NonNull Supplier<T> fxDialogShowingCode) {
        final var resultRef = new AtomicReference<T>();
        final var waitForDialogSem = new Semaphore(0);

        setWindowEnabled(parent, false);
        try {
            Platform.runLater(() -> {
                resultRef.set(fxDialogShowingCode.get());
                waitForDialogSem.release();
            });

            try {
                waitForDialogSem.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } finally {
            setWindowEnabled(parent, true);
        }

        return resultRef.get();
    }

    private static File getLastUsedDirectory(Context context, @NonNull String contentName) {
        if (context == null)
            return null;

        final var lastUsedDirectory = context.getModel().getLastUsedDirectory(contentName);
        if (lastUsedDirectory == null)
            return null;

        return new File(lastUsedDirectory);
    }

    private static void setLastUsedDirectory(Context context, @NonNull String contentName, File dir) {
        if (context == null || dir == null)
            return;

        context.getModel().setLastUsedDirectory(contentName, dir.toString());
    }

    private static void setWindowEnabled(Component aWindowComponent, boolean enabled) {
        if (aWindowComponent == null)
            return;

        final var window = SwingUtilities.getWindowAncestor(aWindowComponent);
        if (window == null)
            return;

        window.setEnabled(enabled);
        if (enabled) {
            // window goes to back after re-enabling? not yet clear why, so use workaround for now:
            window.requestFocus();
            window.toFront();
        }
    }

    public static void showErrorDialog(JComponent parent, String... message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
