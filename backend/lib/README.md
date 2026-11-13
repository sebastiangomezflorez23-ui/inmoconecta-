# Controlador JDBC

Este avance fija MySQL Connector/J 9.7.0. El JAR descargado no se sube a Git.
Desde Git Bash, en la raíz del repositorio:

```bash
curl --fail --location https://repo.maven.apache.org/maven2/com/mysql/mysql-connector-j/9.7.0/mysql-connector-j-9.7.0.jar --output backend/lib/mysql-connector-j-9.7.0.jar
```

Fuente oficial: https://dev.mysql.com/doc/connector-j/en/connector-j-installing-maven.html
Solo usamos JDBC; no usamos X DevAPI ni necesitamos Maven para ejecutar.
