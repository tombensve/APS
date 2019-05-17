#!/bin/sh
cd $(dirname $0)

# This needs doing the first time after checkout.
if [ ! -d ./node_modules ]; then
    npm install
fi

npm run build

