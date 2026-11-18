#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
echo 'Prueba MySQL real: requiere migración 003 y crea dos usuarios ficticios de evidencia.'
export DB_USER=inmoconecta_app
IFS= read -r -s -p 'Contraseña MySQL (no se muestra): ' DB_PASSWORD
printf '\n'
export DB_PASSWORD
trap 'unset DB_PASSWORD' EXIT
mkdir -p backend/out
find backend/src backend/test -name '*.java' > backend/out/fuentes.txt
javac --add-modules jdk.httpserver -encoding UTF-8 -cp 'backend/lib/*' -d backend/out @backend/out/fuentes.txt
java -Dfile.encoding=UTF-8 -cp 'backend/out;backend/lib/*' com.inmoconecta.PruebasAccesoMysql
