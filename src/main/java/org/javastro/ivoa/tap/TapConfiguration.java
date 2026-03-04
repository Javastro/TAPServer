/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javastro.ivoa.entities.vosi.capabilities.Capabilities;
import org.javastro.ivoacore.tap.schema.SchemaProvider;
import org.javastro.ivoacore.tap.schema.VODMLSchemaProvider;
import org.javastro.ivoacore.vosi.VOSIProvider;

/*
 * Created on 03/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
@ApplicationScoped
public class TapConfiguration {

   @ConfigProperty(name="ivoa.tap.schema")
   String tapSchemaResource;

   @Produces
   VOSIProvider vosi(){
      return new VOSIProvider() {
         @Override
         public Capabilities getCapabilities() {
            return new Capabilities(); //FIXME really create these - try to factor out common behaviour rather than just creating manually.
         }
      };
   }

   @Produces
   SchemaProvider schema(){
      return new VODMLSchemaProvider(tapSchemaResource);
   }

}
