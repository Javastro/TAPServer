/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap;


import com.fasterxml.jackson.databind.jsontype.NamedType;
import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javastro.ivoa.entities.resource.Capability;
import org.javastro.ivoa.entities.vosi.capabilities.Capabilities;
import org.javastro.ivoa.quarkus.tap.TAPHelper;
import org.javastro.ivoa.quarkus.uws.TransactionalDatabaseStore;
import org.javastro.ivoacore.common.ServiceLocator;
import org.javastro.ivoacore.tap.TAPJob;
import org.javastro.ivoacore.tap.TAPJobSpecification;
import org.javastro.ivoacore.tap.TapExecutionPolicy;
import org.javastro.ivoacore.tap.schema.SchemaProvider;
import org.javastro.ivoacore.tap.schema.VODMLSchemaProvider;
import org.javastro.ivoacore.uws.JobManager;
import org.javastro.ivoacore.uws.environment.DefaultEnvironmentFactory;
import org.javastro.ivoacore.uws.environment.EnvironmentFactory;
import org.javastro.ivoacore.uws.persist.CachedJobStore;
import org.javastro.ivoacore.uws.persist.JobStore;
import org.javastro.ivoacore.vosi.CapabilityBuilder;
import org.javastro.ivoacore.vosi.VOSIProvider;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

/*
 * Created on 03/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
@ApplicationScoped
public class TapConfiguration {

   @ConfigProperty(name="ivoa.tap.schema")
   String tapSchemaResource;

   @Inject
   TapExecutionPolicy tapExecutionPolicy;

   @Inject
   DataSource ds;

   @Inject
   @PersistenceUnit("uwsstore")
   EntityManager em_uws;

   @ConfigProperty(name="ivoa.tap.dbCaseSensitive", defaultValue = "true")
   boolean isDbCaseSensitive;

   @ConfigProperty(name="ivoa.baseAddress", defaultValue = "http://localhost:8080/")
   URI baseURI;

   @ConfigProperty(name="ivoa.tap.useTapTypesInVOSI", defaultValue = "false") // IMPL actually TAP types should never be used in VOSI
   boolean useTapTypesInVOSI;


   // this is used by TAP to know its own base
   @Produces
   @Singleton
   ServiceLocator serviceLocator() {
      //This should be the base for the tap resources (sync, async, tables)
      final URI tapbase =  UriBuilder.fromUri(baseURI).path("tap").build();
      return new ServiceLocator() {
         @Override
         public URI serviceURI() {
            return tapbase;
         }
      };
   }

   @Produces
   @Singleton
   VOSIProvider vosi() {
      return new VOSIProvider() {
         @Override
         public Capabilities getCapabilities() {
            URL url = null;
            try {
               url = new URL(baseURI.toURL(), "tap/");// note needs to end with /
               // standard VOSI ones
               final List<Capability> capabilities = CapabilityBuilder.createVOSICapabilities(url);
               capabilities.addAll(CapabilityBuilder.createTAPCapabilities(url,
                     tapExecutionPolicy.maxRec(), tapExecutionPolicy.getDefaultLifetime(),
                     tapExecutionPolicy.getMaxLifetime(),
                     tapExecutionPolicy.getMaxRunTime()));

               return Capabilities.builder().addCapabilities(capabilities).build();
            } catch (MalformedURLException e) {
               throw new RuntimeException("base URL is malformed", e);
            }
         }
      };
   }

   @Produces
   @Singleton
   SchemaProvider schema(){
      return new VODMLSchemaProvider(tapSchemaResource, isDbCaseSensitive, useTapTypesInVOSI);
   }

   @Produces
   @Singleton
   JobManager uws(SchemaProvider schemaProvider) {

      File tmpdir = null;
      try {
         tmpdir = Files.createTempDirectory("tapserver").toFile();
      } catch (IOException e) {
         throw new RuntimeException("temporary directory not available", e);
      }
      EnvironmentFactory env = new DefaultEnvironmentFactory(tmpdir);

      TAPJob.JobFactory tapJobFactory = new TAPJob.JobFactory(ds, schemaProvider, env, tapExecutionPolicy);

      JobStore store = new CachedJobStore(
           new TransactionalDatabaseStore(em_uws,
                     List.of(new NamedType(TAPJobSpecification.class, TAPJob.JOB_TYPE))
              )
      );

      return new JobManager(tapJobFactory, store, tapExecutionPolicy);
   }

   @Produces
   @Singleton
   TAPHelper tapHelper( JobManager jobManager, ServiceLocator serviceLocator) {
      return new TAPHelper(jobManager, serviceLocator);
   }

}
