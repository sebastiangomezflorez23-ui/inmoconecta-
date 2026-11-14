#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
mkdir -p backend/lib backend/out
if [[ ! -f backend/lib/gson-2.14.0.jar ]]; then
  curl --fail --location https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.14.0/gson-2.14.0.jar --output backend/lib/gson-2.14.0.jar
fi
find backend/src backend/test -name '*.java' > backend/out/fuentes.txt
javac --add-modules jdk.httpserver -encoding UTF-8 -cp 'backend/lib/*' -d backend/out @backend/out/fuentes.txt
java -Dfile.encoding=UTF-8 --add-modules jdk.httpserver -cp 'backend/out;backend/lib/*' com.inmoconecta.PruebasT09
