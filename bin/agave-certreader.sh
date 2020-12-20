#!/usr/bin/env sh

# Lightweight wrapper around the graalvm binary that suppresses the oracle security warning
# when the crypto library is first loaded.
/bin/agave-certreader-graalvm-amd64 $@ 2>&1 | grep -v WARNING