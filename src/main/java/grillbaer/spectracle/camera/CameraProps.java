package grillbaer.spectracle.camera;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.With;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@Getter
@With
@EqualsAndHashCode
@ToString
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public final class CameraProps {
    @JsonProperty("frameWidth")
    private final int frameWidth;
    @JsonProperty("frameHeight")
    private final int frameHeight;
    @JsonProperty("exposure")
    private final double exposure;

    public CameraProps(@JsonProperty("frameWidth") int frameWidth,
                       @JsonProperty("frameHeight") int frameHeight,
                       @JsonProperty("exposure") double exposure) {
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.exposure = exposure;
    }
}
