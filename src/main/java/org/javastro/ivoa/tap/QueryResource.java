/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap;


import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
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
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.RowListStarTable;

import java.time.Duration;

/**
 * Main TAP Query.
 * This works by creating an asynchronous job and then waiting for it to complete,
 * returning the result if it does complete within a reasonable time, or an error VOTable if it doesn't.
 * The client can then poll the async endpoint if they want to wait longer.
 * Created on 04/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
@Tag(name = "TAP Query", description = "the TAP query endpoints")
@ApplicationScoped
@Path("/sync")
public class QueryResource  extends BaseTAPResource {

   @ConfigProperty(name="ivoa.tap.sync-timeout-seconds", defaultValue = "5")
   int syncTimeoutSeconds;


   @GET
   @Produces("application/x-votable+xml")
   public Uni<java.nio.file.Path> syncGet(@RestQuery String query, @RestQuery String lang, @RestQuery String responseformat, @RestQuery Long maxrec, @RestQuery String runid,
                                                     @RestQuery String upload,
                                                     @Context UriInfo uriInfo) {
      return handleJob(query, lang, responseformat, maxrec, runid, upload, uriInfo);

   }
   @POST
   @Produces("application/x-votable+xml")
   public Uni<java.nio.file.Path> syncPost(@RestForm String query, @RestForm String lang, @RestForm String responseformat, @RestForm Long maxrec, @RestForm String runid,
                                           @RestForm String upload,
                                           @Context UriInfo uriInfo) {
      return handleJob(query, lang, responseformat, maxrec, runid, upload, uriInfo);

   }


   private Uni<java.nio.file.Path> handleJob(String query, String lang, String responseformat, Long maxrec, String runid, String upload, UriInfo uriInfo) {
      final Duration SYNC_WAIT = Duration.ofSeconds(syncTimeoutSeconds);
      return Uni.createFrom().deferred(() -> {
         final TAPJob job;
         try {
            job = (TAPJob) jobmanager.createJob(
                  new TAPJobSpecification(query, lang, responseformat, maxrec, runid, upload)
            );

            jobmanager.runJob(job.getID()); // automatically run the job
         } catch (UWSException e) {
            return Uni.createFrom().failure(e);
         }

         return Uni.createFrom().completionStage(job.getJobFuture())
               .onItem().transformToUni(phase -> {
                        if (phase == ExecutionPhase.COMPLETED)
                           return successResponse(job);
                        else {
                           return Uni.createFrom().failure(new UWSException("Underlying TAP job completed with phase " + phase));//TODO could do more sophisticated error handling here based on the phase
                        }
                     }
               )
               .ifNoItem().after(SYNC_WAIT)
               .recoverWithItem(
                     buildErrorVOTable(job, new UWSException("query did not complete within sync time limit of " + SYNC_WAIT.toSeconds() + " seconds - continuing as UWS job"), uriInfo)//FIXME should this return http error code - if so which code?
               );

      }).runSubscriptionOn(Infrastructure.getDefaultExecutor());
   }

   private Uni<java.nio.file.Path> successResponse(TAPJob job) {
          return Uni.createFrom().item(() -> {
             try {
                return getResultPath(job.getID());
             } catch (UWSException e) {
                throw new RuntimeException("Failed to get result path for job " + job.getID(), e);
             }
          });
   }

   //TODO do we always want to return a VOTable even for errors? Or should we allow some other error response?
   //TODO perhaps some of this can be moved to the TAPJob itself (for dealing with other types of errors - e.g. failure to parse original query)
   protected java.nio.file.Path buildErrorVOTable(TAPJob job, UWSException exception, UriInfo uriInfo) {
      // create a VOTable with STIL that has the error message and return the path to it. We could also include some info from the job if we have it.

      TAPJobSpecification tapJobSpec = (TAPJobSpecification) job.getJobSpecification();
      final String errorMessage = exception.getMessage() == null ? "Unknown error" : exception.getMessage();

      try {

         final TAPWriter tableWriter = new TAPWriter(job);;
         ColumnInfo[] columns = new ColumnInfo[]{
               new ColumnInfo("ERROR", String.class, "TAP error message")
         };
         RowListStarTable table = new RowListStarTable(columns);
         table.setName("results");
         table.addRow(new Object[]{errorMessage});
         tableWriter.setTimeoutInfo(asyncJobUri(uriInfo, job.getID()));

         java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("error", ".vot");
         try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(tempFile)) {

            tableWriter.writeStarTable(table, out);
         }
         return tempFile;
      } catch (java.io.IOException e) {
         throw new RuntimeException("Failed to create error VOTable: " + e.getMessage(), e);
      }
   }


}
