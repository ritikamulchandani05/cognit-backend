#!/bin/sh
set -e

# Railway (and Docker named volumes) mount as root:root. The app runs as the
# unprivileged `spring` user and needs to create subfolders under the upload
# dir, so take ownership of the mount before starting.
UPLOAD_DIR="${FILE_UPLOAD_DIR:-/app/uploads}"
mkdir -p "$UPLOAD_DIR"
chown -R spring:spring "$UPLOAD_DIR" 2>/dev/null || true

exec su-exec spring:spring sh -c 'java $JAVA_OPTS -jar app.jar'
