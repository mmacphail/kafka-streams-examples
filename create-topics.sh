kafka-topics --bootstrap-server docker-desktop:31654 --create --topic temperature --replication-factor=1 --partitions 3
kafka-topics --bootstrap-server docker-desktop:31654 --create --topic temperature-max --replication-factor=1 --partitions 3 --config cleanup.policy=compact