# Jet CDC Tutorial

The `cdc_tutorial` bundle wraps the CDC tutorial available from the Hazelcast site [https://jet-start.sh/docs/tutorials/cdc](https://jet-start.sh/docs/tutorials/cdc).

## Installing Bundle

```console
install_bundle -download jet-4-docker-cdc_tutorial
```

## Use Case

This use case introduces the Debezium MySQL connector as a Jet job for ingesting database CDC records into a Jet cluster.

![CDC Tutorial Data Flow](/images/jet-cdc-tutorial.png)

## Required Software

- Docker

## Tutorial Link

The following link provides detailed explanations to the data flow and source code.

[https://jet-start.sh/docs/tutorials/cdc](https://jet-start.sh/docs/tutorials/cdc)

## Building App

```console
cd_docker cdc_tutorial; cd bin_sh
./build_app
```

## Starting MySQL Docker Container

If you have your own MySQL running, stop it first before starting the `mysql` container.

```console
cd_docker cdc_tutorial; cd bin_sh

# Start mysql container
./start_mysql
```

## Starting Jet Cluster

We can start a Jet cluster on the host machine or Docker containers.

### Starting Jet Cluster on Host Machine

First, create a Jet cluster with the default settings. Any Jet cluster with the default settings will work.

```console
create_cluster -cluster cdc_jet
```

Start the cluster.

```console
switch_cluster cdc_jet
start_cluster
```

### Starting Jet Cluster on Docker Containers

The `create_docker` command automatically selects a host IP address but it may not be accessible from the Docker containers if you have multiple addresses assigned to the host. You can specify the host IP address using the `-host` option.

If you are running Docker Desktop then you can specify the `-host` option with `host.docker.internal`, which resolves to the internal IP address used by the host. Otherwise, specify the IP address that the Docker containers have access to.

```console
create_docker -cluster cdc_jet -host host.docker.internal
```

:exclamation: The Jet's `slf4j` library might not be compatible with the version that the tutorial build packaged in the `cdc-tutorial-1.0-SNAPSHOT.jar` file. To avoid this problem, let's copy the jar file in the Jet cluster `plugins` directory.

```console
cd_docker cdc_jet
cp ../cdc_tutorial/target/cdc-tutorial-1.0-SNAPSHOT.jar hazelcast-addon/plugins/
```

Start the cluster.

```console
cd_docker cdc_jet
docker-compose up
```

## Submitting Debezium Connector Job

The default Jet cluster has been configured with the start port, 6701. Unless the cluster has been configured with the default port, 5701, we need to specify the port number when submitting a job.

The job jar we have built takes the host IP address as the optional argument. If you don't specify the argument, then it defaults to `localhost` (See the source code: `src/main/java/org/example/JetJob.java`). As mentioned before, the host IP must be accessible from the Docker containers.

```console
cd_docker cdc_tutorial

# For host IP: localhost (if running on host machine)
jet --addresses localhost:6701 submit target/cdc-tutorial-1.0-SNAPSHOT.jar

# For Docker Desktop: host.docker.internal
jet --addresses localhost:6701 submit target/cdc-tutorial-1.0-SNAPSHOT.jar host.docker.internal

# For others:
jet --addresses localhost:6701 submit target/cdc-tutorial-1.0-SNAPSHOT.jar <host_ip>
```

Upon submitting the connector, the `customers` table records will be populated in the `customers` map. You can view the `customers` map by running the `read_cache` script.

```console
cd_docker cdc_tutorial; cd bin_sh
./read_cache
```

**Output:**

```console
Currently there are following customers in the cache:
        Customer {id=1002, firstName=George, lastName=Bailey, email=gbailey@foobar.com}
        Customer {id=1001, firstName=Sally, lastName=Thomas, email=sally.thomas@acme.com}
        Customer {id=1003, firstName=Edward, lastName=Walker, email=ed@walker.com}
        Customer {id=1004, firstName=Anne, lastName=Kretchmar, email=annek@noanswer.org}
```

## Updating Database

```console
cd_docker cdc_tutorial; cd bin_sh 
./start_mysql_cli
```

From the CLI, use the `inventory` database and view the `customers` table.

```console
mysql> use inventory;
mysql> select * from customers;
```

**Output:**

```console
+------+------------+-----------+-----------------------+
| id   | first_name | last_name | email                 |
+------+------------+-----------+-----------------------+
| 1001 | Sally      | Thomas    | sally.thomas@acme.com |
| 1002 | George     | Bailey    | gbailey@foobar.com    |
| 1003 | Edward     | Walker    | ed@walker.com         |
| 1004 | Anne       | Kretchmar | annek@noanswer.org    |
+------+------------+-----------+-----------------------+
```

From the CLI, update the `customers` table.

```console
mysql> UPDATE customers SET first_name='Anne Marie' WHERE id=1004;
```

## Viewing Data Updates

Let's take a look at the `customers` map in the Jet cluster.

```console
cd_docker cdc_tutorial; cd bin_sh
./read_cache
```

**Output:**

```console
Currently there are following customers in the cache:
        Customer {id=1002, firstName=George, lastName=Bailey, email=gbailey@foobar.com}
        Customer {id=1001, firstName=Sally, lastName=Thomas, email=sally.thomas@acme.com}
        Customer {id=1003, firstName=Edward, lastName=Walker, email=ed@walker.com}
        Customer {id=1004, firstName=Anne Marie, lastName=Kretchmar, email=annek@noanswer.org} 
```

Note that the `firstName` field is changed from `Anne` to `Anne Marie` for `id=1004`.

## Tearing Down

```console
# Stop cluster (if running on host machine)
stop_cluster

# Stop cluster (if running on Docker containers)
# Ctrl-C Docker Compose

# Stop and prune Docker containers
cd_docker cdc_tutorial; cd bin_sh
./cleanup
```
