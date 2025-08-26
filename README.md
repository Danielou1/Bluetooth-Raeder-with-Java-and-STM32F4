# 📡 Bluetooth Serial Reader (JavaFX)

Diese JavaFX-Anwendung ermöglicht das Auslesen von Daten, die über ein Bluetooth-Modul (z. B. HC-05) per serieller Schnittstelle (COM-Port) an den Computer gesendet werden.  
Die Kommunikation erfolgt mithilfe der Bibliothek [jSerialComm](https://fazecast.github.io/jSerialComm/).

---

## ✨ Funktionen

- ✅ Dynamische Anzeige der verfügbaren seriellen Ports
- ✅ Auswahl der Baudrate zur Datenübertragung
- ✅ Echtzeit-Auslesen eingehender Daten
- ✅ Moderne JavaFX-Benutzeroberfläche
- ✅ Manuelles Öffnen und Schließen des Ports
- ✅ Manueller Refresh der verfügbaren Ports

---

## 📦 Verwendete Technologien

- Java 11 oder höher
- JavaFX
- jSerialComm (für serielle Kommunikation)
- Maven (Abhängigkeitsmanagement)

---

## 📸 Screenshot

![screenshot](docs/screenshot.png) <!-- Ersetze diesen Pfad ggf. durch deinen Screenshot -->

---

## 🚀 Schnellstart

### 1. Repository klonen

```bash
git clone https://github.com/dein-benutzername/bluetooth-serial-reader.git
cd bluetooth-serial-reader
```

### 2. Anwendung starten

Mit Maven:

```bash
mvn javafx:run
```

> ℹ️ Stelle sicher, dass JavaFX korrekt in deiner `pom.xml` eingebunden ist.

---

## 🛠️ Bedienung

1. Verbinde dein Bluetooth-Modul (z. B. HC-05) mit deinem Computer.
2. Starte die Anwendung.
3. Wähle einen verfügbaren seriellen Port (z. B. `COM3`, `ttyUSB0`) aus der Dropdown-Liste.
4. Wähle die gewünschte Baudrate (z. B. `9600`, `115200`) aus.
5. Klicke auf **Open Port**, um die Datenübertragung zu starten.
6. Klicke auf **Close Port**, um die Verbindung wieder zu schließen.
7. Verwende **Refresh**, um die Liste der verfügbaren Ports zu aktualisieren.

---

## CDAN Command Protocol

This application includes a command interpreter for a simple protocol named `CDAN`. You can send these commands through the serial port to control the device.

| Command | Target | Arguments | Description |
|---|---|---|---|
| `LED` | `LD4` | `ON` / `OFF` | Turns the LED on or off. |
| `STATUS`| `ALL` | - | Requests the status of all components. |
| `PWM` | `FAN` | `<0-100>` | Sets the fan speed (Pulse Width Modulation) from 0 to 100%. |
| `TEMP` | `READ` | - | Requests a temperature reading. |
| `IF` | - | `CONDITION` | Starts a conditional block (e.g., `IF TEMP > 30`). *(Control flow not fully implemented in this version)* |
| `ENDIF` | - | - | Ends a conditional block. |
| `WHILE` | - | `CONDITION` | Starts a loop block. *(Control flow not fully implemented in this version)* |
| `ENDWHILE`| - | - | Ends a loop block. |

**Example:** Sending `LED LD4 ON` will turn on the LD4 light.

---

## 🧩 Wichtige Maven-Abhängigkeiten


**jSerialComm:**

```xml
<dependency>
    <groupId>com.fazecast</groupId>
    <artifactId>jSerialComm</artifactId>
    <version>2.9.2</version>
</dependency>
```

**JavaFX (OpenJFX):**

```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>20</version> <!-- oder eine andere kompatible Version -->
</dependency>
```

---

## 📂 Projektstruktur

```
src/
├── main/
│   ├── java/com/example/serial/
│   │   ├── App.java
│   │   ├── Controller.java
│   │   └── SerialService.java
│   └── resources/com/example/serial/
│       └── main-view.fxml
```

---

## 📌 Hinweise

- Kompatibel mit Windows, Linux und macOS.
- Dein Bluetooth-Modul muss vor der Nutzung mit dem System gekoppelt sein.
- Nach Anschluss eines neuen Geräts sollte die Portliste aktualisiert werden.

---

## 📄 Lizenz

Dieses Projekt steht unter der **MIT-Lizenz**. Details siehe [LICENSE](LICENSE).

---

## 🤝 Beitrag leisten

Beiträge sind herzlich willkommen!

1. Forke das Repository
2. Erstelle einen neuen Branch (`git checkout -b feature/neues-feature`)
3. Commite deine Änderungen
4. Erstelle einen Pull Request

---

## 👨‍💻 Autor

**Danielou Mounsande**  
Student im Bereich Embedded Systems, begeistert von der Integration zwischen Software und Hardware.

📫 Kontakt über [LinkedIn](https://linkedin.com) oder per [E-Mail](mounsandedaniel@gmail.com)

---

> Dieses Projekt ist Teil eines Lernprojekts im Rahmen des Studiums an der Technischen Hochschule Mittelhessen (THM).