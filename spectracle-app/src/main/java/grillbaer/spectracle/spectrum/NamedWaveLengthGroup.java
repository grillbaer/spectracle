package grillbaer.spectracle.spectrum;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class NamedWaveLengthGroup {
    private final String groupName;
    private final List<NamedWaveLength> waveLengthList;

    @Override
    public String toString() {
        return this.groupName;
    }
}
