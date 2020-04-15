package bowling.util;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum RollType {
    FIRST_THROW_FOR_FRAME(1),
    SECOND_THROW_FOR_FRAME(2),
    LAST_ROLL(3),
    STRIKE(10),
    SPARE(0);

    private static final Map<Integer,RollType> lookup
            = new HashMap<Integer,RollType>();

    static {
        for(RollType s : EnumSet.allOf(RollType.class))
            lookup.put(s.getCode(), s);
    }

    private int code;

    private RollType(int code) {
        this.code = code;
    }

    public int getCode() { return code; }

    public static RollType get(int code) {
        return lookup.get(code);
    }
}
