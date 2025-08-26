package com.example.serial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

class CommandInterpreterTest {

    private final CommandInterpreter interpreter = new CommandInterpreter();

    @Test
    void testToUartFrameSimple() {
        String command = "LED ON";
        byte[] expected = "LED ON\r\n".getBytes(StandardCharsets.US_ASCII);
        assertArrayEquals(expected, interpreter.toUartFrame(command));
    }

    @Test
    void testToUartFrameWithExtraWhitespace() {
        String command = "  TEMP READ  ";
        byte[] expected = "TEMP READ\r\n".getBytes(StandardCharsets.US_ASCII);
        assertArrayEquals(expected, interpreter.toUartFrame(command));
    }

    @Test
    void testToUartFrameNullInput() {
        byte[] expected = new byte[0];
        assertArrayEquals(expected, interpreter.toUartFrame(null));
    }

    @Test
    void testToUartFrameWithProtocol() {
        String command = "DATA";
        // Expected: STX (0x02) + 'D' 'A' 'T' 'A' + ETX (0x03) + checksum
        // Checksum = 68 + 65 + 84 + 65 = 282. 282 & 0xFF = 26.
        byte[] expected = {0x02, 'D', 'A', 'T', 'A', 0x03, 26};
        assertArrayEquals(expected, interpreter.toUartFrameWithProtocol(command));
    }

    @Test
    void testToUartFrameWithProtocolEmptyCommand() {
        String command = "";
        // Expected: STX (0x02) + ETX (0x03) + checksum (0)
        byte[] expected = {0x02, 0x03, 0};
        assertArrayEquals(expected, interpreter.toUartFrameWithProtocol(command));
    }
}
