# Border Generator 1.0.0

![image](https://github.com/NeuralCortex/Border_Generator/blob/main/images/border.png)

## Funktionsweise des Programms

Der Border Generator ist ein JavaFX-Projekt, welches erstmalig ermöglicht, auf eine standartisierte Weise, Grenzlinien zu erstellen.</br>
Die Grenzdaten können weltweit unter Verwendung von OpenStreetMap erzeugt und als CSV-Format gespeichert werden.</br>

## How the program works

The Border Generator is a JavaFX project that allows you to create border lines in a standardized way for the first time.</br>
The boundary data can be generated worldwide using OpenStreetMap and saved in CSV format.</br>

## Hinweis

Eine bestehende Internetverbindung ist zwingend erforderlich.

## A notice

An existing internet connection is mandatory.

## Arbeitsschritte

### Erster Tab

1. Rechtsklick auf das Land deren Grenze Sie verwenden wollen. (In der Tabelle GEO-Informationen kann nun State oder Country ausgewählt werden).
2. Erzeugen Sie die gewünschten X-Kilometer Linien (zur Zeit wird 0 bis 100 KM unterstützt).
3. Speichern Sie die Grenzdaten als CSV-Format (die Daten werden im Verzeichnis CSV abgelegt).

### Zweiter Tab

1. Import der ersten Grenzlinie (z.B.: Germany.006.csv).
2. Import der zweiten Grenzlinie (z.B.: Austria.000.csv).
3. Suchen Sie den ersten Schnittpunkt der Grenze (wird durch rote und blaue Linie angezeigt) und drücken Sie 1 auf der Tastatur. 
4. Suchen Sie den zweiten Schnittpunkt der Grenze und drücken Sie 2 auf der Tastatur.
5. Zum Abschluß drücken Sie 3 auf der Tastatur (Die Grenze wird auf den Grenzabschnitt zugeschnitten).
6. Export des Grenzabschnitts als CSV-Datei.
7. Der Vorgang ist nun abgeschlossen.

### Dritter Tab

Dieser Tab dient der Überprüfung der konstruierten Grenzen. Es können CSV-Format Dateien gleichzeitig dargestellt werden.</br>
Durch Rechtsklick auf der Karte können 2 Punkte festgelegt werden, deren Abstand berechnet wird.

## Work steps

### First tab

1. Right click on the country whose border you want to use. (State or Country can now be selected in the GEO Information table).
2. Create the desired X-kilometer lines (currently 0 to 100KM is supported).
3. Save the boundary data in CSV format (the data will be placed in the CSV directory).

### Second tab

1. Import of the first boundary line (e.g.: Germany.006.csv).
2. Import of the second boundary line (e.g.: Austria.000.csv).
3. Find the first intersection of the boundary (indicated by the red and blue lines) and press 1 on the keyboard.
4. Find the second intersection of the boundary and press 2 on the keyboard.
5. Finally, press 3 on the keyboard (The border will be clipped to the border section).
6. Export of the border section as a CSV file.
7. The process is now complete.

### Third tab

This tab is for checking the constructed boundaries. CSV and HCM format files can be displayed simultaneously.</br>
By right-clicking on the map, 2 points can be specified whose distance is calculated.

## Verwendete Technologie

Dieses JavaFX-Projekt wurde erstellt mit der Apache NetBeans 17 IDE [NetBeans 17](https://netbeans.apache.org/).

Folgende Frameworks sollten installiert sein:

- JAVA-SDK [JAVA 19](https://www.oracle.com/java/technologies/javase/jdk19-archive-downloads.html)
- SceneBuilder für GUI-Entwicklung [Gluon SceneBuilder](https://gluonhq.com/products/scene-builder/)
- JAVA-FX-SDK [JavaFX](https://gluonhq.com/products/javafx/)

## Technology used

This JavaFX project was built with the Apache NetBeans 17 IDE [NetBeans 17](https://netbeans.apache.org/).

The following frameworks should be installed:

- JAVA SDK [JAVA 19](https://www.oracle.com/java/technologies/javase/jdk19-archive-downloads.html)
- SceneBuilder for GUI development [Gluon SceneBuilder](https://gluonhq.com/products/scene-builder/)
- JAVA FX SDK [JavaFX](https://gluonhq.com/products/javafx/)
