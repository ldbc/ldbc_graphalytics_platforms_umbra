#!/bin/bash

set -e

# Ensure the configuration file exists
if [ "$(uname)" == "Darwin" ]; then
  rootdir=$(dirname $(greadlink -f ${BASH_SOURCE[0]}))/../..
else
  rootdir=$(dirname $(readlink -f ${BASH_SOURCE[0]}))/../..
fi
config="${rootdir}/config"
if [ ! -f "${config}/platform.properties" ]; then
	echo "Missing mandatory configuration file: ${config}/platform.properties" >&2
	exit 1
fi
