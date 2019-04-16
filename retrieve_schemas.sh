#!/bin/sh

echo "    Retrieving STIX schemas for v${1}..."
git submodule init
git submodule update --force
cd src/main/resources/schemas/v${1}
git checkout tags/v${1}

echo "    Retrieving CybOX schemas..."
git submodule init
git submodule update --force
cd cybox
git checkout 97beb32c376a9223e91b52cb3e4c8d2af6baf786

