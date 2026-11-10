#!/usr/bin/env bash

# Copy plugins for develop

DIRECTORY=plugin-jars
rm -rf $DIRECTORY && mkdir -p $DIRECTORY && cp -f plugins/copy-to-clipboard/build/libs/*fat.jar \
plugins/exec/build/libs/*fat.jar \
plugins/gist/build/libs/*fat.jar \
plugins/note/build/libs/*fat.jar \
plugins/note-on-file/build/libs/*fat.jar \
plugins/open-file/build/libs/*fat.jar \
plugins/open-url/build/libs/*fat.jar \
plugins/script/build/libs/*fat.jar \
plugins/separator/build/libs/*fat.jar \
plugins/show-image/build/libs/*fat.jar \
plugins/show-image-svg/build/libs/*fat.jar \
plugins/template/build/libs/*fat.jar \
plugins/text/build/libs/*fat.jar \
plugins/todoist/build/libs/*fat.jar \
plugins/url-parser/build/libs/*fat.jar \
plugins/jira/build/libs/*fat.jar \
plugins/temporary-file/build/libs/*fat.jar \
$DIRECTORY
