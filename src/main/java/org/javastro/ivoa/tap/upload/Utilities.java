package org.javastro.ivoa.tap.upload;

import org.javastro.ivoacore.tap.upload.TapUploadService;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The Utilities class provides functionality for parsing and processing
 * the DALI-compliant UPLOAD parameter, handling file uploads or URLs for
 * input data. It includes methods for extracting upload specifications
 * from the UPLOAD parameter, storing VOTables as temporary files, and
 * generating appropriate URIs.
 */
public class Utilities {

    /**
     * Parses the UPLOAD parameter from a DALI-compliant query and processes any file uploads or URLs present in it.
     *
     * @param uploadParam The UPLOAD parameter containing mappings of table names to data locations.
     *                    Each mapping is provided in the format: tableName,dataLocation, where dataLocation could be a URL or a
     *                    parameter indicating an uploaded file (e.g., param:uploadFile).
     * @param input The multipart form data input containing uploaded file data, if any.
     * @return A map where the keys are table names and the values are URIs pointing to corresponding data sources (e.g., temporary file URIs, remote URLs).
     */
    public static Map<String, URI> parseUploadParams(String uploadParam, MultipartFormDataInput input) {
        Map<String, URI> uploadMap = new java.util.HashMap<>();

        if (uploadParam != null) {
            String[] uploadSpecs = uploadParam.split(";");
            for (String uploadSpec : uploadSpecs) {
                // If there's a "~,param:~~" upload parameter supplied, then upload the file to tmp and create a file: URI to it.
                String[] parts = uploadSpec.split(",");
                String tableName = parts[0];
                String tableLoc = parts[1];            //either param:<upload file> or a URL to a remote file (http, https, vos, etc)
                if (TapUploadService.isValidUploadParam(uploadSpec)) {
                    if (tableLoc.startsWith("param:")) {
                        try {
                            URI fileUri = storeVOTable(tableLoc, input);
                            if (fileUri != null) {
                                uploadSpec = fileUri.toString();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else if (tableLoc.startsWith("vos")) {
                        //Requires a VOSpace client to be configured for testing
                        throw new UnsupportedOperationException("VOSpace uploads are not currently supported");
                    }
                    else {
                        uploadSpec = tableLoc;
                    }
                }
                uploadMap.put(tableName, URI.create(uploadSpec));
            }
        }
        return uploadMap;
    }

    /**
     * Stores a VOTable in a temporary file and returns the URI of the file.
     * @param uploadParam parameter of the DALI UPLOAD query parameter, e.g. "table1,http://example.com/t1.xml",
     *                    "image1,vos://example.authority!tempSpace/foo.fits", or "table3,param:t3"
     * @param input The multipart form data input.
     * @return The URI of the uploaded file, or null if the file was not uploaded.
     * @throws IOException If an I/O error occurs while storing the file.
     */
    private static URI storeVOTable(@NonNull String uploadParam, @NonNull MultipartFormDataInput input) throws IOException {
        String paramName = uploadParam.split(":")[1];

        Optional<FormValue> value = Optional.ofNullable(input.getValues().get(paramName))
                .flatMap(list -> list.stream().findFirst());

        if (value.isPresent() && value.get().isFileItem()) {
            java.nio.file.Path uploadedFile = value.get().getFileItem().getFile();

            UUID uuid = UUID.randomUUID();
            java.nio.file.Path persistent = Files.createTempFile("tap-upload-" + uuid, ".vot");

            Files.copy(uploadedFile, persistent, StandardCopyOption.REPLACE_EXISTING);

            return persistent.toUri();
        }
        return null;
    }
}
