# geow-interlis-functions
Eine Funktionsbibliothek für INTERLIS 2.4 mit einer Implementierung die das Tool [ilivalidator](https://github.com/claeis/ilivalidator) erweitert.

## Licence
To be defined

## Anwendung
- Das Modell [GeoW_FunctionsExt](src/model/GeoW_FunctionsExt.ili) in das zu verwendende Modell importieren. 

- Die Funktionsbibliothek (.jar-File) kann von GitHub aus dem [aktuellsten Release](https://github.com/GeoWerkstatt/geow-interlis-functions/releases) heruntergeladen werden. Das .jar-File muss dem _ilivalidator_ bekannt gemacht werden.

    - Option `-plugins PLUGINS_DIR` bei der verwendung aus der Konsole. 
    - Einstellung `org.interlis2.validator.pluginfolder` bei der Verwendung einer Konfigurationsdatei
    - In einem Ordner `plugins` auf gleicher Ebene der _ilivalidator_ applikation.


## Contribution
- Neue Funktionen müssen im Modell [GeoW_FunctionsExt](src/model/GeoW_FunctionsExt.ili) erfasst werden.

- Implementationen von Funktionen müssen in einer Klasse mit namen `*IoxPlugin` welche `InterlisFunction` implementiert umgesetzt werden.





