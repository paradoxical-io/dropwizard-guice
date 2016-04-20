#!/usr/bin/env bash

set -x

if [ -n "$TRAVIS_TAG" ]; then
    mvn gpg:sign deploy --settings settings.xml -DskipTests -P sign
elif [ "$TRAVIS_BRANCH" = "master" ]; then
    mvn deploy --settings settings.xml -DskipTests
fi
