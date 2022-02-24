package io.github.jspinak.brobot.buildStateStructure.buildFromNames.findImages;

import io.github.jspinak.brobot.buildStateStructure.buildFromNames.babyStates.BabyState;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.reports.ANSI;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.AttributeTypes.Attribute.TRANSFER;

@Component
public class TransferRegion {

    private Region getLargestTransferRegion(BabyState babyState, int page) {
        List<StateImageObject> transfers = babyState.getImagesByAttributeAndPage(TRANSFER, page);
        if (transfers.isEmpty()) return new Region();
        Region largestTransferRegion = transfers.get(0).getSearchRegion();
        for (int i=1; i<transfers.size(); i++) {
            Region newRegion = transfers.get(i).getSearchRegion();
            if (!largestTransferRegion.defined() || newRegion.size() > largestTransferRegion.size())
                largestTransferRegion = newRegion;
        }
        return largestTransferRegion;
    }

    public boolean processRegionTransfer(Set<StateImageObject> images, int page, BabyState babyState) {
        Region transferRegion = getLargestTransferRegion(babyState, page);
        if (!transferRegion.defined()) return false;
        List<StateImageObject> imagesToUpdate = images.stream()
                .filter(img -> img.getSearchRegion().size() < transferRegion.size())
                .collect(Collectors.toList());
        if (imagesToUpdate.isEmpty()) return false;
        Report.format("TRANSFER: SearchRegions updated to %d.%d_%d.%d for images: ",
                transferRegion.x, transferRegion.y, transferRegion.w, transferRegion.h);
        imagesToUpdate.forEach(img -> {
            Report.print(img.getAttributes().getImageName()+" ");
            img.setSearchRegion(transferRegion);
        });
        Report.println();
        return true;
    }
}
