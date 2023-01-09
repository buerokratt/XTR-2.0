package ee.ria.xtr_2_0.xgen.metadata;

import com.nortal.jroad.model.XmlBeansXRoadMetadata;
import ee.ria.xtr_2_0.model.XtrDatabase;
import ee.ria.xtr_2_0.xgen.exception.InvalidMetadataException;
import ee.ria.xtr_2_0.xgen.util.MethodNameUtil;
import org.apache.xmlbeans.impl.common.NameUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import java.beans.Introspector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

/**
 * Reads metadata files and generates yaml configuration files.
 */
public class MetadataReader {

    /**
     * suffix for service names in configuration files
     */
    private static final String XROAD_DATABASE_CLASS_NAME_SUFFIX = "XRoadDatabase";
    /**
     * files with this extensions are considered metadata and processed
     */
    private static final String XROAD_METADATA_FILE_EXTENSION = ".metadata";
    /**
    default output directory path
     */
    private static final String ROOT = ".";

    private final Yaml yaml = new Yaml();

    /**
     * Reads metadata files from input directory and produces yaml configuration files.
     * Configuration files are written in  output directory if specified, else to folder specified in ROOT field in this class.
     *
     * @param args args[0] input directory path (contains metadata files), args[1] (optional) output directory path
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("metadata file(s) directory path no set");
        }

        new MetadataReader().processMetadata(args[0], args.length > 1 ? args[1] : ROOT);
    }

    @SuppressWarnings("unchecked")
    private void processMetadata(String inputPath, String outputPath) throws Exception {
        System.out.println("Generating configuration files: " + inputPath);
        File metadataDir = new File(inputPath);
        if (!metadataDir.isDirectory()) {
            throw new IllegalArgumentException(inputPath + " is not directory");
        }

        File[] files = metadataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(XROAD_METADATA_FILE_EXTENSION));
        System.out.println("Processing " + files.length + " metadata file(s)");
        for (File metadataFile : files) {
            System.out.println("Reading metadata from " + metadataFile.getAbsolutePath());
            FileInputStream fis = new FileInputStream(metadataFile);
            ObjectInputStream ois = new ObjectInputStream(fis);

            Object metadataObj = ois.readObject();
            if (metadataObj instanceof Map) {
                processMetadata((Map<String, XmlBeansXRoadMetadata>) metadataObj, outputPath);
            }
            else {
                throw new InvalidMetadataException(metadataFile.getPath());
            }
        }
    }

    private void processMetadata(Map<String, XmlBeansXRoadMetadata> metadata, String path) throws IOException {
        for (Map.Entry<String, XmlBeansXRoadMetadata> entry : metadata.entrySet()) {
            XmlBeansXRoadMetadata serviceMetadata = entry.getValue();

            String key = entry.getKey();
            String databaseName = key.substring(0, key.lastIndexOf(serviceMetadata.getOperationName().toLowerCase()));

            XtrDatabase db = new XtrDatabase();
            db.setRegistryCode(databaseName);
            db.setServiceCode(serviceMetadata.getOperationName());
            db.setNamespaceUri(serviceMetadata.getResponseElementNs());
            db.setOperationName(serviceMetadata.getOperationName());
            db.setVersion(serviceMetadata.getVersion());

            db.setServiceName(
                    Introspector.decapitalize(NameUtil.upperCamelCase(databaseName) + XROAD_DATABASE_CLASS_NAME_SUFFIX)
            );

            db.setMethod(MethodNameUtil.methodName(serviceMetadata));

            File confFile = new File(path, db.getRegistryCode() + "_" + db.getServiceCode() + ".yaml");
            FileWriter fileWriter = new FileWriter(confFile);

            System.out.println("Writing configuration to " + confFile.getAbsolutePath());

            fileWriter.write(yaml.dumpAs(db, Tag.MAP, DumperOptions.FlowStyle.BLOCK));
            fileWriter.flush();
            fileWriter.close();
        }
    }

}
