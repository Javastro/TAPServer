Configuration reference
=======================

Quarkus reads ``src/main/resources/application.properties`` and supports
profile-specific entries such as ``%dev.`` and ``%prod.``, system properties,
environment variables and external configuration files. Later/higher-priority
sources can override packaged values. See the `Quarkus configuration guide
<https://quarkus.io/guides/config-reference>`_ for precedence, environment
variable conversion, profiles and build-time configuration. Some Quarkus
properties are fixed at build time: set those before building the application
or container rather than relying on a runtime override.

The tables distinguish values **in this example** from defaults in the
application code or the resolved policy configuration library. Values are
strings unless another type is noted. Properties without a profile apply
across profiles unless overridden. This example is not a production-ready template.


TAP and UWS
-----------

.. list-table::
   :header-rows: 1
   :widths: 37 25 38

   * - Property
     - Example / code default
     - Meaning
   * - ``ivoa.baseAddress``
     - ``http://localhost:8080/`` / same
     - Public application-root URI, with trailing slash; TAP is appended.
   * - ``ivoa.tap.schema``
     - ``DemoDM.vo-dml.tap.xml`` / required
     - Classpath resource defining the VO-DML TAP schema.
   * - ``ivoa.tap.dbCaseSensitive``
     - ``true`` / ``true`` [fixed]_
     - Boolean passed to the schema provider for database identifier matching.
   * - ``ivoa.tap.useTapTypesInVOSI``
     - Not set / ``false`` [fixed]_
     - Boolean passed to the schema provider when generating VOSI tables.
   * - ``ivoa.tap.sync-timeout-seconds``
     - ``5`` / ``5``
     - Integer seconds to wait for a synchronous job.
   * - ``ivoa.tap.policy.max-rec``
     - ``10000`` / ``1000000``
     - Long maximum number of query result records.
   * - ``ivoa.uws.execution.default-lifetime``
     - ``3600`` / ``604800``
     - Long default job lifetime in seconds.
   * - ``ivoa.uws.execution.max-lifetime``
     - ``7200`` / ``604800``
     - Long maximum job lifetime in seconds.
   * - ``ivoa.uws.execution.default-run-time``
     - ``300`` / ``3600``
     - Long default execution duration in seconds; zero means unlimited.
   * - ``ivoa.uws.execution.max-run-time``
     - ``600`` / ``86400``
     - Long maximum execution duration in seconds; zero means unlimited.
   * - ``ivoa.uws.execution.kill-period``
     - ``60`` / ``60``
     - Integer seconds between checks for jobs exceeding their runtime.
   * - ``ivoa.uws.execution.destroy-period``
     - Not set / ``3600``
     - Integer seconds between checks for expired jobs.
   * - ``ivoa.uws.execution.max-concurrent``
     - Not set / ``10``
     - Integer maximum number of concurrent jobs.

Policy defaults above are from the ``quarkus-config`` policy mappings used by
the packaged application. The TAP adapter delegates to those mappings; the
framework-independent default policy is a different implementation.

Deployment and build-time configuration
---------------------------------------

.. list-table::
   :header-rows: 1
   :widths: 40 25 35

   * - Property
     - Example value
     - Purpose
   * - ``quarkus.application.name``
     - ``tap-server``
     - Application name.
   * - ``quarkus.helm.description``
     - ``TAPServer example``
     - Helm chart description.
   * - ``quarkus.kubernetes.ingress.host``
     - ``mvptap.test``
     - Example ingress hostname; replace it and review generated resources.
   * - ``ivoa.registry.authority``
     - Not specified
     - Example registry authority metadata; setting it alone does not publish a registry record.
   * - ``ivoa.dc.organizationName``, ``ivoa.dc.contactName``, ``ivoa.dc.contactAddress``, ``ivoa.dc.contactEmail``, ``ivoa.dc.contactTelephone``
     - Not specified
     - Example organization and contact metadata. Verify the configuration mapping and registry integration before relying on these properties.


