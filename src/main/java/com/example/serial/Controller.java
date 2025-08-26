// Controller.java
package com.example.serial;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Controller {

    @FXML
    private ComboBox<String> portComboBox;

    @FXML
    private ComboBox<Integer> baudRateComboBox;

    @FXML
    private TextArea outputArea;

    @FXML
    private TextArea commandField;

    @FXML
    private ListView<String> suggestionList;

    @FXML
    private LineChart<Number, Number> tempHumChart;

    private final SerialService serialService = new SerialService();
    private final ThresholdChecker checker = new ThresholdChecker();
    private final CDANGrammarValidator validator = new CDANGrammarValidator();
    private final CDANSuggester suggester = new CDANSuggester();
    private final CommandInterpreter interpreter = new CommandInterpreter();

    private Thread readThread;
    private final XYChart.Series<Number, Number> tempSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> humSeries = new XYChart.Series<>();
    private int xCounter = 0;

    @FXML
    public void initialize() {
        refreshPorts();
        baudRateComboBox.setItems(FXCollections.observableArrayList(9600, 19200, 38400, 57600, 115200));
        baudRateComboBox.getSelectionModel().select(Integer.valueOf(9600));

        tempSeries.setName("Temperatur (\u00b0C)");
        humSeries.setName("Luftfeuchtigkeit (%)");

        tempHumChart.setTitle("Temperatur & Luftfeuchtigkeit");
        tempHumChart.getData().addAll(tempSeries, humSeries);

        commandField.textProperty().addListener((obs, oldText, newText) -> {
            List<String> suggestions = suggester.suggestNext(newText);
            if (!suggestions.isEmpty()) {
                suggestionList.setItems(FXCollections.observableArrayList(suggestions));
                suggestionList.setVisible(true);
            } else {
                suggestionList.setVisible(false);
            }
        });

        suggestionList.setOnMouseClicked(event -> {
            String selected = suggestionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String[] parts = commandField.getText().trim().split("\\s+");
                if (parts.length == 1) {
                    commandField.setText(selected + " ");
                } else if (parts.length == 2) {
                    commandField.setText(parts[0] + " " + selected + " ");
                } else if (parts.length == 3) {
                    commandField.setText(parts[0] + " " + parts[1] + " " + selected);
                }
                suggestionList.setVisible(false);
            }
        });
    }

    @FXML
    public void refreshPorts() {
        portComboBox.getItems().clear();
        SerialPort[] ports = serialService.getAvailablePorts();
        for (SerialPort port : ports) {
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
            outputArea.appendText("Ein Port ist bereits geöffnet.\n");
            return;
        }

        tempSeries.getData().clear();
        humSeries.getData().clear();
        xCounter = 0;

        boolean opened = serialService.openPort(portName, baudRate);

        if (opened) {
            outputArea.appendText("Port geöffnet: " + portName + " mit Baudrate " + baudRate + "\n");
            readThread = new Thread(() -> {
                while (serialService.isPortOpen() && !Thread.currentThread().isInterrupted()) {
                    String data = serialService.readData();
                    if (!data.isEmpty()) {
                        Platform.runLater(() -> {
                            outputArea.appendText(data);
                            parseAndPlotSensorData(data);
                        });
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                Platform.runLater(() -> outputArea.appendText("\nPort wurde geschlossen.\n"));
            });
            readThread.setDaemon(true);
            readThread.start();
        } else {
            outputArea.appendText("Konnte Port nicht öffnen.\n");
        }
    }

    private void parseAndPlotSensorData(String data) {
        try {
            if (data.contains("Temp:") && data.contains("Hum:")) {
                String[] parts = data.split(",");
                double temp = 0, hum = 0;

                for (String part : parts) {
                    if (part.contains("Temp:")) {
                        String tempStr = part.replace("Temp:", "").replace("C", "").trim();
                        temp = Double.parseDouble(tempStr);
                    } else if (part.contains("Hum:")) {
                        String humStr = part.replace("Hum:", "").replace("%", "").trim();
                        hum = Double.parseDouble(humStr);
                    }
                }

                tempSeries.getData().add(new XYChart.Data<>(xCounter, temp));
                humSeries.getData().add(new XYChart.Data<>(xCounter, hum));
                xCounter++;

                if (tempSeries.getData().size() > 1000) tempSeries.getData().remove(0);
                if (humSeries.getData().size() > 1000) humSeries.getData().remove(0);

                checkThresholdAndAlert("temperature", temp);
                checkThresholdAndAlert("humidity", hum);
            }
        } catch (Exception e) {
            outputArea.appendText("[Fehler beim Parsen der Sensordaten]\n");
        }
    }

    private void checkThresholdAndAlert(String sensorType, double value) {
        if (checker.isValueOutOfThreshold(sensorType, (float) value)) {
            outputArea.appendText("[ALARM] " + sensorType + " außerhalb des Bereichs: " + value + "\n");
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

    @FXML
public void onSendCommand() {
    if (!serialService.isPortOpen()) {
        outputArea.appendText("[Fehler] Kein geöffneter Port.\n");
        return;
    }

    String command = commandField.getText().trim();
    if (command.isEmpty()) {
        outputArea.appendText("[Warnung] Leere Eingabe.\n");
        return;
    }

    // Valider selon CDAN-Grammatik
    CDANGrammarValidator validator = new CDANGrammarValidator();
    if (!validator.isValid(command)) {
        outputArea.appendText("[Fehler] Ungültige CDAN-Kommando: '" + command + "'\n");
        return;
    }

    // Ajout du délimiteur standard UNIX '\n' uniquement
    String commandWithNewline = command + "\n";
    byte[] data = commandWithNewline.getBytes(StandardCharsets.US_ASCII);

    try {
        SerialPort port = serialService.getSerialPort();
        port.writeBytes(data, data.length);
        outputArea.appendText("[Gesendet] " + command + "\n");
        commandField.clear();
    } catch (Exception e) {
        outputArea.appendText("[Fehler beim Senden] " + e.getMessage() + "\n");
    }
}


    @FXML
    public void openThresholdDialog() {
         ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("Temperatur", Arrays.asList("Temperatur", "Luftfeuchtigkeit"));
        choiceDialog.setTitle("Schwelle setzen");
        choiceDialog.setHeaderText("Wähle eine Messgröße");
        choiceDialog.setContentText("Welche Größe willst du einstellen?");
        Optional<String> result = choiceDialog.showAndWait();

        result.ifPresent(choice -> {
            String typeKey = switch (choice) {
                case "Temperatur" -> "temperature";
                case "Luftfeuchtigkeit" -> "humidity";
                default -> null;
            };
            if (typeKey == null) return;

            TextInputDialog minDialog = new TextInputDialog();
            minDialog.setTitle("Minimale Schwelle");
            minDialog.setHeaderText("Gib die minimale Schwelle für " + choice + " ein:");
            Optional<String> minResult = minDialog.showAndWait();

            minResult.ifPresent(minStr -> {
                try {
                    float min = Float.parseFloat(minStr);

                    TextInputDialog maxDialog = new TextInputDialog();
                    maxDialog.setTitle("Maximale Schwelle");
                    maxDialog.setHeaderText("Gib die maximale Schwelle für " + choice + " ein:");
                    Optional<String> maxResult = maxDialog.showAndWait();

                    maxResult.ifPresent(maxStr -> {
                        try {
                            float max = Float.parseFloat(maxStr);
                            checker.setThreshold(typeKey, min, max);
                            outputArea.appendText("[Info] Neue Schwelle gesetzt für " + choice + ": min=" + min + ", max=" + max + "\n");
                        } catch (NumberFormatException e) {
                            outputArea.appendText("[Fehler] Ungültige Eingabe für maximale Schwelle.\n");
                        }
                    });
                } catch (NumberFormatException e) {
                    outputArea.appendText("[Fehler] Ungültige Eingabe für minimale Schwelle.\n");
                }
            });
        });
    }
}
