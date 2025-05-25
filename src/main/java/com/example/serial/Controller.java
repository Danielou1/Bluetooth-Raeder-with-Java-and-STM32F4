package com.example.serial;

import com.fazecast.jSerialComm.SerialPort;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

public class Controller {

    @FXML
    private ComboBox<String> portComboBox;

    @FXML
    private ComboBox<Integer> baudRateComboBox;

    @FXML
    private TextArea outputArea;

    private final SerialService serialService = new SerialService();

    private Thread readThread;

    @FXML
    public void initialize() {
        System.out.println("initialize() called");
        refreshPorts(); // Remplit portComboBox à l'init

        baudRateComboBox.setItems(FXCollections.observableArrayList(
                9600, 19200, 38400, 57600, 115200, 230400, 460800
        ));
        baudRateComboBox.getSelectionModel().select(Integer.valueOf(9600));
    }

    @FXML
    public void refreshPorts() {
        portComboBox.getItems().clear();
        SerialPort[] ports = serialService.getAvailablePorts();

        System.out.println("Availables Ports:");
        for (SerialPort port : ports) {
            System.out.println(" - " + port.getSystemPortName());
            portComboBox.getItems().add(port.getSystemPortName());
        }

        Platform.runLater(() -> outputArea.appendText("Ports Liste aktualisiert.\n"));
    }

    @FXML
    public void onOpenPort() {
        String portName = portComboBox.getValue();
        Integer baudRate = baudRateComboBox.getValue();

        if (portName == null || portName.isEmpty()) {
            outputArea.appendText("Bitte einen Port auswählen.\n");
            return;
        }
        if (baudRate == null) {
            outputArea.appendText("Bitte eine Baudrate auswählen.\n");
            return;
        }

        if (serialService.isPortOpen()) {
            outputArea.appendText("Ein Port ist bereits geöffnet. Bitte zuerst schließen.\n");
            return;
        }

        boolean opened = serialService.openPort(portName, baudRate);

        if (opened) {
            outputArea.appendText("Port geöffnet: " + portName + " mit Baudrate " + baudRate + "\n");

            readThread = new Thread(() -> {
                while (serialService.isPortOpen() && !Thread.currentThread().isInterrupted()) {
                    String data = serialService.readData();
                    if (!data.isEmpty()) {
                        Platform.runLater(() -> outputArea.appendText(data));
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                Platform.runLater(() -> outputArea.appendText("\nPort wurde geschlossen.\n"));
            });
            readThread.setDaemon(true);
            readThread.start();
        } else {
            outputArea.appendText("Konnte Port " + portName + " nicht öffnen.\n");
        }
    }

    @FXML
    public void onClosePort() {
        if (readThread != null && readThread.isAlive()) {
            readThread.interrupt();
            try {
                readThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        serialService.closePort();
        outputArea.appendText("Port geschlossen.\n");
    }
}
