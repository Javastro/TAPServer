package org.javastro.ivoa.tap;


import jakarta.enterprise.context.ApplicationScoped;
import org.javastro.ivoacore.vosi.VOSIProvider;
import org.javastro.ivoa.entities.vosi.capabilities.Capabilities;

/*
 * Created on 28/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
@ApplicationScoped
public class BaseTapServer implements VOSIProvider {
   @Override
   public Capabilities getCapabilities() {
      return new Capabilities();
   }
}
