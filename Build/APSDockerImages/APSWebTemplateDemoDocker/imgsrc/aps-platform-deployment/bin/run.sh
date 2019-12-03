#!/usr/bin/env bash
java -jar $(dirname $0)/aps-platform-booter-1.0.0.jar --dependenciesDir $(dirname $0)/../dependencies --bundlesDir $(dirname $0)/../bundles

# Trouble shooting (APS-211 : aps-config-manager must be started before bundles using it. )
#java -jar $(dirname $0)/aps-platform-booter-1.0.0.jar --dependenciesDir $(dirname $0)/../dependencies --bundlesDir $(dirname $0)/../bundles --order aps-json-lib-1.0.0.jar,aps-web-manager-1.0.0.jar,aps-config-manager-1.0.0.jar,aps-vertx-cluster-datastore-service-provider-1.0.0.jar,aps-core-lib-1.0.0.jar,aps-filesystem-service-provider-1.0.0.jar,aps-apis-1.0.0.jar,aps-vertx-provider-1.0.0.jar
