package io.github.jspinak.brobot.primatives.enums;

public enum SpecialStateType {
    UNKNOWN(-1L),
    PREVIOUS(-2L),
    CURRENT(-3L),
    EXPECTED(-4L),
    NULL(-5L);

    private final Long id;

    SpecialStateType(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public static SpecialStateType fromId(Long id) {
        for (SpecialStateType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No SpecialStateType with id " + id);
    }

    public static boolean isSpecialStateId(Long id) {
        for (SpecialStateType type : values()) {
            if (type.id.equals(id)) {
                return true;
            }
        }
        return false;
    }
}
