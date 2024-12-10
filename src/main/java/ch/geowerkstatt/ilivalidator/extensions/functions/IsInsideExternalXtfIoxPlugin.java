package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxException;
import ch.interlis.iox_j.utility.IoxUtility;
import ch.interlis.iox_j.validator.Value;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class IsInsideExternalXtfIoxPlugin extends BaseIsInsideFunction {
    private static File dataDirectory;
    private static Map<String, Set<URL>> modelFilesFromDataDirectory;
    private static Map<String, Set<URL>> modelFilesFromJars;
    private static final Map<ValidAreaKey, Geometry> VALID_AREA_CACHE = new HashMap<>();
    private static final String QUALIFIED_ILI_NAME = "GeoW_FunctionsExt.IsInsideExternalXtf";

    @Override
    public String getQualifiedIliName() {
        return QUALIFIED_ILI_NAME;
    }

    @Override
    protected void resetCaches() {
        VALID_AREA_CACHE.clear();
    }

    @Override
    protected Value evaluateInternal(String validationKind, String usageScope, IomObject mainObj, Value[] arguments) {
        Value argDatasetName = arguments[0]; // TEXT
        Value argObjects = arguments[1]; // TEXT
        Value argTestObject = arguments[2]; // OBJECT OF ANYCLASS
        Value argTestObjectgeometry = arguments[3]; // TEXT

        if (argDatasetName.isUndefined() || argObjects.isUndefined() || argTestObject.isUndefined()) {
            return Value.createSkipEvaluation();
        }

        String transferIds = argObjects.getValue();
        String datasetName = argDatasetName.getValue();
        ValidAreaKey key = new ValidAreaKey(null, datasetName, transferIds);

        return isInsideValidArea(usageScope, argTestObject, argTestObjectgeometry, () -> VALID_AREA_CACHE.computeIfAbsent(key, this::getValidArea));
    }

    /**
     * Retrieve the geometries from referenced data according to the specified key.
     *
     * @param key The key that specifies which geometries to retrieve.
     * @return The union of all found geometries.
     */
    private Geometry getValidArea(ValidAreaKey key) {
        String datasetName = key.getDatasetName();
        int firstPoint = datasetName.indexOf('.');
        int lastPoint = datasetName.lastIndexOf('.');
        if (firstPoint == -1 || lastPoint == -1) {
            throw new IllegalStateException(MessageFormat.format("Expected qualified attribute name but got <{0}>.", datasetName));
        }

        String model = datasetName.substring(0, firstPoint);
        String attribute = datasetName.substring(lastPoint + 1);
        String qualifiedClass = datasetName.substring(0, lastPoint);

        Collection<URL> urls = getUrlsForModel(model);
        if (urls == null) {
            throw new IllegalStateException(MessageFormat.format("Could not find Transferfile containing model <{0}>.", model));
        }

        Stream<IomObject> objects = urls.stream()
                .map(url -> logExceptionAsWarning(url::openStream))
                .flatMap(xtfStream -> XtfHelper.getObjectsFromXTF(xtfStream, qualifiedClass, key.getTransferIds()).stream());
        Polygon[] polygons = getPolygonsFromObjects(objects, attribute);

        Geometry geometryCollection = new GeometryFactory().createGeometryCollection(polygons);
        return geometryCollection.buffer(0);
    }

    /**
     * Get the URLs of XTF files that may contain objects of the specified model.
     */
    private Collection<URL> getUrlsForModel(String model) {
        if (modelFilesFromJars == null) {
            modelFilesFromJars = analyzeClasspathResources();
        }

        Set<URL> urls = modelFilesFromJars.get(model);
        if (urls != null) {
            return urls;
        }

        if (dataDirectory == null) {
            dataDirectory = getDataDirectory();
        }

        if (modelFilesFromDataDirectory == null) {
            modelFilesFromDataDirectory = analyzeDataDirectory(dataDirectory);
        }
        return modelFilesFromDataDirectory.get(model);
    }

    /**
     * Get the directory where external datasets are located.
     *
     * @return A {@link File} representing the directory or {@code null}.
     */
    private File getDataDirectory() {
        Class<IsInsideExternalXtfIoxPlugin> pluginClass = IsInsideExternalXtfIoxPlugin.class;
        URL resource = pluginClass.getResource(pluginClass.getSimpleName() + ".class");
        if (resource == null) {
            logger.addEvent(logger.logErrorMsg(MessageFormat.format("{0}: Could not resolve data directory, resource is null.", this.getQualifiedIliName())));
            return null;
        }
        String url = resource.toString();
        if (url.startsWith("jar:file:")) {
            String path = url.replaceAll("^jar:file:(.*\\.jar)!/.*", "$1");
            File directory = new File(path).getParentFile();
            logger.addEvent(logger.logDetailInfoMsg(MessageFormat.format("{0}: data directory {1}", this.getQualifiedIliName(), directory)));
            return directory;
        }

        logger.addEvent(logger.logErrorMsg(MessageFormat.format("{0}: Could not resolve data directory.", this.getQualifiedIliName())));
        return null;
    }

    /**
     * Analyze the XTF files in {@link this.dataDirectory}.
     *
     * @return A mapping of Interlis model names to XTF files that contain the model.
     */
    private Map<String, Set<URL>> analyzeDataDirectory(File dataDirectory) {
        final Map<String, Set<URL>> result = new HashMap<>();
        if (dataDirectory == null) {
            return result;
        }

        try {
            List<URL> xtfFiles = Files.walk(dataDirectory.toPath())
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(IoxUtility::isXtfFilename)
                    .map(path -> logExceptionAsWarning(() -> new URL("file", null, path)))
                    .collect(Collectors.toList());

            for (URL xtfUrl : xtfFiles) {
                try (InputStream xtfStream = xtfUrl.openStream()) {
                    Collection<String> models = XtfHelper.getModelsInXTF(xtfStream);
                    for (String model : models) {
                        result.computeIfAbsent(model, k -> new HashSet<>());
                        result.get(model).add(xtfUrl);
                    }
                } catch (IoxException e) {
                    // ignore the erroneous file
                    logger.addEvent(logger.logWarningMsg("{0}: Error while reading xtf file {1}. {2}", QUALIFIED_ILI_NAME, xtfUrl.toString(), e.getLocalizedMessage()));
                }
            }
        } catch (IOException e) {
            logger.addEvent(logger.logWarningMsg("{0}: Could not read data directory {1}. {2}", QUALIFIED_ILI_NAME, dataDirectory.getAbsolutePath(), e.getLocalizedMessage()));
        }

        return result;
    }

    /**
     * Analyze the XTF files inside JARs on the classpath.
     *
     * @return A mapping of Interlis model names to file URLs that contain the model.
     */
    private Map<String, Set<URL>> analyzeClasspathResources() {
        final Map<String, Set<URL>> result = new HashMap<>();
        String[] classPaths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        try {
            for (String path : classPaths) {
                if (path.toLowerCase().endsWith(".jar")) {
                    URL jarUrl = new URL("jar:file:" + path + "!/");
                    JarURLConnection connection = (JarURLConnection) jarUrl.openConnection();
                    JarFile jarfile = connection.getJarFile();

                    List<JarEntry> xtfEntries = jarfile.stream()
                            .filter(entry -> entry.getName().toLowerCase().endsWith(".xtf"))
                            .collect(Collectors.toList());

                    for (JarEntry xtfEntry : xtfEntries) {
                        URL xtfUrl = new URL(jarUrl + xtfEntry.getName());
                        try (InputStream xtfStream = jarfile.getInputStream(xtfEntry)) {
                            Collection<String> models = XtfHelper.getModelsInXTF(xtfStream);
                            for (String model : models) {
                                result.computeIfAbsent(model, k -> new HashSet<>());
                                result.get(model).add(xtfUrl);
                            }
                        } catch (IoxException e) {
                            // ignore the erroneous file
                            logger.addEvent(logger.logWarningMsg("{0}: Error while reading xtf file {1}. {2}", QUALIFIED_ILI_NAME, xtfUrl.toString(), e.getLocalizedMessage()));
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.addEvent(logger.logWarningMsg("{0}: Could not read JAR files from classpath. {2}", QUALIFIED_ILI_NAME, e.getLocalizedMessage()));
        }

        return result;
    }
}
