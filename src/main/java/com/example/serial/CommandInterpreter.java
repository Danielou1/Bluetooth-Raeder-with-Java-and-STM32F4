package com.example.serial;

import java.nio.charset.StandardCharsets;

/**
 * Interpréteur de commandes texte en trames UART (bytes) prêtes à envoyer.
 */
public class CommandInterpreter {

    /**
     * Transforme une commande texte en une trame UART.
     * Par défaut, la trame ajoute un retour chariot + saut de ligne à la commande.
     * 
     * @param command La commande texte, par ex. "LED ON"
     * @return tableau de bytes à envoyer sur le port série
     */
    public byte[] toUartFrame(String command) {
    if (command == null) {
        return new byte[0];
    }

    String cleanedCommand = command.trim();
    String framedCommand = cleanedCommand + "\r\n";

    return framedCommand.getBytes(StandardCharsets.US_ASCII); // ASCII, pas UTF-8
}


    /**
     * Exemple d'extension : encoder une commande avec un protocole simple
     * (exemple : ajouter un header, checksum, etc.)
     * 
     * @param command La commande texte simple
     * @return trame UART avec protocole
     */
    public byte[] toUartFrameWithProtocol(String command) {
        if (command == null) {
            return new byte[0];
        }

        // Exemple protocole : [STX=0x02] + commande en ASCII + [ETX=0x03] + checksum simple
        byte STX = 0x02;
        byte ETX = 0x03;

        byte[] cmdBytes = command.trim().getBytes();

        // Calcul simple checksum : somme modulo 256 des bytes commande
        int checksum = 0;
        for (byte b : cmdBytes) {
            checksum += b & 0xFF;
        }
        checksum = checksum & 0xFF;

        // Construire la trame : STX + cmd + ETX + checksum
        byte[] frame = new byte[cmdBytes.length + 3];
        frame[0] = STX;
        System.arraycopy(cmdBytes, 0, frame, 1, cmdBytes.length);
        frame[cmdBytes.length + 1] = ETX;
        frame[cmdBytes.length + 2] = (byte) checksum;

        return frame;
    }
}
