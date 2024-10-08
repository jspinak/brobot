package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.database.entities.ActionOptionsEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ActionOptionsEntityMapper {

    private final LocationEntityMapper locationEntityMapper;
    private final SearchRegionsEmbeddableMapper searchRegionsEmbeddableMapper;

    public ActionOptionsEntityMapper(LocationEntityMapper locationEntityMapper,
                                     SearchRegionsEmbeddableMapper searchRegionsEmbeddableMapper) {
        this.locationEntityMapper = locationEntityMapper;
        this.searchRegionsEmbeddableMapper = searchRegionsEmbeddableMapper;
    }

    public ActionOptionsEntity map(ActionOptions actionOptions) {
        ActionOptionsEntity actionOptionsEntity = new ActionOptionsEntity();
        actionOptionsEntity.setAction(actionOptions.getAction());
        actionOptionsEntity.setClickUntil(actionOptions.getClickUntil());
        actionOptionsEntity.setFind(actionOptions.getFind());
        actionOptionsEntity.setFindActions(actionOptions.getFindActions());
        actionOptionsEntity.setKeepLargerMatches(actionOptions.isKeepLargerMatches());
        actionOptionsEntity.setDoOnEach(actionOptions.getDoOnEach());
        actionOptionsEntity.setCaptureImage(actionOptions.isCaptureImage());
        actionOptionsEntity.setUseDefinedRegion(actionOptions.isUseDefinedRegion());
        actionOptionsEntity.setSimilarity(actionOptions.getSimilarity());
        actionOptionsEntity.setPauseBeforeMouseDown(actionOptions.getPauseBeforeMouseDown());
        actionOptionsEntity.setPauseAfterMouseDown(actionOptions.getPauseAfterMouseDown());
        actionOptionsEntity.setMoveMouseDelay(actionOptions.getMoveMouseDelay());
        actionOptionsEntity.setPauseBeforeMouseUp(actionOptions.getPauseBeforeMouseUp());
        actionOptionsEntity.setPauseAfterMouseUp(actionOptions.getPauseAfterMouseUp());
        actionOptionsEntity.setClickType(actionOptions.getClickType());
        actionOptionsEntity.setMoveMouseAfterClick(actionOptions.isMoveMouseAfterAction());
        actionOptionsEntity.setLocationAfterAction(locationEntityMapper.map(actionOptions.getMoveMouseAfterActionTo()));
        actionOptionsEntity.setOffsetLocationBy(locationEntityMapper.map(actionOptions.getMoveMouseAfterActionBy()));
        actionOptionsEntity.setSearchRegions(searchRegionsEmbeddableMapper.map(actionOptions.getSearchRegions()));
        actionOptionsEntity.setPauseBeforeBegin(actionOptions.getPauseBeforeBegin());
        actionOptionsEntity.setPauseAfterEnd(actionOptions.getPauseAfterEnd());
        actionOptionsEntity.setPauseBetweenIndividualActions(actionOptions.getPauseBetweenIndividualActions());
        actionOptionsEntity.setTimesToRepeatIndividualAction(actionOptions.getTimesToRepeatIndividualAction());
        actionOptionsEntity.setMaxTimesToRepeatActionSequence(actionOptions.getMaxTimesToRepeatActionSequence());
        actionOptionsEntity.setPauseBetweenActionSequences(actionOptions.getPauseBetweenActionSequences());
        actionOptionsEntity.setMaxWait(actionOptions.getMaxWait());
        actionOptionsEntity.setMaxMatchesToActOn(actionOptions.getMaxMatchesToActOn());
        actionOptionsEntity.setDragToOffsetX(actionOptions.getDragToOffsetX());
        actionOptionsEntity.setDragToOffsetY(actionOptions.getDragToOffsetY());
        actionOptionsEntity.setDefineAs(actionOptions.getDefineAs());
        actionOptionsEntity.setAddW(actionOptions.getAddW());
        actionOptionsEntity.setAddH(actionOptions.getAddH());
        actionOptionsEntity.setAbsoluteW(actionOptions.getAbsoluteW());
        actionOptionsEntity.setAbsoluteH(actionOptions.getAbsoluteH());
        actionOptionsEntity.setAddX(actionOptions.getAddX());
        actionOptionsEntity.setAddY(actionOptions.getAddY());
        actionOptionsEntity.setAddX2(actionOptions.getAddX2());
        actionOptionsEntity.setAddY2(actionOptions.getAddY2());
        actionOptionsEntity.setHighlightAllAtOnce(actionOptions.isHighlightAllAtOnce());
        actionOptionsEntity.setHighlightSeconds(actionOptions.getHighlightSeconds());
        actionOptionsEntity.setHighlightColor(actionOptions.getHighlightColor());
        actionOptionsEntity.setGetTextUntil(actionOptions.getGetTextUntil());
        actionOptionsEntity.setTextToAppearOrVanish(actionOptions.getTextToAppearOrVanish());
        actionOptionsEntity.setTypeDelay(actionOptions.getTypeDelay());
        actionOptionsEntity.setModifiers(actionOptions.getModifiers());
        actionOptionsEntity.setScrollDirection(actionOptions.getScrollDirection());
        actionOptionsEntity.setColor(actionOptions.getColor());
        actionOptionsEntity.setDiameter(actionOptions.getDiameter());
        actionOptionsEntity.setKmeans(actionOptions.getKmeans());
        actionOptionsEntity.setHueBins(actionOptions.getHueBins());
        actionOptionsEntity.setSaturationBins(actionOptions.getSaturationBins());
        actionOptionsEntity.setValueBins(actionOptions.getValueBins());
        actionOptionsEntity.setMinScore(actionOptions.getMinScore());
        actionOptionsEntity.setMinArea(actionOptions.getMinArea());
        actionOptionsEntity.setMaxArea(actionOptions.getMaxArea());
        actionOptionsEntity.setMaxMovement(actionOptions.getMaxMovement());
        actionOptionsEntity.setIllustrate(actionOptions.getIllustrate());
        actionOptionsEntity.setFusionMethod(actionOptions.getFusionMethod());
        actionOptionsEntity.setMaxFusionDistanceX(actionOptions.getMaxFusionDistanceX());
        actionOptionsEntity.setMaxFusionDistanceY(actionOptions.getMaxFusionDistanceY());
        actionOptionsEntity.setSceneToUseForCaptureAfterFusingMatches(actionOptions.getSceneToUseForCaptureAfterFusingMatches());
        return actionOptionsEntity;
    }
    
    public ActionOptions map(ActionOptionsEntity actionOptionsEntity) {
        ActionOptions actionOptions = new ActionOptions();
        actionOptions.setAction(actionOptionsEntity.getAction());
        actionOptions.setClickUntil(actionOptionsEntity.getClickUntil());
        actionOptions.setFind(actionOptionsEntity.getFind());
        actionOptions.setFindActions(new ArrayList<>(actionOptionsEntity.getFindActions()));
        actionOptions.setKeepLargerMatches(actionOptionsEntity.isKeepLargerMatches());
        actionOptions.setDoOnEach(actionOptionsEntity.getDoOnEach());
        actionOptions.setCaptureImage(actionOptionsEntity.isCaptureImage());
        actionOptions.setUseDefinedRegion(actionOptionsEntity.isUseDefinedRegion());
        actionOptions.setSimilarity(actionOptionsEntity.getSimilarity());
        actionOptions.setPauseBeforeMouseDown(actionOptionsEntity.getPauseBeforeMouseDown());
        actionOptions.setPauseAfterMouseDown(actionOptionsEntity.getPauseAfterMouseDown());
        actionOptions.setMoveMouseDelay(actionOptionsEntity.getMoveMouseDelay());
        actionOptions.setPauseBeforeMouseUp(actionOptionsEntity.getPauseBeforeMouseUp());
        actionOptions.setPauseAfterMouseUp(actionOptionsEntity.getPauseAfterMouseUp());
        actionOptions.setClickType(actionOptionsEntity.getClickType());
        actionOptions.setMoveMouseAfterAction(actionOptionsEntity.isMoveMouseAfterClick());
        actionOptions.setMoveMouseAfterActionTo(locationEntityMapper.map(actionOptionsEntity.getLocationAfterAction()));
        actionOptions.setMoveMouseAfterActionBy(locationEntityMapper.map(actionOptionsEntity.getOffsetLocationBy()));
        actionOptions.setSearchRegions(searchRegionsEmbeddableMapper.map(actionOptionsEntity.getSearchRegions()));
        actionOptions.setPauseBeforeBegin(actionOptionsEntity.getPauseBeforeBegin());
        actionOptions.setPauseAfterEnd(actionOptionsEntity.getPauseAfterEnd());
        actionOptions.setPauseBetweenIndividualActions(actionOptionsEntity.getPauseBetweenIndividualActions());
        actionOptions.setTimesToRepeatIndividualAction(actionOptionsEntity.getTimesToRepeatIndividualAction());
        actionOptions.setMaxTimesToRepeatActionSequence(actionOptionsEntity.getMaxTimesToRepeatActionSequence());
        actionOptions.setPauseBetweenActionSequences(actionOptionsEntity.getPauseBetweenActionSequences());
        actionOptions.setMaxWait(actionOptionsEntity.getMaxWait());
        actionOptions.setMaxMatchesToActOn(actionOptionsEntity.getMaxMatchesToActOn());
        actionOptions.setDragToOffsetX(actionOptionsEntity.getDragToOffsetX());
        actionOptions.setDragToOffsetY(actionOptionsEntity.getDragToOffsetY());
        actionOptions.setDefineAs(actionOptionsEntity.getDefineAs());
        actionOptions.setAddW(actionOptionsEntity.getAddW());
        actionOptions.setAddH(actionOptionsEntity.getAddH());
        actionOptions.setAbsoluteW(actionOptionsEntity.getAbsoluteW());
        actionOptions.setAbsoluteH(actionOptionsEntity.getAbsoluteH());
        actionOptions.setAddX(actionOptionsEntity.getAddX());
        actionOptions.setAddY(actionOptionsEntity.getAddY());
        actionOptions.setAddX2(actionOptionsEntity.getAddX2());
        actionOptions.setAddY2(actionOptionsEntity.getAddY2());
        actionOptions.setHighlightAllAtOnce(actionOptionsEntity.isHighlightAllAtOnce());
        actionOptions.setHighlightSeconds(actionOptionsEntity.getHighlightSeconds());
        actionOptions.setHighlightColor(actionOptionsEntity.getHighlightColor());
        actionOptions.setGetTextUntil(actionOptionsEntity.getGetTextUntil());
        actionOptions.setTextToAppearOrVanish(actionOptionsEntity.getTextToAppearOrVanish());
        actionOptions.setTypeDelay(actionOptionsEntity.getTypeDelay());
        actionOptions.setModifiers(actionOptionsEntity.getModifiers());
        actionOptions.setScrollDirection(actionOptionsEntity.getScrollDirection());
        actionOptions.setColor(actionOptionsEntity.getColor());
        actionOptions.setDiameter(actionOptionsEntity.getDiameter());
        actionOptions.setKmeans(actionOptionsEntity.getKmeans());
        actionOptions.setHueBins(actionOptionsEntity.getHueBins());
        actionOptions.setSaturationBins(actionOptionsEntity.getSaturationBins());
        actionOptions.setValueBins(actionOptionsEntity.getValueBins());
        actionOptions.setMinScore(actionOptionsEntity.getMinScore());
        actionOptions.setMinArea(actionOptionsEntity.getMinArea());
        actionOptions.setMaxArea(actionOptionsEntity.getMaxArea());
        actionOptions.setMaxMovement(actionOptionsEntity.getMaxMovement());
        actionOptions.setIllustrate(actionOptionsEntity.getIllustrate());
        actionOptions.setFusionMethod(actionOptionsEntity.getFusionMethod());
        actionOptions.setMaxFusionDistanceX(actionOptionsEntity.getMaxFusionDistanceX());
        actionOptions.setMaxFusionDistanceY(actionOptionsEntity.getMaxFusionDistanceY());
        actionOptions.setSceneToUseForCaptureAfterFusingMatches(actionOptionsEntity.getSceneToUseForCaptureAfterFusingMatches());
        return actionOptions;
    }
}
