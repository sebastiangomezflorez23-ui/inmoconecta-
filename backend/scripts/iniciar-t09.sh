#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
mkdir -p backend/lib backend/out
if [[ ! -f backend/lib/gson-2.14.0.jar ]]; then
  curl --fail --location https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.14.0/gson-2.14.0.jar --output backend/lib/gson-2.14.0.jar
fi
if [[ ! -f backend/lib/mysql-connector-j-9.7.0.jar ]]; then
  curl --fail --location https://repo.maven.apache.org/maven2/com/mysql/mysql-connector-j/9.7.0/mysql-connector-j-9.7.0.jar --output backend/lib/mysql-connector-j-9.7.0.jar
fi
export DB_USER=inmoconecta_app
IFS= read -r -s -p 'Contraseña del usuario inmoconecta_app: ' DB_PASSWORD
printf '\n'
export DB_PASSWORD
trap 'unset DB_PASSWORD' EXIT
find backend/src -name '*.java' > backend/out/fuentes.txt
javac --add-modules jdk.httpserver -encoding UTF-8 -cp 'backend/lib/*' -d backend/out @backend/out/fuentes.txt
java --add-modules jdk.httpserver -cp 'backend/out;backend/lib/*' com.inmoconecta.Servidor
