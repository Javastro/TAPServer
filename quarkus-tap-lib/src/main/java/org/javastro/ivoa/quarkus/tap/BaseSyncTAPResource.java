/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.quarkus.tap;


import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoa.quarkus.tap.upload.QuarkusTapUploader;
import org.javastro.ivoacore.tap.TAPJob;
import org.javastro.ivoacore.tap.TAPJobSpecification;
import org.javastro.ivoacore.tap.TAPWriter;
import org.javastro.ivoacore.tap.upload.NullUploader;
import org.javastro.ivoacore.tap.upload.TAPUploadCacher;
import org.javastro.ivoacore.uws.UWSException;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.RowListStarTable;

import java.nio.file.Path;
import java.time.Duration;

/**
 * Main TAP Query.
 * This works by creating an asynchronous job and then waiting for it to complete,
 * returning the result if it does complete within a reasonable time, or an error VOTable if it doesn't.
 * The client can then poll the async endpoint if they want to wait longer.
 * Created on 04/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
public abstract class BaseSyncTAPResource {

   @Inject
   TAPJobService jobService;

   protected static final Logger log = LoggerFactory.getLogger(BaseSyncTAPResource.class);

   /**
    * supply and appropriate TAPHelper implementation for the specific service.
    * @return The TAPHelper.
    */
   abstract protected TAPHelper getTapHelper();

   /**
    * supply the sync wait time before the synchronous call times out..
    * @return the wait time in seconds.
    */
   abstract protected int getSyncWait();

   @GET
   @Produces("application/x-votable+xml")
   public Uni<Path> syncGet(@RestQuery String query, @RestQuery String lang, @RestQuery String responseformat, @RestQuery Long maxrec, @RestQuery String runid,
                            @RestQuery String upload,
                            @Context UriInfo uriInfo) {

      return handleJob(query, lang, responseformat, maxrec, runid, upload, null, uriInfo);
   }

   //UPLOAD param details - https://www.ivoa.net/documents/DALI/20170517/REC-DALI-1.1.html#tth_sEc3.4.5
   //UPLOAD=table1,http://example.com/t1.xml
   //UPLOAD=image1,vos://example.authority!tempSpace/foo.fits
   //UPLOAD=table3,param:t3
   @POST
   @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})
   @Produces("application/x-votable+xml")
   public Uni<Path> syncPost(@RestForm("QUERY") String query, @RestForm("LANG") String lang, @RestForm("RESPONSEFORMAT") String responseformat, @RestForm("MAXREC") Long maxrec, @RestForm("RUNID") String runid,
                             @RestForm("UPLOAD") String upload,
                             MultipartFormDataInput input,
                             @Context UriInfo uriInfo) {

      return handleJob(query, lang, responseformat, maxrec, runid, upload, input, uriInfo);
   }


   protected Uni<Path> handleJob(String query, String lang, String responseformat, Long maxrec, String runid, String upload, MultipartFormDataInput input, UriInfo uriInfo) {
      final Duration SYNC_WAIT = Duration.ofSeconds(getSyncWait());
      return Uni.createFrom().deferred(() -> {
         final TAPJob job;
         try {
            TAPUploadCacher tapUploader = new NullUploader();
            if(upload != null && !upload.isEmpty() ) {
               tapUploader = new QuarkusTapUploader(upload, input);
            }
            job = jobService.createJob(new TAPJobSpecification(query, lang, responseformat, maxrec, runid, tapUploader));

            getTapHelper().getJobmanager().runJob(job.getID()); // automatically run the job
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

   private Uni<Path> successResponse(TAPJob job) {
          return Uni.createFrom().item(() -> {
             try {
                return getTapHelper().getResultPath(job.getID());
             } catch (UWSException e) {
                throw new RuntimeException("Failed to get result path for job " + job.getID(), e);
             }
          });
   }

   //TODO do we always want to return a VOTable even for errors? Or should we allow some other error response?
   //TODO perhaps some of this can be moved to the TAPJob itself (for dealing with other types of errors - e.g. failure to parse original query)
   protected Path buildErrorVOTable(TAPJob job, UWSException exception, boolean timeout) {
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
            tableWriter.setTimeoutInfo(getTapHelper().asyncJobUri(job.getID()));
         }

         Path tempFile = java.nio.file.Files.createTempFile("error", ".vot");
         try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(tempFile)) {

            tableWriter.writeStarTable(table, out);
         }
         return tempFile;
      } catch (java.io.IOException e) {
         throw new RuntimeException("Failed to create error VOTable: " + e.getMessage(), e);
      }
   }
}
