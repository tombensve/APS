#!/usr/bin/env bash
cat /etc/resolv.conf | grep nameserver | awk '{print $2}'
