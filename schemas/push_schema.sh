curl -XPOST -H "Content-Type: application/vnd.schemaregistry.v1+json" http://schema-registry.cluster.local/subjects/temperature-value/versions/ -d '{ "schema": "{ \"type\": \"record\", \"namespace\": \"eu.mmacphail.data\", \"name\": \"Temperature\", \"fields\": [ {\"name\": \"station\", \"type\": \"string\"}, {\"name\": \"temperature\", \"type\": \"int\"} ]}" }'

