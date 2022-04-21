#!/usr/bin/env bash

DIRECTORY=plugins
rm -rf $DIRECTORY && mkdir -p $DIRECTORY && cp -f plugin-copy-to-clipboard/target/*fat.jar \
plugin-exec/target/*fat.jar \
plugin-gist/target/*fat.jar \
plugin-note/target/*fat.jar \
plugin-note-on-file/target/*fat.jar \
plugin-open-file/target/*fat.jar \
plugin-open-url/target/*fat.jar \
plugin-script/target/*fat.jar \
plugin-separator/target/*fat.jar \
plugin-show-image/target/*fat.jar \
plugin-show-image-svg/target/*fat.jar \
plugin-template/target/*fat.jar \
plugin-text/target/*fat.jar \
plugin-todoist/target/*fat.jar \
plugin-url-parser/target/*fat.jar \
$DIRECTORY
