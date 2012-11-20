#!/bin/sh

curdir="$(pwd)"
dir="$(dirname "$(readlink -f "$0")")"
# building files at server/
cd "$dir"/server/
cmake . && make && sudo make install
cd "$curdir"
