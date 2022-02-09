# geow-interlis-functions
Eine Funktionsbibliothek für INTERLIS 2.4 mit einer Implementierung die das Tool [ilivalidator](https://github.com/claeis/ilivalidator) erweitert.

## Licence
LGPL-2.1 License See [LICENSE.md](LICENSE.md)

## Anwendung
- Das Modell [GeoW_FunctionsExt](src/model/GeoW_FunctionsExt.ili) in das zu verwendende Modell importieren. 

- Es muss sichergestellt werden, dass der ilivalidator das Modell in einem Repository finden kann. Hier kann GitHub z.B. direkt eingebunden werden. 
```
https://raw.githubusercontent.com/GeoWerkstatt/geow-interlis-functions/master/src/model/
```

- **Jar-File:** Die Funktionsbibliothek (.jar-File) kann von GitHub aus dem [aktuellsten Release](https://github.com/GeoWerkstatt/geow-interlis-functions/releases/latest) heruntergeladen werden. Das .jar-File muss dem _ilivalidator_ bekannt gemacht werden.

    - Option `-plugins PLUGINS_DIR` bei der verwendung aus der Konsole. 
    - Einstellung `org.interlis2.validator.pluginfolder` bei der Verwendung einer Konfigurationsdatei
    - In einem Ordner `plugins` auf gleicher Ebene der _ilivalidator_ applikation.

- **Maven:** Das Projekt steht für integrierte Umgebungen auch als [Maven-Paket](https://github.com/GeoWerkstatt/geow-interlis-functions/packages/) bereit. 


## Contribution
- Neue Funktionen müssen im Modell [GeoW_FunctionsExt](src/model/GeoW_FunctionsExt.ili) erfasst werden.

- Eine Anpassung von Modellen unter [src/model](src/model) bedingt ein update von [ilimodels.xml](src/model/ilimodels.xml). Das File kann mit `ili2c.jar` generiert werden:
```
java -cp ili2c.jar ch.interlis.ili2c.MakeIliModelsXml2 .\src\model
```

- Implementationen von Funktionen müssen in einer Klasse mit namen `*IoxPlugin` welche `InterlisFunction` implementiert umgesetzt werden.






