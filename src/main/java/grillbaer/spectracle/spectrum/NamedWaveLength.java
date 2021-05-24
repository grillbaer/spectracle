package grillbaer.spectracle.spectrum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Wavelength plus a readable name.
 */
@AllArgsConstructor
@Getter
public class NamedWaveLength {
    private final String name;
    private final double nanoMeters;

    public String getWaveLengthNameString(boolean withUnit) {
        final var sb = new StringBuilder();
        sb.append(Formatting.formatWaveLength(this.nanoMeters));
        if (withUnit) {
            sb.append(" nm");
        }
        if (name != null) {
            sb.append(" ").append(getName());
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return Formatting.formatWaveLength(this.nanoMeters);
    }
}
