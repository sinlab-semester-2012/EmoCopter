#!/bin/sh

curdir="$(pwd)"
dir="$(dirname "$(readlink -f "$0")")"
cd "$dir"
cmake . && make && sudo make install
cd "$curdir"
