package io.github.jspinak.brobot.test.jackson;

/**
 * Provides JSON test fixtures for common Brobot objects. These are properly formatted JSON strings
 * that can be used in deserialization tests.
 */
public class JsonTestFixtures {

    public static final String VALID_ACTION_RECORD =
            """
        {
            "actionConfig": {
                "@class": "io.github.jspinak.brobot.action.basic.find.PatternFindOptions",
                "similarity": 0.8
            },
            "matchList": [],
            "text": "",
            "duration": 100.0,
            "timeStamp": "2024-01-15T10:30:00",
            "actionSuccess": true,
            "resultSuccess": false,
            "stateName": "TestState",
            "stateId": 1
        }
        """;

    public static final String VALID_MATCH =
            """
        {
            "name": "test-match",
            "region": {
                "x": 10,
                "y": 20,
                "w": 100,
                "h": 50
            },
            "score": 0.95,
            "text": "",
            "timeStamp": "2024-01-15T10:30:00",
            "stateObjectId": 0
        }
        """;

    public static final String VALID_PATTERN =
            """
        {
            "name": "test-pattern",
            "imgpath": "images/test-pattern.png",
            "url": "http://example.com/test-pattern",
            "fixed": false,
            "dynamic": false,
            "index": 0,
            "targetPosition": {
                "x": 0.5,
                "y": 0.5
            },
            "targetOffset": {
                "x": 0,
                "y": 0
            },
            "searchRegions": {
                "regions": []
            },
            "anchors": {
                "anchorList": []
            },
            "matchHistory": {
                "actionRecords": []
            }
        }
        """;

    public static final String VALID_STATE_IMAGE =
            """
        {
            "name": "test-state-image",
            "id": 1000,
            "ownerStateName": "TestState",
            "ownerStateId": 1,
            "timesActedOn": 0,
            "staysVisible": true,
            "probabilityExists": 100,
            "patterns": [
                {
                    "name": "pattern-1",
                    "imgpath": "images/pattern-1.png",
                    "fixed": false,
                    "dynamic": false
                }
            ],
            "sharedPatterns": [],
            "snapshots": []
        }
        """;

    public static final String VALID_STATE_REGION =
            """
        {
            "name": "test-state-region",
            "id": 1001,
            "ownerStateName": "TestState",
            "ownerStateId": 1,
            "timesActedOn": 0,
            "staysVisible": true,
            "probabilityExists": 100,
            "searchRegion": {
                "x": 0,
                "y": 0,
                "w": 800,
                "h": 600
            }
        }
        """;

    public static final String VALID_STATE_LOCATION =
            """
        {
            "name": "test-state-location",
            "id": 1002,
            "ownerStateName": "TestState",
            "ownerStateId": 1,
            "timesActedOn": 0,
            "staysVisible": true,
            "probabilityExists": 100,
            "location": {
                "x": 400,
                "y": 300
            },
            "position": {
                "x": 0.5,
                "y": 0.5
            },
            "anchor": "CENTER"
        }
        """;

    public static final String VALID_STATE_STRING =
            """
        {
            "name": "test-state-string",
            "id": 1003,
            "ownerStateName": "TestState",
            "ownerStateId": 1,
            "timesActedOn": 0,
            "staysVisible": true,
            "probabilityExists": 100,
            "string": "Test String Value"
        }
        """;

    public static final String VALID_OBJECT_COLLECTION =
            """
        {
            "stateLocations": [
                {
                    "name": "location-1",
                    "location": {
                        "x": 100,
                        "y": 200
                    }
                }
            ],
            "stateImages": [
                {
                    "name": "image-1",
                    "patterns": []
                }
            ],
            "stateRegions": [
                {
                    "name": "region-1",
                    "searchRegion": {
                        "x": 0,
                        "y": 0,
                        "w": 800,
                        "h": 600
                    }
                }
            ],
            "stateStrings": [
                {
                    "name": "string-1",
                    "string": "test value"
                }
            ],
            "matches": [],
            "scenes": []
        }
        """;

    public static final String VALID_ACTION_RESULT =
            """
        {
            "actionConfig": {
                "@class": "io.github.jspinak.brobot.action.basic.find.PatternFindOptions",
                "similarity": 0.8
            },
            "matchList": [
                {
                    "name": "match-1",
                    "score": 0.95,
                    "region": {
                        "x": 10,
                        "y": 10,
                        "w": 50,
                        "h": 50
                    }
                }
            ],
            "success": true
        }
        """;

    public static final String VALID_PATTERN_FIND_OPTIONS =
            """
        {
            "@class": "io.github.jspinak.brobot.action.basic.find.PatternFindOptions",
            "similarity": 0.8,
            "waitUntilFound": 1.0,
            "waitUntilVanished": 1.0,
            "pauseAfterFind": 0.5,
            "pauseBeforeFind": 0.5,
            "maxWaitTime": 10.0
        }
        """;

    public static final String VALID_SEARCH_REGIONS =
            """
        {
            "regions": [
                {
                    "x": 0,
                    "y": 0,
                    "w": 800,
                    "h": 600
                },
                {
                    "x": 100,
                    "y": 100,
                    "w": 600,
                    "h": 400
                }
            ],
            "fixedRegion": {
                "x": 200,
                "y": 200,
                "w": 400,
                "h": 300
            }
        }
        """;

    public static final String VALID_ANCHORS =
            """
        {
            "anchorList": [
                {
                    "anchorType": "TOP_LEFT",
                    "position": {
                        "x": 0,
                        "y": 0
                    }
                },
                {
                    "anchorType": "CENTER",
                    "position": {
                        "x": 0.5,
                        "y": 0.5
                    }
                }
            ]
        }
        """;

    public static final String VALID_STATE =
            """
        {
            "name": "TestState",
            "stateImages": [],
            "stateRegions": [],
            "stateLocations": [],
            "stateStrings": [],
            "blocking": false,
            "pathScore": 1.0
        }
        """;

    /**
     * Get a JSON fixture with placeholders replaced.
     *
     * @param fixture The fixture template
     * @param replacements Key-value pairs for replacement (key1, value1, key2, value2, ...)
     * @return The fixture with replacements applied
     */
    public static String customize(String fixture, String... replacements) {
        String result = fixture;
        for (int i = 0; i < replacements.length - 1; i += 2) {
            result = result.replace("${" + replacements[i] + "}", replacements[i + 1]);
        }
        return result;
    }

    /** Template for creating custom patterns. */
    public static final String PATTERN_TEMPLATE =
            """
        {
            "name": "${name}",
            "imgpath": "${imgpath}",
            "fixed": ${fixed},
            "dynamic": ${dynamic}
        }
        """;

    /** Template for creating custom matches. */
    public static final String MATCH_TEMPLATE =
            """
        {
            "name": "${name}",
            "score": ${score},
            "region": {
                "x": ${x},
                "y": ${y},
                "w": ${w},
                "h": ${h}
            }
        }
        """;
}
