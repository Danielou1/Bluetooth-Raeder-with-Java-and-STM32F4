package com.example.serial;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public enum CDANCommandType {
    LED("LD4", Arrays.asList("ON", "OFF")),
    STATUS("LD4", Arrays.asList("ALL", "LD4")),
    PWM("FAN", Arrays.asList("<0-100>")),
    TEMP("READ", Collections.emptyList()),
    IF("", Arrays.asList("CONDITION")),
    ENDIF("", Collections.emptyList()),
    WHILE("", Arrays.asList("CONDITION")),
    ENDWHILE("", Collections.emptyList());

    private final String target;
    private final List<String> arguments;

    CDANCommandType(String target, List<String> arguments) {
        this.target = target;
        this.arguments = arguments;
    }

    public String getTarget() {
        return target;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public static Optional<CDANCommandType> fromKeyword(String keyword) {
        for (CDANCommandType type : values()) {
            if (type.name().equalsIgnoreCase(keyword)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
