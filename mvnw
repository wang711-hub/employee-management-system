#!/usr/bin/env sh
set -eu
PROJECT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MAVEN_VERSION=3.9.16
MAVEN_ROOT="$PROJECT_DIR/.mvn/wrapper/dists/apache-maven-$MAVEN_VERSION"
MAVEN_HOME="$MAVEN_ROOT/apache-maven-$MAVEN_VERSION"

if ! command -v java >/dev/null 2>&1; then
  echo "Java was not found. Install JDK 21 and set JAVA_HOME or add java to PATH." >&2
  exit 1
fi

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  command -v curl >/dev/null 2>&1 || { echo "curl is required to download Maven." >&2; exit 1; }
  mkdir -p "$MAVEN_ROOT"
  archive="$MAVEN_ROOT/apache-maven-$MAVEN_VERSION-bin.zip"
  url="https://dlcdn.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip"
  curl -fsSL "$url" -o "$archive"
  curl -fsSL "$url.sha512" -o "$archive.sha512"
  expected=$(awk '{print tolower($1)}' "$archive.sha512")
  if command -v sha512sum >/dev/null 2>&1; then
    actual=$(sha512sum "$archive" | awk '{print tolower($1)}')
  else
    actual=$(shasum -a 512 "$archive" | awk '{print tolower($1)}')
  fi
  [ "$actual" = "$expected" ] || { echo "Apache Maven checksum verification failed." >&2; exit 1; }
  unzip -oq "$archive" -d "$MAVEN_ROOT"
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
