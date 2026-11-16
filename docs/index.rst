TAPServer documentation
=======================

TAPServer is a Quarkus application implementing the
`IVOA Table Access Protocol (TAP). <https://www.ivoa.net/documents/TAP/>`_
Most TAP and UWS functionality is implemented in the
`ivoacore <https://github.com/Javastro/ivoacore>`_ and
`quarkus-tap-lib <https://github.com/Javastro/quarkusIVOA/tree/main/quarkus-tap-lib>`_
libraries. This repository contains a working example of a TAP service, with five top-level classes
that wire the libraries together to make a TAP server. This makes it easy to start a new TAP service by copying the build scaffolding and the five classes into a new project, then adding your own data model and configuration. See :doc:`developer` for a guide to starting a new service.

.. toctree::
   :maxdepth: 2

   deployer
   database
   developer
   configuration

`Java API reference <api/index.html>`_ documents this project's
``org.javastro.ivoa.tap`` classes. The example entities package is excluded.
