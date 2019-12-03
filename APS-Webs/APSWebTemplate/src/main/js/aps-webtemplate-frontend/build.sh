#!/bin/sh
#
# At least on Mac OS X npm must be downloaded and installed manually. Its an .pkg file that
# needs to be approved by "Security & Integrity" in system setting and then manually
# installed. Can't script a download and unpack.
#
cd $(dirname $0)

# This needs doing the first time after checkout.
if [[ ! -d ./node_modules ]]; then
    npm install
fi

npm run build

