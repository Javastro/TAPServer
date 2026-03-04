package org.javastro.ivoa.tap;


import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javastro.ivoacore.tap.schema.SchemaProvider;
import org.javastro.ivoacore.vosi.VOSIProvider;
import org.javastro.ivoa.entities.vosi.capabilities.Capabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/*
 * Created on 28/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
@ApplicationScoped
public class BaseTapServer  {
   private static final Logger log = LoggerFactory.getLogger(BaseTapServer.class);
   @PersistenceContext
   EntityManager em;

   @Inject
   SchemaProvider schemaProvider;

   @Transactional
   void onStart(@Observes StartupEvent ev) {
       log.info("Starting tap server");
       Long i = em.createQuery("select count(o) from Schema o", Long.class).getSingleResult();
       if(i == 0) {
           log.info("populating tap schema");
           for(var s: schemaProvider.getSchemas()) {
               log.info("adding "+s.getSchema_name());
               em.persist(s);
           }

       }
   }

}
