package com.example.serial;

public class CDANGrammarValidator {

   public boolean isValid(String command) {
    if (command == null || command.isEmpty()) return false;

    String[] parts = command.trim().split("\\s+");
    if (parts.length < 2) return false;

    String cmd = parts[0];
    String target = parts[1];
    String arg = parts.length > 2 ? parts[2] : "";

    if (cmd.equalsIgnoreCase("STATUS") && target.equalsIgnoreCase("ALL")) {
        return parts.length == 2;
    }

    return CDANCommandType.fromKeyword(cmd).map(cmdType -> {
        if (!cmdType.getTarget().equalsIgnoreCase(target)) return false;

        if (cmdType.getArguments().isEmpty()) {
            return parts.length == 2;
        }

        if (cmdType == CDANCommandType.PWM) {
            try {
                int val = Integer.parseInt(arg);
                return val >= 0 && val <= 100;
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            return cmdType.getArguments().contains(arg.toUpperCase());
        }

    }).orElse(false);
}

}
