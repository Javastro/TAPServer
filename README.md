# tap-server

This project creates a generic TAP server 

This project uses Quarkus, If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

[![Java CI with Gradle](https://github.com/Javastro/TAPServer/actions/workflows/gradle.yml/badge.svg)](https://github.com/Javastro/TAPServer/actions/workflows/gradle.yml)

Most of the functionality in this server comes from dependencies on the [ivoacore libraries](https://github.com/Javastro/ivoacore), which 
contains the framework independent code, and the [quarkus-tap-lib](https://github.com/Javastro/quarkusIVOA/tree/main/quarkus-tap-lib)  some quarkus 
specific functionality that should be re-used in another Quarkus-based TAP implementation.

## Documentation

The [documentation site](https://javastro.github.io/TAPServer/) covers developing and deploying a TAP service and includes this project's Java API.
To build it locally, install Python 3.11 or later and JDK 17, then run:

```shell
python3 -m venv .venv
. .venv/bin/activate
python -m pip install -r docs/requirements.txt
python docs/build.py
```

Open `build/docs/site/index.html` in a browser. The documentation build also runs the Gradle Javadoc task.
The documentation workflow uploads the site on pushes to `main`; after it
succeeds, the separate publishing workflow deploys that artifact to GitHub Pages. In
repository settings, set Pages' build and deployment source to **GitHub Actions**
and allow the `github-pages` environment and Pages deployment permissions.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

