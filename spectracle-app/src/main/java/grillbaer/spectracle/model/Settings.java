package grillbaer.spectracle.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import grillbaer.spectracle.camera.CameraProps;
import grillbaer.spectracle.spectrum.WaveLengthCalibration;
import lombok.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;


/**
 * Persistent application settings.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class Settings {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Map<Integer, Camera> camerasById = new TreeMap<>();

    @JsonProperty("selectedCameraId")
    private Integer selectedCameraId;
    @JsonProperty("normalizeSampleValues")
    private Boolean normalizeSampleValues;

    @JsonProperty("lastUsedDirectories")
    private final Map<String, String> lastUsedDirectories = new HashMap<>();

    public static Settings readJson(@NonNull Path settingsFile) throws IOException {
        return new ObjectMapper().readValue(settingsFile.toFile(), Settings.class);
    }

    public void writeJson(@NonNull Path settingsFile) throws IOException {
        new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValue(settingsFile.toFile(), this);
    }

    public Camera getCamera(int id) {
        return this.camerasById.get(id);
    }

    public Camera getOrCreateCamera(int id) {
        return this.camerasById.computeIfAbsent(id, Camera::new);
    }

    @JsonGetter("cameras")
    public List<Camera> getCamerasAsList() {
        return new ArrayList<>(this.camerasById.values());
    }

    @JsonSetter("cameras")
    public void setCamerasAsList(List<Camera> cameras) {
        this.camerasById.clear();
        cameras.forEach(cam -> this.camerasById.put(cam.getId(), cam));
    }

    public void setLastUsedDirectories(Map<String, String> lastUsedDirectories) {
        this.lastUsedDirectories.clear();
        this.lastUsedDirectories.putAll(lastUsedDirectories);
    }

    @Getter
    @Setter
    @JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public static class Camera {
        @JsonProperty("id")
        private final int id;
        @JsonProperty("waveLengthCalibration")
        private WaveLengthCalibration waveLengthCalibration;
        @JsonProperty("properties")
        private CameraProps cameraProps;
        @JsonProperty("sensitivityCalibration")
        private SensitivityCalibration sensitivityCalibration;

        public Camera(@JsonProperty("id") int id) {
            this.id = id;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public static class SensitivityCalibration {
        @JsonProperty("beginNanoMeters")
        private double beginNanoMeters;
        @JsonProperty("endNanoMeters")
        private double endNanoMeters;
        @JsonProperty("correctionFactors")
        private double[] correctionFactors;
    }
}
