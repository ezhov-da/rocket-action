#!/usr/bin/env bash

# Copy all for prod usage

ROOT_ROCKET_ACTION=$ROCKET_ACTION

if [[ -z "$ROOT_ROCKET_ACTION" ]]
then
	echo "Please set 'ROCKET_ACTION' folder path to system environment"
	exit 1
fi

# Copy common plugins

PLUGINS_DIRECTORY=${ROOT_ROCKET_ACTION}/plugins

rm -rf $PLUGINS_DIRECTORY && mkdir -p $PLUGINS_DIRECTORY && cp -f \
plugins/copy-to-clipboard/build/libs/*fat.jar \
plugins/exec/build/libs/*fat.jar \
plugins/gist/build/libs/*fat.jar \
plugins/note/build/libs/*fat.jar \
plugins/note-on-file/build/libs/*fat.jar \
plugins/open-file/build/libs/*fat.jar \
plugins/open-url/build/libs/*fat.jar \
plugins/separator/build/libs/*fat.jar \
plugins/show-image/build/libs/*fat.jar \
plugins/show-image-svg/build/libs/*fat.jar \
plugins/template/build/libs/*fat.jar \
plugins/text/build/libs/*fat.jar \
plugins/todoist/build/libs/*fat.jar \
plugins/url-parser/build/libs/*fat.jar \
plugins/jira/build/libs/*fat.jar \
plugins/temporary-file/build/libs/*fat.jar \
$PLUGINS_DIRECTORY

# Copy script plugins to extension

PLUGINS_EXT_DIRECTORY=${ROOT_ROCKET_ACTION}/plugins-ext
rm -rf $PLUGINS_EXT_DIRECTORY && mkdir -p $PLUGINS_EXT_DIRECTORY && cp -f \
plugins/script/build/libs/*fat.jar \
$PLUGINS_EXT_DIRECTORY

# Copy root application

cp ./application-ui-swing/build/libs/rocket-action.jar $ROOT_ROCKET_ACTION

echo "All JARs copied successfully"
