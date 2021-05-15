package grillbaer.spectracle.settings;

import grillbaer.spectracle.spectrum.Calibration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.TreeMap;


public class Settings {
    private final Map<Integer, Camera> camerasById = new TreeMap<>();

    @Getter
    @Setter
    private int selectedCameraId;

    public Camera getCamera(int id) {
        return this.camerasById.get(id);
    }

    public Camera getSelectedCamera() {
        return getCamera(getSelectedCameraId());
    }

    public Camera getOrCreateCamera(int id) {
        return this.camerasById.computeIfAbsent(id, Camera::new);
    }

    @RequiredArgsConstructor
    @Getter
    @Setter
    public static class Camera {
        private final int id;
        private Calibration calibration = Calibration.createDefault();
        private double exposure;
    }
}
