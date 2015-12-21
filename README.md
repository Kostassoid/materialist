Materialist [![Build Status](https://travis-ci.org/Kostassoid/materialist.svg)](https://travis-ci.org/Kostassoid/materialist)
===========

Builds and maintains materialized views from changelog stream stored in [Kafka](http://kafka.apache.org/) compacted topics (event sourcing). Allows to easily query and analyze data stored by applications which embraced the idea of distributed data (such as [Samza](https://samza.apache.org/)).

Currently supports [MongoDb](https://www.mongodb.org/) as a target storage. Hence, while any value formats are supported, only JSON will allow to use full power of MongoDb query engine.

## Usage

First, you might want to fork this repo to keep your config files nearby.

In `/src/main/resources` there're several .conf files ([HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md)). By convention, `application.conf` contains common and default values. These values can be overrided in `env.*.conf` files (one for each environment, like 'qa', 'live', etc.).

To build and pack everything with dependencies, run:

`sbt pack`

After successful build `target/pack` should contain everything needed to run the service.

To use settings for specific environment set `ENV` variable before start.

To run the service, simply run:

`bin/start` (or `bin/start.bat` if you're using Windows)

## Configuration

### routes

Routes define the source and the destination of data. Each route is processed separately.

Key | Description | Example
----|-------------|--------
from | Source stream name (e.g. topic for Kafka source) | billing
to | Target stream name. If omitted then same as "from" |
match.key | A regex pattern to match keys. If omitted any key is allowed. | "^this"

### source

Key | Description | Example
----|-------------|--------
factory.class | Factory class for source adapter. Currently only Kafka is supported. | com.kostassoid.materialist.KafkaSourceFactory
kafka.consumer | See [Kafka docs](http://kafka.apache.org/documentation.html#consumerconfigs) |

### target

Key | Description | Example
----|-------------|--------
factory.class | Factory class for target adapter. Currently only MongoDb is supported. | com.kostassoid.materialist.MongoDbTargetFactory
batch.size | Max # of messages in one batch | 1000
mongodb.connection | Connection string | mongodb://localhost:27017
mongodb.database | Database name | materialist

## Rebuilding views

When routes have been changed, it is most likely that the views have to be rebuilt from scratch. To do this, simply remove collections from MongoDb and current offsets from ZooKeeper (using correct Zk host and Kafka group.id):

`zkCli.sh -server localhost:2181 rmr /kafka/consumers/materialist-$topic`

## Limitations

- Both key and value must be UTF-8 strings (no null keys obviously).
- Kafka consumer currently uses single thread per topic.
- no metrics reporting yet
- no key names conflict (same keys from different partitions) resolution yet

## License

Licensed under Apache 2.0 License.