Database, ORM and indexing
--------------------------

.. list-table::
   :header-rows: 1
   :widths: 40 25 35

   * - Property
     - Example value
     - Purpose
   * - ``quarkus.datasource.db-kind``
     - ``postgresql`` [fixed]_
     - Default JDBC database kind. Set ``quarkus.datasource.jdbc.url``,
       ``quarkus.datasource.username`` and ``quarkus.datasource.password``
       for a provisioned database.
   * - ``quarkus.datasource.devservices.enabled``, ``.reuse``, ``.image-name``
     - ``true``, ``true``, ``images.dev.uksrc.org/library/postgres-pgsphere14:latest``
     - Dev Services database options; the image must be accessible.
   * - ``%dev.quarkus.datasource.devservices.port``
     - ``54771``
     - Development-profile Dev Services host port.
   * - ``quarkus.hibernate-orm.dialect``
     - ``org.javastro.ivoacore.pgsphere.PgSphereDialect`` [fixed]_
     - Hibernate dialect for the TAP pgSphere integration.
   * - ``quarkus.hibernate-orm.packages``
     - ``org.ivoa.dm,org.ivoa.vodml.stdtypes,org.javastro.ivoacore.pgsphere`` [fixed]_
     - Packages assigned to the default persistence unit; adapt to your model.
   * - ``quarkus.hibernate-orm.quote-identifiers.strategy`` [fixed]_
     - ``all``
     - Quote Hibernate identifiers.
   * - ``quarkus.hibernate-orm.schema-management.create-schemas``
     - ``true``
     - Allow schema creation by Hibernate.
   * - ``quarkus.hibernate-orm.schema-management.strategy``
     - ``drop-and-create``
     - Destructive example schema lifecycle; override for production.
   * - ``quarkus.hibernate-orm.scripts.generation``
     - ``drop-and-create``
     - Generate schema scripts; targets are ``TAPddl.sql`` and
       ``TAP_drop_ddl.sql``.
   * - ``quarkus.hibernate-orm."uwsstore".packages``
     - ``org.javastro.ivoacore.uws.persist``
     - UWS entity package assigned to the named persistence unit.
   * - ``quarkus.hibernate-orm."uwsstore".datasource``
     - ``<default>``
     - Datasource used by the UWS unit; it may instead be a named datasource.
   * - ``quarkus.hibernate-orm."uwsstore".schema-management.strategy``
     - ``drop-and-create``
     - Destructive UWS schema lifecycle; override for production.
   * - ``quarkus.hibernate-orm."uwsstore".schema-management.create-schemas``
     - ``true``
     - Allow UWS schema creation by Hibernate.
   * - ``quarkus.index-dependency.quarkus-tap-lib.group-id`` and ``.artifact-id``
     - ``org.javastro.ivoa.core.quarkus`` / ``quarkus-tap-lib``
     - Index library resource classes for CDI and interceptor discovery.
   * - ``quarkus.hibernate-orm.log.sql``, ``quarkus.hibernate-orm.log.bind-parameters``
     - ``true``, ``true``
     - SQL and bind-parameter logging; avoid exposing sensitive query values in production.
   * - ``%test.quarkus.hibernate-orm``
     - Formatted/highlighted SQL and Hibernate TRACE logging
     - Additional test-profile SQL formatting and logging.
   * - ``%dev.quarkus.hibernate-orm.dev-ui.allow-hql``
     - ``true``
     - Permit HQL in the development UI.


Consult the `Quarkus datasource <https://quarkus.io/guides/datasource>`_ and
`Hibernate ORM <https://quarkus.io/guides/hibernate-orm>`_ references for
defaults, build-time restrictions and named-datasource syntax. Never use the
example's schema lifecycle against data you need to keep.

Other properties
------------------------

* ``quarkus.log.category."io.netty.handler.codec.http.websocketx".use-parent-handlers=false``
  changes logging for that category.

.. [fixed] This property cannot be changed from that specified for the application to work.