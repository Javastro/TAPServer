/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javastro.ivoa.entities.resource.AccessURL;
import org.javastro.ivoa.entities.resource.Capability;
import org.javastro.ivoa.entities.resource.ServiceInterface;
import org.javastro.ivoa.entities.resource.dataservice.ParamHTTP;
import org.javastro.ivoa.entities.resource.tap.TableAccess;
import org.javastro.ivoa.entities.vosi.capabilities.Capabilities;
import org.javastro.ivoacore.tap.schema.SchemaProvider;
import org.javastro.ivoacore.tap.schema.VODMLSchemaProvider;
import org.javastro.ivoacore.vosi.CapabilityBuilder;
import org.javastro.ivoacore.vosi.VOSIProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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
            final URL url;
            try {
               url = new URL("https://localhost/me"); // FIXME get the real endpoint....
            } catch (MalformedURLException e) {
               throw new RuntimeException(e);
            }
            // standard VOSI ones
            final List<Capability> capabilities = CapabilityBuilder.createCapabilities(url);
            //then the TAP one
            ParamHTTP intf = ParamHTTP.builder().withVersion("1.1").build();//TODO add all the parameters

            capabilities.add(TableAccess.builder().withStandardID("ivo://ivoa.net/std/TAP")
                  .withInterfaces(
                     List.of(intf.newCopyBuilder().addAccessURLs(new AccessURL("sync","full")).build(),
                             intf.newCopyBuilder().addAccessURLs(new AccessURL("async","full")).build())
                  )
                  .build());//FIXME really create these - try to factor out common behaviour rather than just creating manually.
            return Capabilities.builder().addCapabilities(capabilities).build();
         }
      };
   }

   @Produces
   SchemaProvider schema(){
      return new VODMLSchemaProvider(tapSchemaResource);
   }

}
