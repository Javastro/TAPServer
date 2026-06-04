/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap;


import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoacore.tap.TAPJob;
import org.javastro.ivoacore.tap.TAPJobSpecification;
import org.javastro.ivoacore.tap.TAPWriter;
import org.javastro.ivoacore.uws.UWSException;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;
import uk.ac.starlink.table.*;
import uk.ac.starlink.votable.VOTableBuilder;
import uk.ac.starlink.votable.VOTableWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Main TAP Query.
 * This works by creating an asynchronous job and then waiting for it to complete,
 * returning the result if it does complete within a reasonable time, or an error VOTable if it doesn't.
 * The client can then poll the async endpoint if they want to wait longer.
 * Created on 04/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
@Tag(name = "TAP Query", description = "the TAP query endpoints")
@ApplicationScoped
@Path("sync")
public class QueryResource  {

   @ConfigProperty(name="ivoa.tap.sync-timeout-seconds", defaultValue = "5")
   int syncTimeoutSeconds;

   @Inject
   TAPHelper  tapHelper;

   private static final Pattern UPLOAD_PATTERN = Pattern.compile("^[^,:]+,[a-zA-Z][a-zA-Z0-9+.-]*:.+$");
   

   @GET
   @Produces("application/x-votable+xml")
   public Uni<java.nio.file.Path> syncGet(@RestQuery String query, @RestQuery String lang, @RestQuery String responseformat, @RestQuery Long maxrec, @RestQuery String runid,
                                                     @RestQuery String upload,
                                                     @Context UriInfo uriInfo) {
      return handleJob(query, lang, responseformat, maxrec, runid, upload, uriInfo);

   }

   //UPLOAD param details - https://www.ivoa.net/documents/DALI/20170517/REC-DALI-1.1.html#tth_sEc3.4.5
   //UPLOAD=table1,http://example.com/t1.xml
   //UPLOAD=image1,vos://example.authority!tempSpace/foo.fits
   //UPLOAD=table3,param:t3
   @POST
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces("application/x-votable+xml")
   public Uni<java.nio.file.Path> syncPost(@RestForm("QUERY") String query, @RestForm("LANG") String lang, @RestForm("RESPONSEFORMAT") String responseformat, @RestForm("MAXREC") Long maxrec, @RestForm("RUNID") String runid,
                                           @RestForm("UPLOAD") String upload,
                                           MultipartFormDataInput input,
                                           @Context UriInfo uriInfo) {

      if (isValidUploadParam(upload)) {
         String[] parts = upload.split(",");
         String tableName = parts[0];
         String uploadParam = parts[1];

         if (uploadParam.startsWith("param:")) {
            storeVOTable(uploadParam, input);
         } else {
            //URI located VOTable - probably only need to handle the param: version as https: etc will be handled in the actual job
            switch (URI.create(uploadParam).getScheme()) {
               case "http":
               case "https":
               case "file":
               case "vos"://might need handling differently to explicit https
            }
         }
      }

      return handleJob(query, lang, responseformat, maxrec, runid, upload, uriInfo);

   }


   private Uni<java.nio.file.Path> handleJob(String query, String lang, String responseformat, Long maxrec, String runid, String upload, UriInfo uriInfo) {
      final Duration SYNC_WAIT = Duration.ofSeconds(syncTimeoutSeconds);
      return Uni.createFrom().deferred(() -> {
         final TAPJob job;
         try {
            job = (TAPJob) tapHelper.jobmanager.createJob(
                  new TAPJobSpecification(query, lang, responseformat, maxrec, runid, upload)
            );

            tapHelper.jobmanager.runJob(job.getID()); // automatically run the job
         } catch (UWSException e) {
            return Uni.createFrom().failure(e);
         }

         return Uni.createFrom().completionStage(job.getJobFuture())
               .onItem().transformToUni(phase -> {
                        if (phase == ExecutionPhase.COMPLETED) {
                           return successResponse(job);
                        }
                        else if (phase == ExecutionPhase.ERROR)
                        {
                           return Uni.createFrom().item( buildErrorVOTable(job,null, false));
                        }
                        else {
                           return Uni.createFrom().failure(new UWSException("Underlying TAP job completed with unexpected phase " + phase));//TODO could do more sophisticated error handling here based on the phase
                        }
                     }
               )
               .ifNoItem().after(SYNC_WAIT)
               .recoverWithItem(
                     buildErrorVOTable(job, new UWSException("query did not complete within sync time limit of " + SYNC_WAIT.toSeconds() + " seconds - continuing as UWS job"), true)//FIXME should this return http error code - if so which code?
               );

      }).runSubscriptionOn(Infrastructure.getDefaultExecutor()); //TODO review whether this is the right way to do this - We might want to use a dedicated thread pool for this or some other strategy for managing the threads.
   }

   private Uni<java.nio.file.Path> successResponse(TAPJob job) {
          return Uni.createFrom().item(() -> {
             try {
                return tapHelper.getResultPath(job.getID());
             } catch (UWSException e) {
                throw new RuntimeException("Failed to get result path for job " + job.getID(), e);
             }
          });
   }

   //TODO do we always want to return a VOTable even for errors? Or should we allow some other error response?
   //TODO perhaps some of this can be moved to the TAPJob itself (for dealing with other types of errors - e.g. failure to parse original query)
   protected java.nio.file.Path buildErrorVOTable(TAPJob job, UWSException exception, boolean timeout) {
      // create a VOTable with STIL that has the error message and return the path to it. We could also include some info from the job if we have it.

      TAPJobSpecification tapJobSpec = (TAPJobSpecification) job.getJobSpecification();
      try {

         final TAPWriter tableWriter = new TAPWriter(job);
         ColumnInfo[] columns = new ColumnInfo[]{
               new ColumnInfo("ERROR", String.class, "TAP error message")
         };
         RowListStarTable table = new RowListStarTable(columns);
         table.setName("error");
         if(exception != null) {
            table.addRow(new Object[]{exception.getMessage()});
         }
         if(timeout) {
            tableWriter.setTimeoutInfo(tapHelper.asyncJobUri(job.getID()));
         }

         java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("error", ".vot");
         try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(tempFile)) {

            tableWriter.writeStarTable(table, out);
         }
         return tempFile;
      } catch (java.io.IOException e) {
         throw new RuntimeException("Failed to create error VOTable: " + e.getMessage(), e);
      }
   }

   private static boolean isValidUploadParam(String input) {
      return input != null && UPLOAD_PATTERN.matcher(input).matches();
   }

   private void storeVOTable(String uploadParam, MultipartFormDataInput input){
      String paramName = uploadParam.split(":")[1];

      Optional<FormValue> value =
              Optional.ofNullable(input.getValues().get(paramName))
                      .flatMap(list -> list.stream().findFirst());

      if (value.isPresent() && value.get().isFileItem()) {
         java.nio.file.Path uploadedFile = value.get().getFileItem().getFile();

         try (InputStream in = Files.newInputStream(uploadedFile)) {

            StarTable t = new StarTableFactory().makeStarTable(in, new VOTableBuilder());

            //test output
            StarTableWriter writer = new VOTableWriter();
            // Alternatives:
            // StarTableWriter writer = new uk.ac.starlink.table.formats.CsvTableWriter();
            // StarTableWriter writer = new uk.ac.starlink.table.formats.TextTableWriter();

            // 3. Pipe the stream into a ByteArrayOutputStream
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (os) {
               writer.writeStarTable(t, os);
            }

            // 4. Convert the byte stream to a String
            String tableString = os.toString(StandardCharsets.UTF_8);

            // Now you can print it or use it as needed
            System.out.println(tableString);
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }
   }
}
