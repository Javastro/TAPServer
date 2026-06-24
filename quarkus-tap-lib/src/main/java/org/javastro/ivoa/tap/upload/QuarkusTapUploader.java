    package org.javastro.ivoa.tap.upload;

    import org.javastro.ivoacore.tap.upload.BaseTAPUploadCacher;
    import org.jboss.resteasy.reactive.server.multipart.FormValue;
    import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;
    import org.jspecify.annotations.NonNull;

    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.StandardCopyOption;
    import java.util.Optional;

    /**
     * The QuarkusTapUploader class provides functionality for parsing and processing
     * the DALI-compliant UPLOAD parameter, handling file uploads or URLs for
     * input data. It includes methods for extracting upload specifications
     * from the UPLOAD parameter, storing VOTables as temporary files, and
     * generating appropriate Paths.
     */
    public class QuarkusTapUploader extends BaseTAPUploadCacher {

        private final MultipartFormDataInput input;

        public QuarkusTapUploader(String uploadParam, MultipartFormDataInput input) {
            super(uploadParam);
            this.input = input;
        }

        /**
         * Stores a VOTable in a temporary file and returns the URI of the file.
         * @param theParam a param parameter of the DALI UPLOAD query parameter, e.g. "param:t3"
         * @return The Path of the uploaded file, or null if the file was not uploaded.
         * @throws IOException If an I/O error occurs while storing the file.
         */
        protected Path storeParam(@NonNull String theParam, Path dir) throws IOException {
            String paramName = theParam.split(":")[1];

            Optional<FormValue> value = Optional.ofNullable(input.getValues().get(paramName))
                    .flatMap(list -> list.stream().findFirst());

            if (value.isPresent() && value.get().isFileItem()) {
                java.nio.file.Path uploadedFile = value.get().getFileItem().getFile();
                Path persistent = generateFileName(dir, paramName);
                Files.copy(uploadedFile, persistent, StandardCopyOption.REPLACE_EXISTING);

                return persistent;
            }
            return null;
        }

        @Override
        public boolean hasUpload() {
            return true;
        }




    }
