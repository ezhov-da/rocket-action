#!/usr/bin/env sh

RA_FOLDER_PATH=$ROCKET_ACTION

#echo $RA_FOLDER_PATH

if [[ -z "$RA_FOLDER_PATH" ]]
then
	echo "Please set 'ROCKET_ACTION' volder path"
else
	cp ./application-ui-swing/target/rocket-action.jar $RA_FOLDER_PATH
	echo "JAR copied successfully"
fi
