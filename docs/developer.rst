Developer guide
===============

This guide starts a new Quarkus application using the reusable TAP and UWS
libraries, with this repository's five top-level TAP classes as a working
starting point. They are application code, not a dependency published by this
repository. The example entities in ``org.javastro.ivoa.tap.entities`` are not
needed for the TAP wiring.

Prerequisites and project setup
-------------------------------

Install JDK 17 and Git. For the example development database, a working
container runtime and access to the configured PostgreSQL/pgSphere image are
also required. The Gradle wrapper downloads Gradle and the build resolves
dependencies from Maven repositories; access to
``https://repo.dev.uksrc.org/repository/maven-public/`` is required for the
Javastro build plugin and may be required for Javastro artifacts.

Create a directory for the new service and copy the build scaffolding and the
five application classes from a checkout of this repository. Run these commands
from the checkout, replacing the destination with your new project directory:

.. code-block:: sh

   export TAPSERVER="$(pwd)"
   export NEW_SERVICE="$HOME/my-tap-service"
   mkdir -p "$NEW_SERVICE/src/main/java/org/javastro/ivoa/tap"
   mkdir -p "$NEW_SERVICE/src/main/resources"
   cp "$TAPSERVER/gradlew" "$TAPSERVER/gradlew.bat" "$TAPSERVER/gradle.properties" "$NEW_SERVICE/"
   cp "$TAPSERVER/settings.gradle.kts" "$TAPSERVER/build.gradle.kts" "$NEW_SERVICE/"
   cp -R "$TAPSERVER/gradle" "$NEW_SERVICE/"
   cp "$TAPSERVER/src/main/java/org/javastro/ivoa/tap/"*.java "$NEW_SERVICE/src/main/java/org/javastro/ivoa/tap/"
   cp "$TAPSERVER/src/main/resources/application.properties" "$NEW_SERVICE/src/main/resources/"
   cd "$NEW_SERVICE"
   git init

Change ``rootProject.name`` in ``settings.gradle.kts`` and ``group`` and
``quarkus.application.name`` to suit your project. In the copied
``quarkus.hibernate-orm.packages`` setting, remove
``org.javastro.ivoa.tap.entities`` because the example entities were not
copied. The copied application configuration is a *development example*, not a
production configuration: both persistence units use destructive schema
creation. See :doc:`deployer` before connecting to any valuable database.
The copied Javastro build plugin requires a Git worktree for versioning, so
initialize the repository before invoking Gradle.

The copied Gradle build uses the Javastro Quarkus build plugin, which supplies
the Quarkus platform and ``quarkus-config`` dependency. Its direct dependencies
include ``quarkus-tap-lib``, ``quarkus-uws`` and ``dal``. TAP core and its TAP
schema model are transitive dependencies. The example also uses ``pgsphere``,
``DemoDM``, Hibernate ORM, PostgreSQL JDBC, REST, JAXB, JSON-B and OpenAPI.
Keep the versions and repository configuration from the checked-out build
together; choose and test compatible versions when upgrading. The ``DemoDM``
dependency supplies the sample schema resource; replace it when introducing
your own data model.

Minimal service wiring
----------------------

The five source files can initially be used unchanged. Adapt their package
names and resource paths together if necessary:

* ``TapConfiguration`` is a CDI application-scoped producer. It supplies
  ``ServiceLocator``, ``VOSIProvider``, ``SchemaProvider``, ``JobManager`` and
  ``TAPHelper``. It injects the default datasource, a ``TapExecutionPolicy``
  and the ``uwsstore`` persistence unit's entity manager. The job manager
  combines a ``TAPJob.JobFactory``, temporary-directory environment, cached
  transactional database job store and the execution policy.
* ``QueryResource`` and ``AsyncQueryResource`` extend the library's sync and
  async resources and inject the shared helper. Their paths must share the
  same TAP root: this example uses ``/tap/sync`` and ``/tap/async``. Sync
  requests wait up to ``ivoa.tap.sync-timeout-seconds`` for the job.
* ``VOSIResource`` supplies availability and capabilities through the
  library's VOSI resource and the produced provider. ``VOSITablesResource``
  serves tables metadata at ``/tap/tables`` from the same schema provider.

``TapConfiguration`` loads ``ivoa.tap.schema`` as a classpath VO-DML TAP XML
resource using ``VODMLSchemaProvider``. The database, the resource and the
tables advertised in it must agree. Set ``ivoa.baseAddress`` to the externally
reachable application root; the service locator appends ``tap``. The copied
configuration indexes ``quarkus-tap-lib`` for CDI/interceptor discovery and
configures a named ``uwsstore`` persistence unit. The store uses the default
datasource in this example. See :doc:`configuration` for these settings.

The temporary job directory, cached database store, PostgreSQL/pgSphere and
DemoDM are example choices rather than universal TAP requirements. Consider
the lifetime of the temporary directory and job results when adapting the
service for restarts or multiple instances; do not assume shared result files.

Where TAP is only one of the interfaces in a service, the five classes can be copied into a larger Quarkus application, and appropriate dependencies made - An example of such a service is the `RegTAP TAP interface on a Registry <https://github.com/Javastro/Registry/tree/main/src/main/java/org/javastro/ivoa/registry/tap>`_.


Run and check
-------------

With the development container runtime available, run ``./gradlew quarkusDev``.
In another terminal:

.. code-block:: sh

   curl -f http://localhost:8080/tap/availability
   curl -f http://localhost:8080/tap/capabilities
   curl -f http://localhost:8080/tap/tables
   curl -f -X POST -d 'QUERY=select * from TAP_SCHEMA.tables' http://localhost:8080/tap/sync
   curl -i -X POST -d 'QUERY=select * from TAP_SCHEMA.tables' http://localhost:8080/tap/async

The async request returns a redirect to a job initially in the ``PENDING``
phase. POST ``PHASE=RUN`` to its ``/phase`` URL, then poll that URL and fetch
``/results`` on completion. HTTP success alone does not establish that the
returned VOTable contains a successful query result. The repository's
``QueryTest`` and ``VOSIResourceTest`` provide further examples.




.. note::

   Generated API documentation for these classes is available in the
   `Java API reference <api/index.html>`_. Reusable library APIs are maintained
   in their respective upstream projects.
