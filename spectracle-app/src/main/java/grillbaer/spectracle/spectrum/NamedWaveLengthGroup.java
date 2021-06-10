package grillbaer.spectracle.spectrum;

import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@Getter
public class NamedWaveLengthGroup {
    private final @NonNull String groupName;
    private final @NonNull List<NamedWaveLength> waveLengthList;

    public NamedWaveLengthGroup(@NonNull String groupName, @NonNull List<NamedWaveLength> waveLengthList) {
        this.groupName = groupName;
        this.waveLengthList = waveLengthList.stream().map(nwl -> nwl.withGroup(this)).toList();
    }

    @Override
    public String toString() {
        return this.groupName;
    }
}
