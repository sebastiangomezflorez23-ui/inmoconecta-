#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
if [[ ! -f backend/lib/mysql-connector-j-9.7.0.jar ]]; then
  curl --fail --location https://repo.maven.apache.org/maven2/com/mysql/mysql-connector-j/9.7.0/mysql-connector-j-9.7.0.jar --output backend/lib/mysql-connector-j-9.7.0.jar
fi
export DB_USER=inmoconecta_app
read -r -s -p 'Contraseña del usuario inmoconecta_app: ' DB_PASSWORD
printf '\n'
export DB_PASSWORD
trap 'unset DB_PASSWORD' EXIT
java --add-modules jdk.httpserver -cp 'backend/out;backend/lib/*' com.inmoconecta.Servidor
