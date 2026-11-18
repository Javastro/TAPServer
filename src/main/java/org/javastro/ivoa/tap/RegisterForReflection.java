/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap;


import uk.ac.starlink.votable.DataFormat;
import uk.ac.starlink.votable.VOSerializer;

/**
 * This class serves to register some classes for reflection in native mode - i.e. quarkus cannot work it out for itself
 *
 * They have been discovered by trial and error of creating an running the executable and seeing what it fails on.
 *
 * The class has no other purpose and is not used in any other way. i.e. if a native build is not required then neither is this class.
 *
 * Created on 04/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk)
 * */
@io.quarkus.runtime.annotations.RegisterForReflection(targets = {org.javastro.ivoa.entities.resource.registry.iface.ResourceInstance.class
, org.javastro.ivoacore.uws.persist.mappers.JobEntityMapper.class, org.javastro.ivoacore.uws.persist.mappers.JobEntityMapperImpl.class
      },registerFullHierarchy = true) // relies on jandex indexing
public class RegisterForReflection {
}

//stil is tricky
@io.quarkus.runtime.annotations.RegisterForReflection(targets = {uk.ac.starlink.votable.VOTableWriter.class, uk.ac.starlink.table.StreamStarTableWriter.class
, uk.ac.starlink.fits.FitsTableWriter.class,  uk.ac.starlink.votable.UnifiedFitsTableWriter.class
, uk.ac.starlink.table.formats.CsvTableWriter.class, uk.ac.starlink.table.formats.AsciiTableWriter.class,
      uk.ac.starlink.votable.VOSerializer.class , uk.ac.starlink.votable.DataFormat.class
}, ignoreNested = false)
class Stil {

      // Stil will complain about a number of table writers not being registered for reflection, but they are not actually used in the code so we can ignore them.
}
