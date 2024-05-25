#!/usr/bin/env bash

# Copy all for prod usage

ROOT_ROCKET_ACTION=$ROCKET_ACTION

if [[ -z "$ROOT_ROCKET_ACTION" ]]
then
	echo "Please set 'ROCKET_ACTION' folder path to system environment"
	exit 1
fi

# Copy root application

cp ./application-ui-swing/target/rocket-action.jar $ROOT_ROCKET_ACTION

echo "Application copied successfully"
