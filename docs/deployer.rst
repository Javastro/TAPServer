Deployer guide
==============

Before deploying
----------------

The checked-in configuration is an example. In particular,
``quarkus.hibernate-orm.schema-management.strategy=drop-and-create`` and the
corresponding ``uwsstore`` setting can delete and recreate database objects.
Do **not** use these settings against production data. Manage schema creation
and migrations separately and explicitly configure both persistence units,
for example:

.. code-block:: properties

   %prod.quarkus.hibernate-orm.schema-management.strategy=none
   %prod.quarkus.hibernate-orm."uwsstore".schema-management.strategy=none
   %prod.quarkus.hibernate-orm.scripts.generation=none
   %prod.quarkus.hibernate-orm.log.sql=false
   %prod.quarkus.hibernate-orm.log.bind-parameters=false

These settings do not create or migrate the schemas. Provision a compatible
PostgreSQL database, pgSphere extension, TAP schema/data and UWS job-store
tables first. The default datasource is shared with the UWS persistence unit;
configure a named datasource and point the ``uwsstore`` unit to it if
separation is needed. Supply credentials through a secret manager or
environment variables, not committed files. Replace the DemoDM VO-DML TAP
schema and ensure it describes the actual database.

Set ``ivoa.baseAddress`` to the externally visible application root, including
a trailing slash; generated service and capability URLs use it. Configure
reverse proxies and ingress consistently, and follow Quarkus guidance for
trusted proxy headers. The service needs a writable temporary directory for
jobs. The sample creates a new local directory at startup; database-backed job
metadata does not by itself make temporary files durable or shared between
replicas. Plan storage, cleanup and scaling accordingly.

Packaging options
-----------------

The application can be packaged in several ways

Jar with runner
^^^^^^^^^^^^^^^

 **JVM fast jar:** ``./gradlew build`` creates
  ``build/quarkus-app/quarkus-run.jar``. Deploy the entire
  ``build/quarkus-app`` directory, not just the runner, and run
  ``java -jar build/quarkus-app/quarkus-run.jar``.
* **JVM uber jar:** ``./gradlew build -Dquarkus.package.jar.type=uber-jar``
  creates a runner jar in ``build/``; run it with ``java -jar``.


Container
^^^^^^^^^^

The project is set up to produce a container image via Docker build. The container will run a JVM and the application in it. The image can be built with:

.. code-block:: sh

   ./gradlew build -Dquarkus.container-image.build=true

For example, after provisioning a database and production configuration:

.. code-block:: sh

   docker run --rm -p 8080:8080 \
     -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://db.example:5432/tap \
     -e QUARKUS_DATASOURCE_USERNAME=tap \
     -e QUARKUS_DATASOURCE_PASSWORD="$DB_PASSWORD" \
     -e IVOA_BASEADDRESS=https://tap.example.org/ \
     images.dev.uksrc.org/javastro/tap-server:0.10.1

The database hostname must resolve from inside the container. Provide the
production schema settings above through a mounted configuration file or
other Quarkus configuration source; the command alone does not make the
example configuration production-safe. Environment variables can be visible
to container administrators; use your platform's secret mechanism.

Native Image
^^^^^^^^^^^^

See also the `quarkus guide <https://quarkus.io/guides/building-native-image/>`_ for some
pre-requisites

.. code-block:: sh

   ./gradlew build -x test -Dquarkus.container-image.build=true -Dquarkus.package.jar.enabled=false

which will produce an executable in ``build/tap-server-0.10.1-runner``.
The native image can be run directly or packaged in a container.



Kubernetes and Helm
-------------------

The repository includes a Kubernetes ConfigMap fragment and Helm/ingress
properties. The build exposes a ``prepareHelmChart`` task and may generate
manifests and chart files under ``build/kubernetes`` and ``build/helm``.
Inspect the outputs of the actual build and enabled extensions before
deploying: the checked-in ConfigMap is not a complete deployment, and its
presence does not guarantee that its values are mounted or injected. Review
image name, ingress, secrets, database access and persistence in generated
resources before applying them. Quarkus' Kubernetes and Helm extension guides
describe their respective generation and configuration options.

Operations and checks
---------------------

Configure resource limits and TAP/UWS policies to match available capacity.
Review logs and protect them from exposing query parameters or credentials.
Check ``/tap/availability``, ``/tap/capabilities`` and ``/tap/tables``, then
submit a representative sync and async query as shown in :doc:`developer`.
The :doc:`configuration` reference describes application properties and
configuration sources.
