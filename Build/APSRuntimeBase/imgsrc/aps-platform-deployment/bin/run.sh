#!/usr/bin/env bash
java -jar $(dirname $0)/aps-platform-booter-1.0.0.jar --dependenciesDir $(dirname $0)/../dependencies --bundlesDir $(dirname $0)/../bundles
