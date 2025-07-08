# API Migration Report

Generated: Fri Jul  4 22:56:54 CEST 2025

## Files Requiring Manual Review

The following patterns were detected that may need manual intervention:

### ActionOptions with complex builders
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/ActionDefinitionJsonParserTest.java:                new ActionOptions.Builder().setAction(FIND).setFind(ALL).build(),
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/ActionDefinitionJsonParserTest.java:                new ActionOptions.Builder().setAction(CLICK).setClickType(RIGHT).build(),
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTestUpdated.java:        ActionOptions legacyOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTestUpdated.java:        ActionOptions legacyOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTest.java:                .setActionOptions(new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/WordMatchesTestsUpdated.java:        ActionOptions oldOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/WordMatchesTests.java:            ActionOptions findWordsOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTestUpdated.java:     * Example of updating a test that used old ActionOptions.Action enum
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTestUpdated.java:        // ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:        ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:        ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:        ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:        ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:        ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:        ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:            ActionOptions.Action actionType = index % 2 == 0 ? 
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:                ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:        ActionOptions options1 = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:        ActionOptions options2 = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindAllTestUpdated.java:            ActionOptions legacyOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindAllTestUpdated.java:                    .setDefineAs(ActionOptions.DefineAs.INSIDE_ANCHORS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindAllTestUpdated.java:            ActionOptions legacyOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindAllTestUpdated.java:            ActionOptions legacyOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindInSceneTest.java:            ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindInSceneTest.java:                    .setDefineAs(ActionOptions.DefineAs.INSIDE_ANCHORS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindInSceneTestUpdated.java:        ActionOptions oldOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindInSceneTestUpdated.java:                .setDefineAs(ActionOptions.DefineAs.INSIDE_ANCHORS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindAllTest.java:            ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindAllTest.java:                    .setDefineAs(ActionOptions.DefineAs.INSIDE_ANCHORS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTestUpdated.java:        ActionOptions oldOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTestUpdated.java:        ActionOptions oldOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTestUpdated.java:        ActionOptions oldOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTest.java:            ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTest.java:            ActionOptions findAndFuseWords = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTest.java:                    .setFusionMethod(ActionOptions.MatchFusionMethod.RELATIVE)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTest.java:            ActionOptions findWordsDontFuse = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTest.java:                    .setFusionMethod(ActionOptions.MatchFusionMethod.NONE)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTest.java:            ActionOptions findSimilar = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/GetSceneCombinationsTest.java:            ActionOptions findWordsOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/PopulateSceneCombinationsTest.java:            ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/PopulateSceneCombinationsTest.java:            ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/FindStatesData.java:    private final ActionOptions findWordsOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/FindStatesData.java:    private final ActionOptions findStatesOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/GetSceneCombinationsTestUpdated.java:        ActionOptions oldOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/PopulateSceneCombinationsTestUpdated.java:        ActionOptions oldOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithOffsetTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindFixedPixelMatchesTestUpdated.java:        ActionOptions oldOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindDynamicPixelMatchesTest.java:    private ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindFixedPixelMatchesTest.java:    private ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindDynamicPixelMatchesTestUpdated.java:        ActionOptions oldOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithPositionTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java:        ActionOptions oldOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java:                .setFusionMethod(ActionOptions.MatchFusionMethod.RELATIVE)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindAllWordsTest.java:            ActionOptions findAllWords = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindAllWordsTest.java:                    .setFusionMethod(ActionOptions.MatchFusionMethod.RELATIVE)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithOffsetTestUpdated.java:        ActionOptions oldOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithPositionTestUpdated.java:        ActionOptions oldOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/matchManagement/MatchFusionTest.java:        matches.setActionOptions(new ActionOptions.Builder().setFusionMethod(ActionOptions.MatchFusionMethod.ABSOLUTE).build());
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindAllWordsTestUpdated.java:        ActionOptions oldOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindAllWordsTestUpdated.java:                .setFusionMethod(ActionOptions.MatchFusionMethod.RELATIVE)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java:        ActionOptions oldOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/TestData.java:            defineInsideAnchors = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/TestData.java:                    .setDefineAs(ActionOptions.DefineAs.INSIDE_ANCHORS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:        ActionOptions findOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:        ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:        ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:        ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:        ActionOptions firstOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:        ActionOptions allOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:        ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:        ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:        ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTestUpdated.java:        ActionOptions oldOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTestUpdated.java:                .setAction(ActionOptions.Action.DRAG)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsJsonParserTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsJsonParserTest.java:        ActionOptions deserializedOptions = jsonParser.convertJson(json, ActionOptions.class);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsJsonParserTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsJsonParserTest.java:        ActionOptions deserializedOptions = jsonParser.convertJson(json, ActionOptions.class);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTest.java:        ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTest.java:                .setAction(ActionOptions.Action.DRAG)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTest.java:        assertEquals(ActionOptions.Action.DRAG, options.getAction());
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTest.java:        ActionOptions dragOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTest.java:                .setAction(ActionOptions.Action.DRAG)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTest.java:        ActionOptions clickOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:            .setClickUntil(ActionOptions.ClickUntil.OBJECTS_APPEAR)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:            .setClickUntil(ActionOptions.ClickUntil.OBJECTS_VANISH)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:        ActionOptions actionOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:            .setAction(ActionOptions.Action.DRAG)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:            .setScrollDirection(ActionOptions.ScrollDirection.DOWN)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:        ActionOptions clickOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:            .setClickType(ActionOptions.ClickType.MID)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:        ActionOptions findOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:        ActionOptions moveOptions = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTest.java:        finishOptions.setAction(ActionOptions.Action.HIGHLIGHT);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTest.java:        assertEquals(ActionOptions.Action.HIGHLIGHT,
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/PathFindingComponentTests.java:        ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/PathFindingWithActionDefinitionTests.java:        ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsCreationTest.java:        ActionOptions options = new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/test/SimpleAPITest.java:        ActionOptions options = new ActionOptions.Builder()

### Deprecated method calls
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/BuilderMethodJsonParserTest.java:                  "method": "setAction",
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/BuilderMethodJsonParserTest.java:        assertEquals("setAction", method.getMethod());
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/ActionDefinitionJsonParserTestUpdated.java:                .setClickType(ClickOptions.Type.RIGHT)
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/ActionDefinitionJsonParserTest.java:                new ActionOptions.Builder().setAction(FIND).setFind(ALL).build(),
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/ActionDefinitionJsonParserTest.java:                new ActionOptions.Builder().setAction(CLICK).setClickType(RIGHT).build(),
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/ActionDefinitionJsonParserTest.java:        options.setAction(action);
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/BuilderExpressionJsonParserTest.java:                      "method": "setAction",
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/BuilderExpressionJsonParserTest.java:                      "method": "setClickType",
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/BuilderExpressionJsonParserTest.java:        assertEquals("setAction", method1.getMethod());
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/BuilderExpressionJsonParserTest.java:        assertEquals("setClickType", method3.getMethod());
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/BuilderExpressionJsonParserTest.java:        method1.setMethod("setAction");
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/BuilderExpressionJsonParserTest.java:        method2.setMethod("setFind");
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/BuilderExpressionJsonParserTest.java:        assertEquals("setAction", deserializedMethod1.getMethod());
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/BuilderExpressionJsonParserTest.java:        assertEquals("setFind", deserializedMethod2.getMethod());
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/ExpressionJsonParserTest.java:                      "method": "setAction",
../../library-test/src/test/java/io/github/jspinak/brobot/dsl/ExpressionJsonParserTest.java:        assertEquals("setAction", method1.getMethod());
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTestUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTestUpdated.java:                .setActionConfig(clickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTestUpdated.java:                .setActionSuccess(true)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTestUpdated.java:                .setActionConfig(findOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTestUpdated.java:                .setClickType(ClickOptions.Type.RIGHT)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTestUpdated.java:                .setActionConfig(clickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTestUpdated.java:                .setActionConfig(findOptions1)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTestUpdated.java:                .setActionConfig(findOptions2)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTestUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTestUpdated.java:                .setActionConfig(clickOptions)  // Different action type
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTestUpdated.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTestUpdated.java:                .setFind(FindOptions.FindStrategy.FIRST)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTestUpdated.java:                .setActionOptions(legacyOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchesJsonParserTest.java:        matches.setActionDescription("Test Find Action");
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTestUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTestUpdated.java:        snapshot.setActionConfig(clickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTestUpdated.java:        snapshot.setActionSuccess(true);
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTestUpdated.java:                .setActionConfig(moveOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTestUpdated.java:                .setActionSuccess(true)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTestUpdated.java:                .setActionConfig(findOptions1)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTestUpdated.java:                .setActionConfig(findOptions2)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTestUpdated.java:                .setActionConfig(findOptions2)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTestUpdated.java:                .setActionConfig(findOptions2)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTestUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTestUpdated.java:                .setActionConfig(clickOptions) // Different action type
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTestUpdated.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTestUpdated.java:                .setFind(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTestUpdated.java:        snapshot.setActionOptions(legacyOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTest.java:                .setActionOptions(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTest.java:                .setActionSuccess(true)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTest.java:                .setActionOptions(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTest.java:                .setActionOptions(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTest.java:                .setActionOptions(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTest.java:                .setActionOptions(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchHistoryJsonParserTest.java:                .setActionOptions(ClickOptions)  // Different action
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTest.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTest.java:                .setFind(FindOptions.FindStrategy.FIRST)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTest.java:        snapshot.setActionOptions(actionOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTest.java:        snapshot.setActionSuccess(true);
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTest.java:                .setActionOptions(new ActionOptions.Builder()
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTest.java:                        .setAction(MoveOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTest.java:                        .setFind(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTest.java:                .setActionSuccess(true)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTest.java:                .setActionOptions(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTest.java:                .setActionOptions(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTest.java:                .setActionOptions(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTest.java:                .setActionOptions(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/model/match/MatchSnapshotJsonParserTest.java:                .setActionOptions(ClickOptions) // Different action
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/WordMatchesTestsUpdated.java: * - ActionResult requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/WordMatchesTestsUpdated.java:            result.setActionConfig(findWordsOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/WordMatchesTestsUpdated.java:            result.setActionConfig(findWordsOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/WordMatchesTestsUpdated.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/WordMatchesTestsUpdated.java:                .setFind(FindOptions.FindStrategy.ALL_WORDS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/WordMatchesTestsUpdated.java:        newResult.setActionConfig(newOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/WordMatchesTests.java:                    .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/WordMatchesTests.java:                    .setFind(FindOptions.FindStrategy.ALL_WORDS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTestUpdated.java: * - ActionResult now requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.BEST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTestUpdated.java:                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTestUpdated.java:        result.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTestUpdated.java:                .setClickType(ClickOptions.Type.RIGHT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTestUpdated.java:        result.setActionConfig(clickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTestUpdated.java:        result.setActionConfig(customConfig);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTestUpdated.java:        //     .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTestUpdated.java:        //     .setFind(FindOptions.FindStrategy.BEST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.BEST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:                .setAction(TypeOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:                .setFind(FindOptions.FindStrategy.FIRST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:                .setAction(DefineRegionOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:                        .setAction(actionType)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionExecution/ActionServiceIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindAllTestUpdated.java:                    .setAction(DefineRegionOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindAllTestUpdated.java:                    .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindAllTestUpdated.java:                    .setFind(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindAllTestUpdated.java:                    .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindAllTestUpdated.java:                    .setFind(FindOptions.FindStrategy.BEST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindInSceneTest.java:                    .setAction(DefineRegionOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindInSceneTestUpdated.java: * - ActionResult requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindInSceneTestUpdated.java:            result.setActionConfig(defineOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindInSceneTestUpdated.java:                result.setActionConfig(options);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindInSceneTestUpdated.java:                .setAction(DefineRegionOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindInSceneTestUpdated.java:        newResult.setActionConfig(newOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/sikuliWrappers/find/FindAllTest.java:                    .setAction(DefineRegionOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/define/DefineInsideAnchorsTest.java:            matches.setActionOptions(testData.getDefineInsideAnchors());
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/define/DefineHelperTest.java:            matches.setActionOptions(testData.getDefineInsideAnchors());
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTestUpdated.java: * - ActionResult requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTestUpdated.java:                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTestUpdated.java:        result.setActionConfig(clickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTestUpdated.java:                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTestUpdated.java:        result.setActionConfig(clickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTestUpdated.java:                .setClickType(ClickOptions.Type.TRIPLE_LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTestUpdated.java:        result.setActionConfig(clickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTestUpdated.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTestUpdated.java:                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTestUpdated.java:                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTestUpdated.java:        newResult.setActionConfig(newOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTest.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTest.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTest.java:                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTest.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTest.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTest.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTestUpdated.java: * - ActionResult requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTestUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTestUpdated.java:        result.setActionConfig(clickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTestUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTestUpdated.java:        result.setActionConfig(clickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTestUpdated.java:                .setClickType(ClickOptions.Type.RIGHT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTestUpdated.java:        rightClickResult.setActionConfig(rightClickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTestUpdated.java:                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTestUpdated.java:        doubleClickResult.setActionConfig(doubleClickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTestUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTestUpdated.java:        result.setActionConfig(clickWithPausesOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTestUpdated.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickMatchWithAddXYTestUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTestUpdated.java: * - ActionResult requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTestUpdated.java:        result.setActionConfig(clickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTestUpdated.java:                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTestUpdated.java:        result.setActionConfig(clickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTestUpdated.java:        result.setActionConfig(clickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTestUpdated.java:                .setClickType(ClickOptions.Type.RIGHT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTestUpdated.java:        result.setActionConfig(clickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTestUpdated.java:                .setClickType(ClickOptions.Type.MIDDLE)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTestUpdated.java:        result.setActionConfig(clickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTestUpdated.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTestUpdated.java:                .setClickType(ClickOptions.Type.RIGHT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTestUpdated.java:                .setClickType(ClickOptions.Type.RIGHT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/ClickIntegrationTestUpdated.java:        newResult.setActionConfig(newOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTest.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTest.java:                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTest.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/click/DoubleClickTest.java:                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTest.java:                    .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTest.java:                    .setFind(FindOptions.FindStrategy.SIMILAR_IMAGES)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTest.java:                    .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTest.java:                    .setFind(ALL_WORDS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTest.java:                    .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTest.java:                    .setFind(ALL_WORDS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTest.java:                    .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTest.java:                    .setFind(FindOptions.FindStrategy.SIMILAR_IMAGES)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/FindStatesDataUpdated.java:            matches.setActionConfig(textFindOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/GetSceneCombinationsTest.java:                    .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/GetSceneCombinationsTest.java:                    .setFind(FindOptions.FindStrategy.ALL_WORDS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/PopulateSceneCombinationsTest.java:                    .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/PopulateSceneCombinationsTest.java:                    .setFind(FindOptions.FindStrategy.STATES)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/PopulateSceneCombinationsTest.java:                    .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/PopulateSceneCombinationsTest.java:                    .setFind(FindOptions.FindStrategy.STATES)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/FindStatesData.java:            .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/FindStatesData.java:            .setFind(FindOptions.FindStrategy.ALL_WORDS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/FindStatesData.java:            .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/FindStatesData.java:            .setFind(FindOptions.FindStrategy.STATES)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/GetSceneCombinationsTestUpdated.java: * - ActionResult requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/GetSceneCombinationsTestUpdated.java:            matches.setActionConfig(textFindOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/GetSceneCombinationsTestUpdated.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/GetSceneCombinationsTestUpdated.java:                .setFind(FindOptions.FindStrategy.ALL_WORDS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/PopulateSceneCombinationsTestUpdated.java: * - ActionResult requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/PopulateSceneCombinationsTestUpdated.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/states/PopulateSceneCombinationsTestUpdated.java:                .setFind(FindOptions.FindStrategy.STATES)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setFind(FindOptions.FindStrategy.FIRST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setFind(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setFind(FindOptions.FindStrategy.BEST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setFind(FindOptions.FindStrategy.FIRST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setFind(FindOptions.FindStrategy.EACH)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setFind(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTest.java:                .setFind(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithOffsetTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindFixedPixelMatchesTestUpdated.java: * - ActionResult requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindFixedPixelMatchesTestUpdated.java:        matches.setActionConfig(fixedPixelsOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindFixedPixelMatchesTestUpdated.java:        matches.setActionConfig(fixedPixelsOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindFixedPixelMatchesTestUpdated.java:        matches.setActionConfig(customOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindFixedPixelMatchesTestUpdated.java:        matches.setActionConfig(fixedPixelsOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindFixedPixelMatchesTestUpdated.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindFixedPixelMatchesTestUpdated.java:                .setFind(FindOptions.FindStrategy.FIXED_PIXELS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindDynamicPixelMatchesTest.java:            .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindDynamicPixelMatchesTest.java:            .setFind(FindOptions.FindStrategy.FIXED_PIXELS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindFixedPixelMatchesTest.java:            .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindFixedPixelMatchesTest.java:            .setFind(FindOptions.FindStrategy.FIXED_PIXELS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindDynamicPixelMatchesTestUpdated.java: * - ActionResult requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindDynamicPixelMatchesTestUpdated.java:        matches.setActionConfig(dynamicPixelsOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindDynamicPixelMatchesTestUpdated.java:        matches.setActionConfig(dynamicPixelsOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindDynamicPixelMatchesTestUpdated.java:        matches.setActionConfig(customOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindDynamicPixelMatchesTestUpdated.java:        matches.setActionConfig(dynamicPixelsOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindDynamicPixelMatchesTestUpdated.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/fixedAndDynamicPixels/FindDynamicPixelMatchesTestUpdated.java:                .setFind(FindOptions.FindStrategy.FIXED_PIXELS)  // Note: was using FIXED_PIXELS
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithPositionTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java: * - ActionResult requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java:                    .setFindStrategy(FindOptions.FindStrategy.SIMILAR_IMAGES)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java:            result.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java:                    .setFindStrategy(FindOptions.FindStrategy.ALL_WORDS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java:            fusedResult.setActionConfig(findAndFuseWords);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java:                    .setFindStrategy(FindOptions.FindStrategy.ALL_WORDS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java:            notFusedResult.setActionConfig(findWordsDontFuse);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java:                    .setFindStrategy(FindOptions.FindStrategy.SIMILAR_IMAGES)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java:            similarResult.setActionConfig(findSimilar);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java:                        .setFindStrategy(FindOptions.FindStrategy.ALL_WORDS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java:                result.setActionConfig(options);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java:                .setFind(FindOptions.FindStrategy.ALL_WORDS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.ALL_WORDS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindSimilarImagesTestUpdated.java:        newResult.setActionConfig(newOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindAllWordsTest.java:                    .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindAllWordsTest.java:                    .setFind(ALL_WORDS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithOffsetTestUpdated.java: * - ActionResult requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithOffsetTestUpdated.java:        matches.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithOffsetTestUpdated.java:        matches2.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithOffsetTestUpdated.java:        bestResult.setActionConfig(bestFindOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithOffsetTestUpdated.java:        allResult.setActionConfig(allFindOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithOffsetTestUpdated.java:        matches.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithOffsetTestUpdated.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithPositionTestUpdated.java: * - ActionResult requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithPositionTestUpdated.java:        matches.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithPositionTestUpdated.java:        matches2.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithPositionTestUpdated.java:        bestResult.setActionConfig(bestOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithPositionTestUpdated.java:        allResult.setActionConfig(allOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithPositionTestUpdated.java:        result.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindImageWithPositionTestUpdated.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/matchManagement/MatchFusionTest.java:        matches.setActionOptions(new ActionOptions.Builder().setFusionMethod(ActionOptions.MatchFusionMethod.ABSOLUTE).build());
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindAllWordsTestUpdated.java: * - ActionResult requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindAllWordsTestUpdated.java:            result.setActionConfig(textFindOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindAllWordsTestUpdated.java:            result.setActionConfig(textFindOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindAllWordsTestUpdated.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindAllWordsTestUpdated.java:                .setFind(ALL_WORDS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java: * - ActionResult requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.FIRST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java:        result.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java:        result.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java:        result.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.BEST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java:        result.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.FIRST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java:        result.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.EACH)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java:        result.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java:                .setFind(FindOptions.FindStrategy.BEST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.BEST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/find/FindActionIntegrationTestUpdated.java:        newResult.setActionConfig(newOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/methods/basicactions/TestData.java:                    .setAction(DefineRegionOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:        snapshot1.setActionSuccess(true);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:        snapshot1.setActionOptions(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:                .setFind(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:                .setFind(FindOptions.FindStrategy.FIRST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:                .setFind(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTestUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.FIRST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.BEST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.EACH)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.ALL_WORDS)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTestUpdated.java:                .setAction(ActionOptions.Action.DRAG)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTestUpdated.java:                .setFind(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTestUpdated.java:                .setFindStrategy(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsJsonParserTest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsJsonParserTest.java:                .setFind(FindOptions.FindStrategy.FIRST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsJsonParserTest.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsJsonParserTest.java:                .setFind(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTest.java:                .setAction(ActionOptions.Action.DRAG)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTest.java:                .setFind(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTest.java:                .setAction(ActionOptions.Action.DRAG)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/actionOptions/ActionOptionsIntegrationTest.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTestUpdated.java: * - ActionResult requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTestUpdated.java:            .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTestUpdated.java:        result.setActionConfig(clickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTestUpdated.java:        result.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTestUpdated.java:        result.setActionConfig(typeOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTestUpdated.java:        result.setActionConfig(dragOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTestUpdated.java:        findResult.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTestUpdated.java:            .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTestUpdated.java:        clickResult.setActionConfig(clickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTestUpdated.java:        typeResult.setActionConfig(typeOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTestUpdated.java:        result.setActionConfig(quickFindOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTestUpdated.java: * - ActionResult requires setActionConfig() before perform()
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTestUpdated.java:        snapshot1.setActionSuccess(true);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTestUpdated.java:        snapshot1.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTestUpdated.java:        matches.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTestUpdated.java:        matches.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTestUpdated.java:        matches.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTestUpdated.java:        firstMatches.setActionConfig(firstOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTestUpdated.java:        allMatches.setActionConfig(allOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTestUpdated.java:        matches.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTestUpdated.java:        mockMatches.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTestUpdated.java:        realMatches.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTestUpdated.java:            matches.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/BrobotMockingIntegrationTestUpdated.java:            result.setActionConfig(options);
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:            .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:            .setFind(FindOptions.FindStrategy.ALL)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:            .setAction(ActionOptions.Action.DRAG)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:            .setFind(FindOptions.FindStrategy.FIRST)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:            .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:            .setClickType(ActionOptions.ClickType.MID)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:            .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:            .setFind(FindOptions.FindStrategy.EACH)
../../library-test/src/test/java/io/github/jspinak/brobot/actions/ActionExecutionIntegrationTest.java:            .setAction(MoveOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsCreationTestUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsCreationTestUpdated.java:        transition.setActionDefinition(actionDefinition);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsCreationTestUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsCreationTestUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsCreationTestUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsCreationTestUpdated.java:        transition.setActionDefinition(actionDefinition);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsCreationTestUpdated.java:        transition.setActionDefinition(actionDefinition);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTest.java:        finishOptions.setAction(ActionOptions.Action.HIGHLIGHT);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTest.java:        finishStep.setActionOptions(finishOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTest.java:        finishTransition.setActionDefinition(finishActionDef);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTest.java:        options.setAction(ClickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTest.java:        step.setActionOptions(options);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTest.java:        transition.setActionDefinition(actionDef);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTest.java:        stateTransitions.setActionDefinitionTransitions(actionDefinitionTransitions);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTest.java:        transition.setActionDefinition(actionDef);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/PathFindingComponentTests.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/PathFindingComponentTests.java:        transition.setActionDefinition(actionDefinition);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/ActionDefinitionStateTransitionJsonParserTestUpdated.java:                .setClickType(ClickOptions.Type.RIGHT)
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/ActionDefinitionStateTransitionJsonParserTestUpdated.java:        transition.setActionDefinition(actionDef);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/ActionDefinitionStateTransitionJsonParserTestUpdated.java:                new ClickOptions.Builder().setClickType(ClickOptions.Type.LEFT).build(),
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/ActionDefinitionStateTransitionJsonParserTestUpdated.java:        transition.setActionDefinition(actionDef);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/PathFindingWithActionDefinitionTests.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/PathFindingWithActionDefinitionTests.java:        transition.setActionDefinition(actionDefinition);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/PathFindingComponentTestsUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/PathFindingComponentTestsUpdated.java:        transition.setActionDefinition(actionDefinition);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/PathFindingComponentTestsUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/PathFindingComponentTestsUpdated.java:        transition.setActionDefinition(actionDefinition);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/PathFindingComponentTestsUpdated.java:                .setClickType(ClickOptions.Type.DOUBLE)
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/PathFindingComponentTestsUpdated.java:        transition.setActionDefinition(actionDefinition);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/PathFindingWithActionDefinitionTestsUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/PathFindingWithActionDefinitionTestsUpdated.java:        transition.setActionDefinition(actionDefinition);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/PathFindingWithActionDefinitionTestsUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/PathFindingWithActionDefinitionTestsUpdated.java:        transition.setActionDefinition(actionDefinition);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/ActionDefinitionStateTransitionJsonParserTest.java:        options.setAction(FindOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/ActionDefinitionStateTransitionJsonParserTest.java:        step.setActionOptions(options);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/ActionDefinitionStateTransitionJsonParserTest.java:        transition.setActionDefinition(actionDef);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/ActionDefinitionStateTransitionJsonParserTest.java:        transition.setActionDefinition(actionDef);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/ActionDefinitionStateTransitionJsonParserTest.java:        transition.setActionDefinition(null);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsCreationTest.java:                .setAction(ClickOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsCreationTest.java:        transition.setActionDefinition(actionDefinition);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTestUpdated.java:        finishStep.setActionConfig(highlightOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTestUpdated.java:        finishTransition.setActionDefinition(finishActionDef);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTestUpdated.java:                .setClickType(ClickOptions.Type.RIGHT)
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTestUpdated.java:        step.setActionConfig(clickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTestUpdated.java:        transition.setActionDefinition(actionDef);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTestUpdated.java:        stateTransitions.setActionDefinitionTransitions(actionDefinitionTransitions);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTestUpdated.java:        step.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/manageStates/StateTransitionsJsonParserTestUpdated.java:        transition.setActionDefinition(actionDef);
../../library-test/src/test/java/io/github/jspinak/brobot/test/SimpleAPITest.java:        snapshot.setActionSuccess(true);
../../library-test/src/test/java/io/github/jspinak/brobot/test/SimpleAPITest.java:                .setAction(FindOptions)
../../library-test/src/test/java/io/github/jspinak/brobot/test/APIVerificationTest.java:        snapshot.setActionSuccess(true);
../../library-test/src/test/java/io/github/jspinak/brobot/test/APIVerificationTest.java:        snapshot.setActionSuccess(true);
../../library-test/src/test/java/io/github/jspinak/brobot/test/SimpleAPITestUpdated.java: * - ActionResult.setActionConfig() instead of ActionResult constructor with ActionOptions
../../library-test/src/test/java/io/github/jspinak/brobot/test/SimpleAPITestUpdated.java:        snapshot.setActionSuccess(true);
../../library-test/src/test/java/io/github/jspinak/brobot/test/SimpleAPITestUpdated.java:        snapshot.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/test/SimpleAPITestUpdated.java:        result1.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/test/SimpleAPITestUpdated.java:                .setClickType(ClickOptions.Type.RIGHT)
../../library-test/src/test/java/io/github/jspinak/brobot/test/SimpleAPITestUpdated.java:        result2.setActionConfig(clickOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/test/SimpleAPITestUpdated.java:                .setClickType(ClickOptions.Type.DOUBLE)
../../library-test/src/test/java/io/github/jspinak/brobot/test/SimpleAPITestUpdated.java:        findResult.setActionConfig(findOptions);
../../library-test/src/test/java/io/github/jspinak/brobot/test/SimpleAPITestUpdated.java:                .setClickType(ClickOptions.Type.LEFT)
../../library-test/src/test/java/io/github/jspinak/brobot/test/SimpleAPITestUpdated.java:        clickResult.setActionConfig(clickOptions);
