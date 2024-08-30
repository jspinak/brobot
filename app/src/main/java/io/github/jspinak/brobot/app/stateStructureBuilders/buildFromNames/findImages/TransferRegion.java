package io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.findImages;

import io.github.jspinak.brobot.app.stateStructureBuilders.ExtendedStateImageDTO;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.babyStates.BabyState;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.attributes.AttributeTypes.Attribute.TRANSFER;

@Component
public class TransferRegion {

    private Region getLargestTransferRegion(BabyState babyState, int page) {
        List<ExtendedStateImageDTO> transfers = babyState.getImagesByAttributeAndPage(TRANSFER, page);
        if (transfers.isEmpty()) return new Region();
        Region largestTransferRegion = transfers.get(0).getStateImage().getLargestDefinedFixedRegionOrNewRegion();
        for (int i=1; i<transfers.size(); i++) {
            Region newRegion = transfers.get(i).getStateImage().getLargestDefinedFixedRegionOrNewRegion();
            if (!largestTransferRegion.isDefined() || newRegion.size() > largestTransferRegion.size())
                largestTransferRegion = newRegion;
        }
        return largestTransferRegion;
    }

    /*
    Only transfers to images, not to regions or locations.
     */
    public boolean processRegionTransfer(Set<ExtendedStateImageDTO> images, int page, BabyState babyState) {
        Region transferRegion = getLargestTransferRegion(babyState, page);
        if (!transferRegion.isDefined()) return false;
        List<ExtendedStateImageDTO> imagesToUpdate = images.stream()
                .filter(img -> !img.getStateImage().getLargestDefinedFixedRegionOrNewRegion().isDefined() ||
                        img.getStateImage().getLargestDefinedFixedRegionOrNewRegion().size() < transferRegion.size() &&
                                img.getAttributes().isStateImage())
                .toList();
        if (imagesToUpdate.isEmpty()) return false;
        Report.format("TRANSFER: SearchRegions updated to %d.%d_%d.%d for images: ",
                transferRegion.x(), transferRegion.y(), transferRegion.w(), transferRegion.h());
        imagesToUpdate.forEach(img -> {
            Report.print(img.getAttributes().getImageName()+" ");
            img.getStateImage().getDefinedFixedRegions().add(transferRegion);
        });
        Report.println();
        return true;
    }
}
