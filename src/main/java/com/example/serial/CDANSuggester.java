package com.example.serial;

import java.util.*;

public class CDANSuggester {

   public List<String> suggestNext(String partialCommand) {
    String[] parts = partialCommand.trim().split("\\s+");

    if (parts.length == 0 || parts[0].isEmpty()) {
        return getAllCommandNames();
    }

    if (parts.length == 1) {
        return filterStartWith(getAllCommandNames(), parts[0]);
    }

    String cmd = parts[0].toUpperCase();

    if (cmd.equals("IF") || cmd.equals("WHILE")) {
        return List.of("TEMP > 30", "PWM == 0", "HUM < 50");
    }

    Optional<CDANCommandType> typeOpt = CDANCommandType.fromKeyword(cmd);
    if (typeOpt.isEmpty()) return List.of();

    CDANCommandType type = typeOpt.get();

    if (parts.length == 2) {
        return filterStartWith(List.of(type.getTarget()), parts[1]);
    }

    if (parts.length == 3) {
        return filterStartWith(type.getArguments(), parts[2]);
    }

    return List.of();
}

    private List<String> getAllCommandNames() {
        return Arrays.stream(CDANCommandType.values())
                .map(Enum::name)
                .toList();
    }

   private List<String> filterStartWith(List<String> options, String input) {
    final String finalInput = input.toUpperCase();
    return options.stream()
            .filter(opt -> opt.toUpperCase().startsWith(finalInput))
            .toList();
    }   

}
