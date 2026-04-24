/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap.entities;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.javastro.ivoacore.pgsphere.types.Circle;
import org.javastro.ivoacore.pgsphere.types.Ellipse;
import org.junit.jupiter.api.*;

import jakarta.inject.Inject;
import org.javastro.ivoacore.pgsphere.types.Point;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PgSphereQuarkusTest {
    @Inject
    EntityManager em;
    static Point location;
    static Circle circle;
    static Ellipse el;
    static TestCoord obj;

    @BeforeAll
    public static void init(){
     location = new Point(0.5, 1.1);
     el = new Ellipse(.5,.4,location,.1);
     circle= new Circle(location,.7);
     obj = new TestCoord();
    }

    @Test
    @Transactional
    @Order(1)
    public void testSave() {
       obj.name = "Nebula Y";

        obj.location = new Point(location);

        obj.el = el;

        obj.circle = circle;

        em.persist(obj);
    }
    @Test
    @Order(2)
    @Transactional
    public void testRetrieve(){

        //Clear the persistence context to force a reload from DB
        em.flush();
        em.clear();

        // Fetch from DB
        TypedQuery<TestCoord> q = em.createQuery("select s from TestCoord s where s.id = :id", TestCoord.class);
        q.setParameter("id",obj.getId());
        TestCoord retrieved = q.getSingleResult();

        Assertions.assertNotNull(retrieved);
        Assertions.assertEquals(location, retrieved.location, "Location should match");
        Assertions.assertEquals(circle, retrieved.circle, "circle should match");
 //       Assertions.assertTrue(el.equals(retrieved.el), "Ellipse should match"); // TODO report pgsphere bug
    }




/*    @Test
    @Transactional
    public void testDistanceQuery() {
        // Setup data
        AstroObject star1 = new AstroObject();
        star1.name = "Star A";
        star1.location = new SPoint(0.0, 0.0);
        star1.persist();

        AstroObject star2 = new AstroObject();
        star2.name = "Star B";
        star2.location = new SPoint(0.2, 0.0); // Close
        star2.persist();
        
        // Use Panache's JPQL support (which uses our Dialect)
        // Note: 'sphere_distance' is the function registered in our custom Dialect
        List<AstroObject> nearStars = AstroObject.find(
            "sphere_distance(location, ?1) < ?2", 
            new SPoint(0.0, 0.0), 
            0.5
        ).list();

        Assertions.assertEquals(2, nearStars.size());
    }

 */
}