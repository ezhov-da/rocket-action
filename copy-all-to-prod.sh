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
plugin-copy-to-clipboard/target/*fat.jar \
plugin-exec/target/*fat.jar \
plugin-gist/target/*fat.jar \
plugin-note/target/*fat.jar \
plugin-note-on-file/target/*fat.jar \
plugin-open-file/target/*fat.jar \
plugin-open-url/target/*fat.jar \
plugin-separator/target/*fat.jar \
plugin-show-image/target/*fat.jar \
plugin-show-image-svg/target/*fat.jar \
plugin-template/target/*fat.jar \
plugin-text/target/*fat.jar \
plugin-todoist/target/*fat.jar \
plugin-url-parser/target/*fat.jar \
plugin-jira/target/*fat.jar \
plugin-temporary-file/target/*fat.jar \
$PLUGINS_DIRECTORY

# Copy script plugins to extension

PLUGINS_EXT_DIRECTORY=${ROOT_ROCKET_ACTION}/plugins-ext
rm -rf $PLUGINS_EXT_DIRECTORY && mkdir -p $PLUGINS_EXT_DIRECTORY && cp -f \
plugin-script/target/*fat.jar \
$PLUGINS_EXT_DIRECTORY

# Copy root application

cp ./application-ui-swing/target/rocket-action.jar $ROOT_ROCKET_ACTION

echo "All JARs copied successfully"
