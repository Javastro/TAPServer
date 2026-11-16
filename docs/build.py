#!/usr/bin/env python3
"""Build the Sphinx site and its accompanying Java API documentation."""

from pathlib import Path
import shutil
import subprocess
import sys

ROOT = Path(__file__).resolve().parent.parent
SITE = ROOT / "build" / "docs" / "site"
JAVADOC = ROOT / "build" / "docs" / "javadoc"


def main():
    subprocess.run([str(ROOT / "gradlew"), "javadoc"], cwd=ROOT, check=True)
    if SITE.exists():
        shutil.rmtree(SITE)
    subprocess.run(
        [sys.executable, "-m", "sphinx", "-W", "--keep-going", "-b", "html",
         str(ROOT / "docs"), str(SITE)],
        cwd=ROOT,
        check=True,
    )
    shutil.copytree(JAVADOC, SITE / "api")
    if not (SITE / "api" / "index.html").is_file():
        raise FileNotFoundError("Javadoc index was not generated")


if __name__ == "__main__":
    main()
