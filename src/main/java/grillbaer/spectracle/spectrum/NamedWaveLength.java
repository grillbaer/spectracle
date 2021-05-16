package grillbaer.spectracle.spectrum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@AllArgsConstructor
@Getter
public class NamedWaveLength {
    private final @NonNull String name;
    private final double nanoMeters;

    public static List<NamedWaveLength> COMMON_WAVELENGTHS = List.of(
            new NamedWaveLength("FL Purple", 405.4),
            new NamedWaveLength("FL Hg Bright Blue", 436.6),
            new NamedWaveLength("FL Hg Brightest Green", 546.4),
            new NamedWaveLength("FL Eu Bright Red", 611.4)
    );

    @Override
    public String toString() {
        return WaveLengths.format(this.nanoMeters);
    }
}
