package grillbaer.spectracle.camera;


import lombok.*;

@AllArgsConstructor
@Getter
@With
@EqualsAndHashCode
@ToString
public final class CameraProps {
    private final int frameWidth;
    private final int frameHeight;
    private final double exposure;
}
