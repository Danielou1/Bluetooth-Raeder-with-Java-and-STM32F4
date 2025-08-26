package com.example.serial;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import org.controlsfx.control.Notifications;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class Controller {

    // FXML Components
    @FXML private ComboBox<String> portComboBox;
    @FXML private ComboBox<Integer> baudRateComboBox;
    @FXML private TextArea outputArea;
    @FXML private TextArea commandField;
    @FXML private ListView<String> suggestionList;
    @FXML private LineChart<Number, Number> tempHumChart;
    @FXML private Label statusLabel;
    @FXML private Button openPortButton;
    @FXML private Button closePortButton;
    @FXML private Button runButton;

    // Services and Utilities
    private final SerialService serialService = new SerialService();
    private final ThresholdChecker checker = new ThresholdChecker();
    private final CDANGrammarValidator validator = new CDANGrammarValidator();
    private final CDANSuggester suggester = new CDANSuggester();
    private final CommandInterpreter interpreter = new CommandInterpreter();

    // State
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

        setupCommandSuggestions();
        updateStatus(false, null); // Set initial UI state
    }

    private void setupCommandSuggestions() {
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
                if (parts.length <= 1) {
                    commandField.setText(selected + " ");
                } else if (parts.length == 2) {
                    commandField.setText(parts[0] + " " + selected + " ");
                } else if (parts.length == 3) {
                    commandField.setText(parts[0] + " " + parts[1] + " " + selected);
                }
                commandField.requestFocus();
                commandField.end();
                suggestionList.setVisible(false);
            }
        });
    }

    @FXML
    public void refreshPorts() {
        String selectedPort = portComboBox.getValue();
        portComboBox.getItems().clear();
        SerialPort[] ports = serialService.getAvailablePorts();
        for (SerialPort port : ports) {
            portComboBox.getItems().add(port.getSystemPortName());
        }
        portComboBox.setValue(selectedPort);
        outputArea.appendText("Ports Liste aktualisiert.\n");
    }

    @FXML
    public void onOpenPort() {
        String portName = portComboBox.getValue();
        Integer baudRate = baudRateComboBox.getValue();

        if (portName == null || portName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Fehler", "Bitte einen Port auswählen.");
            return;
        }
        if (baudRate == null) {
            showAlert(Alert.AlertType.ERROR, "Fehler", "Bitte eine Baudrate auswählen.");
            return;
        }

        tempSeries.getData().clear();
        humSeries.getData().clear();
        xCounter = 0;

        if (serialService.openPort(portName, baudRate)) {
            updateStatus(true, portName);
            startReadingThread();
        } else {
            showAlert(Alert.AlertType.ERROR, "Verbindungsfehler", "Konnte Port nicht öffnen: " + portName);
            updateStatus(false, null);
        }
    }

    private void startReadingThread() {
        readThread = new Thread(() -> {
            while (serialService.isPortOpen() && !Thread.currentThread().isInterrupted()) {
                try {
                    String data = serialService.readData();
                    if (!data.isEmpty()) {
                        Platform.runLater(() -> {
                            outputArea.appendText(data);
                            parseAndPlotSensorData(data);
                        });
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Preserve interrupt status
                    break;
                } catch (Exception e) {
                    // Handle unexpected errors during read
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lesefehler", "Ein Fehler ist beim Lesen der Daten aufgetreten."));
                    break;
                }
            }
            Platform.runLater(() -> updateStatus(false, null));
        });
        readThread.setDaemon(true);
        readThread.start();
    }

    @FXML
    public void onClosePort() {
        if (readThread != null && readThread.isAlive()) {
            readThread.interrupt();
        }
        serialService.closePort();
        // The updateStatus is called automatically when the reading thread terminates
    }

    @FXML
    public void onSendCommand() {
        if (!serialService.isPortOpen()) {
            showAlert(Alert.AlertType.ERROR, "Fehler", "Kein geöffneter Port. Bitte verbinden Sie sich zuerst.");
            return;
        }

        String command = commandField.getText().trim();
        if (command.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warnung", "Das Eingabefeld ist leer.");
            return;
        }

        if (!validator.isValid(command)) {
            showAlert(Alert.AlertType.ERROR, "Ungültiges Kommando", "Das Kommando '" + command + "' ist ungültig.");
            return;
        }

        String commandWithNewline = command + "\n";
        byte[] data = commandWithNewline.getBytes(StandardCharsets.US_ASCII);

        try {
            SerialPort port = serialService.getSerialPort();
            port.writeBytes(data, data.length);
            outputArea.appendText("[Gesendet] " + command + "\n");
            commandField.clear();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Sendefehler", "Fehler beim Senden der Daten: " + e.getMessage());
        }
    }

    private void updateStatus(boolean isConnected, String portName) {
        if (isConnected) {
            statusLabel.setText("Verbunden mit " + portName);
            statusLabel.setTextFill(Color.GREEN);
        } else {
            statusLabel.setText("Déconnecté");
            statusLabel.setTextFill(Color.RED);
        }

        // Disable/Enable controls based on connection status
        portComboBox.setDisable(isConnected);
        baudRateComboBox.setDisable(isConnected);
        openPortButton.setDisable(isConnected);
        closePortButton.setDisable(!isConnected);
        runButton.setDisable(!isConnected);
        commandField.setDisable(!isConnected);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

                final double finalTemp = temp;
                final double finalHum = hum;
                Platform.runLater(() -> {
                    tempSeries.getData().add(new XYChart.Data<>(xCounter, finalTemp));
                    humSeries.getData().add(new XYChart.Data<>(xCounter, finalHum));
                    xCounter++;

                    if (tempSeries.getData().size() > 1000) tempSeries.getData().remove(0);
                    if (humSeries.getData().size() > 1000) humSeries.getData().remove(0);

                    checkThresholdAndAlert("temperature", finalTemp);
                    checkThresholdAndAlert("humidity", finalHum);
                });
            }
        } catch (Exception e) {
            Platform.runLater(() -> outputArea.appendText("[Fehler beim Parsen der Sensordaten]\n"));
        }
    }

    private void checkThresholdAndAlert(String sensorType, double value) {
        if (checker.isValueOutOfThreshold(sensorType, (float) value)) {
            String sensorName = sensorType.equals("temperature") ? "Temperatur" : "Luftfeuchtigkeit";
            String text = String.format("%s außerhalb des Bereichs: %.2f", sensorName, value);

            Notifications.create()
                    .title("Schwellenwert-Alarm")
                    .text(text)
                    .showWarning();
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
                            showAlert(Alert.AlertType.ERROR, "Fehler", "Ungültige Eingabe für maximale Schwelle.");
                        }
                    });
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Fehler", "Ungültige Eingabe für minimale Schwelle.");
                }
            });
        });
    }
}
