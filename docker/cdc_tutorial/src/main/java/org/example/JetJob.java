package org.example;

import com.hazelcast.jet.Jet;
import com.hazelcast.jet.Util;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.contrib.debezium.DebeziumSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.ServiceFactories;
import com.hazelcast.jet.pipeline.ServiceFactory;
import com.hazelcast.jet.pipeline.Sinks;
import io.debezium.config.Configuration;
import io.debezium.serde.DebeziumSerdes;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.connect.data.Values;

import java.util.Collections;

public class JetJob {

    public static void main(String[] args) {
        String dbHost;
        if (args.length > 0) {
            dbHost = args[0];
        } else {
            dbHost = "localhost";
        }
        Configuration configuration = Configuration
                .create()
                .with("name", "mysql-inventory-connector")
                .with("connector.class", "io.debezium.connector.mysql.MySqlConnector")
                .with("database.hostname", dbHost)
                .with("database.port", 3306)
                .with("database.user", "debezium")
                .with("database.password", "dbz")
                .with("database.server.id", "184054")
                .with("database.server.name", "dbserver1")
                .with("database.whitelist", "inventory")
                .with("table.whitelist", "inventory.customers")
                .with("include.schema.changes", "false")
                .with("database.history.hazelcast.list.name", "test")
                .build();

        ServiceFactory<?, Serde<Customer>> serdeFactory =
            ServiceFactories.nonSharedService(cntx -> {
                Serde<Customer> serde = DebeziumSerdes.payloadJson(Customer.class);
                serde.configure(Collections.singletonMap("from.field", "after"), false);
                return serde;
            });

        Pipeline pipeline = Pipeline.create();
        pipeline.readFrom(DebeziumSources.cdc(configuration))
                .withoutTimestamps()
                .filter(r -> r.topic().equals("dbserver1.inventory.customers"))
                .map(record -> Values.convertToString(record.valueSchema(), record.value()))
                .mapUsingService(
                        serdeFactory,
                        (serde, json) -> {
                            Customer customer = serde.deserializer()
                                    .deserialize("topic", json.getBytes());
                            return Util.entry(customer.id, customer);
                        })
                .peek()
                .writeTo(Sinks.map("customers"));

        JobConfig cfg = new JobConfig().setName("mysql-monitor");
        Jet.bootstrappedInstance().newJob(pipeline, cfg);
    }

}
