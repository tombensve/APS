#!/usr/bin/env bash

vertxver=3.8.0
nettyver=4.1.19.Final
groovyver=2.5.7 #3.0.0-beta-3
hazelcastver=3.12.4
jacksonannotationsver=2.9.0
jacksoncorever=2.9.0

dl=$(dirname $0)/../../../bin/downloadJar.sh
bundles=$(dirname $0)/../imgsrc/aps-platform-deployment/bundles
deps=$(dirname $0)/../imgsrc/aps-platform-deployment/dependencies
bin=$(dirname $0)/../imgsrc/aps-platform-deployment/bin

# Base
${dl} io.vertx                   vertx-core          ${vertxver}              ${deps}
${dl} io.vertx                   vertx-web           ${vertxver}              ${deps}
${dl} io.vertx                   vertx-web-common    ${vertxver}              ${deps}
${dl} io.vertx                   vertx-hazelcast     ${vertxver}              ${deps}
${dl} io.vertx                   vertx-codegen       ${vertxver}              ${deps}
${dl} io.vertx                   vertx-health-check  ${vertxver}              ${deps}
${dl} io.vertx                   vertx-jdbc-client   ${vertxver}              ${deps}
${dl} io.vertx                   vertx-lang-groovy   ${vertxver}              ${deps}
${dl} io.vertx                   vertx-bridge-common ${vertxver}              ${deps}
${dl} io.vertx                   vertx-amqp-bridge   ${vertxver}              ${deps}
${dl} io.vertx                   vertx-amqp-client   ${vertxver}              ${deps}
${dl} io.vertx                   vertx-auth-common   ${vertxver}              ${deps}

${dl} io.netty                   netty-handler       ${nettyver}              ${deps}
${dl} io.netty                   netty-handler-proxy ${nettyver}              ${deps}
${dl} io.netty                   netty-codec         ${nettyver}              ${deps}
${dl} io.netty                   netty-codec-socks   ${nettyver}              ${deps}
${dl} io.netty                   netty-codec-http    ${nettyver}              ${deps}
${dl} io.netty                   netty-codec-http2   ${nettyver}              ${deps}
${dl} io.netty                   netty-codec-dns     ${nettyver}              ${deps}
${dl} io.netty                   netty-resolver      ${nettyver}              ${deps}
${dl} io.netty                   netty-resolver-dns  ${nettyver}              ${deps}
${dl} io.netty                   netty-buffer        ${nettyver}              ${deps}
${dl} io.netty                   netty-common        ${nettyver}              ${deps}
${dl} io.netty                   netty-transport     ${nettyver}              ${deps}
#${dl} io.netty                   netty-transport-native-epoll ${nettyver}        ${deps}
#${dl} io.netty                   netty-transport-native-kqueue ${nettyver}       ${deps}
#${dl} io.netty                   netty-transport-native-unix-common  ${nettyver} ${deps}

${dl} org.codehaus.groovy        groovy              ${groovyver}             ${deps}

${dl} com.hazelcast              hazelcast           ${hazelcastver}          ${deps}

${dl} com.fasterxml.jackson.core jackson-annotations ${jacksonannotationsver} ${deps}
${dl} com.fasterxml.jackson.core jackson-core        ${jacksoncorever}        ${deps}
${dl} com.fasterxml.jackson.core jackson-databind    ${jacksoncorever}        ${deps}

${dl} com.fasterxml.jackson.jr   jackson-jr-all      2.9.6                    ${deps}

#${dl} org.jline                  jline               3.7.0                    ${deps}
#${dl} org.fusesource.jansi       jansi               1.17.1                   ${deps}

${dl} org.osgi                   org.osgi.core       4.2.0                    ${deps}
${dl} org.osgi                   org.osgi.compendium 4.2.0                    ${deps}

#${dl} org.apache.geronimo.specs geronimo-jta_1.1_spec 1.1.1                   ${deps}

${dl} se.natusoft.osgi.aps       aps-runtime         1.0.0                    ${deps}
${dl} se.natusoft.osgi.aps       aps-platform-booter 1.0.0                    ${bin} shaded

# APS stuff (these do not exist in jcenter! This works due to these being built before
# this is run, and thus existing in ~/.mvn/repository which is looked in before trying
# to download from jcenter).
${dl} se.natusoft.osgi.aps       aps-apis                                     1.0.0  ${bundles}
${dl} se.natusoft.osgi.aps       aps-config-manager                            1.0.0  ${bundles}
${dl} se.natusoft.osgi.aps       aps-core-lib                                 1.0.0  ${bundles}
${dl} se.natusoft.osgi.aps       aps-json-lib                                 1.0.0  ${bundles}
${dl} se.natusoft.osgi.aps       aps-vertx-provider                           1.0.0  ${bundles}
${dl} se.natusoft.osgi.aps       aps-vertx-cluster-datastore-service-provider 1.0.0  ${bundles}
${dl} se.natusoft.osgi.aps       aps-filesystem-service-provider               1.0.0  ${bundles}
${dl} se.natusoft.osgi.aps       aps-web-manager                              1.0.0  ${bundles}
