Database Configuration
======================

Allowing a database to be queried with ADQL is the primary purpose of a TAP service.
Supplying the necessary metadata to enable the TAP service is core to this functionality.
The implementation expects this metadata to be presented to it in the form of a `TAPSchemaDM <https://ivoa.github.io/TAPSchemaDM/>`_ model instance.

There are at
least two different approaches to this dependent on whether the database that is to be served
already exists - these two board use-cases are covered in the following sections.


Pre-existing Database
---------------------

If the database already exists, the TAP service can be configured to use it by providing a connection to it and a mapping of the database schema to the TAPSchemaDM model.
The mapping is provided in the form of a VO-DML model instance that describes the database schema.
The VO-DML model can be generated from the database schema using the instructions for the `TAPSchemaDM creation tool <https://ivoa.github.io/TAPSchemaDM/#generating-a-tapschema-instance-from-exiting-database>`_.
Once you have the bare TAPSchemaDM model, you should add the additional metadata required by the TAP service. Normally this will consist of;


* Descriptions
* UCDs
* Utypes


At this point you should have an XML instance that can then be put in ``src/main/resources`` and the
``ivoa.tap.schema`` property in the ``src/main/resources/application.properties``
file should be updated to point to the VO-DML model instance filename (no path).


A new Datamodel
---------------

If you are creating a new database one of the easiest routes is to define the datamodel directly in VO-DML
using the `VO-DML tools <https://ivoa.github.io/vo-dml/>`_. This repository has been created as an example using the `DemoDM <https://github.com/Javastro/DemoModel>`_ datamodel.
The VO-DML model can be used to generate the database schema and the TAPSchemaDM model instance via the generated
code in the VO-DML during the development phase - Note that in this case the VO-DML model contains all of the extra
metadata that TAP needs (cf `above <Pre-existing Database>`_). The generated jar for the VO-DML is made a build dependency.