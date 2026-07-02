/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.quarkus.tap;


import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.javastro.ivoa.quarkus.tap.upload.QuarkusTapUploader;
import org.javastro.ivoacore.tap.TAPJobSpecification;
import org.javastro.ivoacore.tap.upload.NullUploader;
import org.javastro.ivoacore.tap.upload.TAPUploadCacher;
import org.javastro.ivoacore.uws.BaseUWSJob;
import org.javastro.ivoacore.uws.JobManager;
import org.javastro.ivoacore.uws.UWSException;
import org.javastro.ivoacore.uws.webapi.BaseUWSResource;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;

public abstract class BaseAsyncTAPResource extends BaseUWSResource {

   /**
    * supply and appropriate TAPHelper implementation for the specific service.
    * @return The TAPHelper.
    */
   abstract protected TAPHelper getTapHelper();

   @Override
   protected JobManager getJobManager() {
      return getTapHelper().getJobmanager();
   }

   @Override
   protected Response redirectToJob(String jobid)  {

      final UriBuilder urib = UriBuilder.fromUri(getTapHelper().getServiceLocator().serviceURI())
            .path("async");
      if (jobid != null && !jobid.isEmpty()) {
         urib.path(jobid);
      }
      return Response.seeOther(urib
            .build()).build();
   }

   //IMPL the two query endpoints are in different resources for routing purposes
   @POST
   @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})
   @Transactional
   public Response async(@RestForm("QUERY") String query, @RestForm("LANG") String lang, @RestForm("RESPONSEFORMAT") String responseformat,
                         @RestForm("MAXREC") Long maxrec, @RestForm("RUNID") String runid,
                         @RestForm("UPLOAD") String upload, MultipartFormDataInput input, @Context UriInfo uriInfo) throws UWSException {
      TAPUploadCacher tapUploader = new NullUploader();
      if(upload != null && !upload.isEmpty() ) {
         tapUploader = new QuarkusTapUploader(upload, input);
      }
      BaseUWSJob job = getTapHelper().getJobmanager().createJob(new TAPJobSpecification(query,lang,responseformat,maxrec,runid,tapUploader));
      return Response.seeOther(getTapHelper().asyncJobUri(job.getID())).build();
   }

   @GET
   @Path("{jobid}/results/result")
   @Produces("application/x-votable+xml")
   public RestResponse<java.nio.file.Path> getVotable(@PathParam("jobid") String jobid) throws UWSException {
      final java.nio.file.Path path = getTapHelper().getResultPath(jobid);
      return RestResponse.ResponseBuilder.ok(path)
            .header(HttpHeaders.CONTENT_DISPOSITION, "result.vot")
            .build();
   }

//----------------------- Need to make database modifying operations transactional ----------------------------------
// Which means the base UWS modifying tasks need to be wrapped in a transactional override
   @Override
   @DELETE
   @Path("{jobid}")
   @Transactional
   public Response deleteJob(@PathParam("jobid")String jobid) throws UWSException {
      return super.deleteJob(jobid);
   }

   @Override
   @POST
   @Path("{jobid}/phase")
   @Transactional
   public Response setPhase(@PathParam("jobid") String jobid, @FormParam("PHASE") String phase) throws UWSException {
      return super.setPhase(jobid, phase);
   }
}
