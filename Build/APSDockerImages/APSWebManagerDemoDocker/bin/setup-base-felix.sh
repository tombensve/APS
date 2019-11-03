#!/usr/bin/env bash

felixver=6.0.3
vertxver=3.8.0
nettyver=4.1.19.Final
groovyver=2.5.7
#3.0.0-beta-3
hazelcastver=3.12.4
jacksonannotationsver=2.9.0
jacksoncorever=2.9.0

felixunpack=$(dirname $0)/../imgsrc/felix-framework-${felixver}

if [ ! -d ${felixunpack} ]; then
    (
      cd $(dirname $0)/../imgsrc; unzip felix-framework-${felixver}
    )
fi

dl=$(dirname $0)/../../../bin/downloadJar.sh
#target=$(dirname $0)/../imgsrc/felix-framework-${felixver}/bundle
target=$(dirname $0)/../imgsrc/classpath

#${dl} io.vertx                   vertx-core          ${vertxver}              ${target}
#${dl} io.vertx                   vertx-auth-common   ${vertxver}              ${target}
#${dl} io.vertx                   vertx-web           ${vertxver}              ${target}
#${dl} io.vertx                   vertx-web-common    ${vertxver}              ${target}
#${dl} io.vertx                   vertx-hazelcast     ${vertxver}              ${target}
#${dl} io.vertx                   vertx-codegen       ${vertxver}              ${target}
#${dl} io.vertx                   vertx-health-check  ${vertxver}              ${target}
#${dl} io.vertx                   vertx-jdbc-client   ${vertxver}              ${target}
#${dl} io.vertx                   vertx-lang-groovy   ${vertxver}              ${target}
#${dl} io.vertx                   vertx-bridge-common ${vertxver}              ${target}

${dl} io.netty                   netty-handler       ${nettyver}              ${target}
${dl} io.netty                   netty-handler-proxy ${nettyver}              ${target}
${dl} io.netty                   netty-codec         ${nettyver}              ${target}
${dl} io.netty                   netty-codec-socks   ${nettyver}              ${target}
${dl} io.netty                   netty-codec-http    ${nettyver}              ${target}
${dl} io.netty                   netty-codec-http2   ${nettyver}              ${target}
${dl} io.netty                   netty-codec-dns     ${nettyver}              ${target}
${dl} io.netty                   netty-resolver      ${nettyver}              ${target}
${dl} io.netty                   netty-resolver-dns  ${nettyver}              ${target}
${dl} io.netty                   netty-buffer        ${nettyver}              ${target}
${dl} io.netty                   netty-common        ${nettyver}              ${target}
${dl} io.netty                   netty-transport     ${nettyver}              ${target}
${dl} io.netty                   netty-transport-native-epoll ${nettyver}              ${target}
${dl} io.netty                   netty-transport-native-kqueue ${nettyver}              ${target}
${dl} io.netty                   netty-transport-native-unix-common  ${nettyver}              ${target}

${dl} org.codehaus.groovy        groovy              ${groovyver}             ${target}

${dl} com.hazelcast              hazelcast           ${hazelcastver}          ${target}

${dl} com.fasterxml.jackson.core jackson-annotations ${jacksonannotationsver} ${target}
${dl} com.fasterxml.jackson.core jackson-core        ${jacksoncorever}        ${target}
${dl} com.fasterxml.jackson.core jackson-databind    ${jacksoncorever}        ${target}

${dl} com.fasterxml.jackson.jr   jackson-jr-all      2.9.6                    ${target}

${dl} org.osgi org.osgi.core 4.2.0 ${target}
${dl} org.osgi org.osgi.compendium 4.2.0 ${target}

#${dl}  $(dirname $0)/../imgsrc/felix-framework-${felixver}/bundle
