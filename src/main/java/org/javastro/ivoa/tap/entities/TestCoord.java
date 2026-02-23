/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap.entities;

import jakarta.persistence.*;
import org.javastro.ivoacore.pgsphere.types.Circle;
import org.javastro.ivoacore.pgsphere.types.Ellipse;
import org.javastro.ivoacore.pgsphere.types.Point;
//import org.locationtech.jts.geom.Point;


@Entity
@Table(name = "test_coord",schema="DemoDM" )
public class TestCoord {
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @Column(name = "id", nullable = false)
   private Integer id;

   public Integer getId() {
      return id;
   }

   String name;

   Point location;

   Ellipse el;

   Circle circle;

   public void setId(Integer id) {
      this.id = id;
   }

}