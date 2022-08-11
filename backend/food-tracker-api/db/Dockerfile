FROM postgres:13.1-alpine

EXPOSE 5432

COPY schema.sql /docker-entrypoint-initdb.d/1-schema.sql
COPY data.sql /docker-entrypoint-initdb.d/2-data.sql