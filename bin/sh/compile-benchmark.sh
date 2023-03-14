#!/bin/bash

set -e

# Ensure the configuration file exists
rootdir="$( cd "$( dirname "${BASH_SOURCE[0]:-${(%):-%x}}" )" >/dev/null 2>&1 && pwd )/../.."
config="${rootdir}/config"

if [ ! -f "${config}/platform.properties" ]; then
	echo "Missing mandatory configuration file: ${config}/platform.properties" >&2
	exit 1
fi
