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

### groupings

Groupings define how source keys should be distributed among groups (i.e. MongoDb collections). They also allow to filter out the keys you're not interested in. You can choose between fixed and dynamically resolved group names. Multiple groupings per process are allowed.

Key | Description | Example
----|-------------|--------
group.name | Fixed group name (plain string) | billing
group.pattern | Group pattern (regex) to dinamically extract group name from key | ^project-(\w+)$
allow | A list of patterns (regex) for allowed keys. If omitted any key is allowed | [ "^this", "that$"]
exclude | A list of patterns (regex) for excluded keys. If omitted no keys are excluded | [ ".*-secret$" ]

### source

Key | Description | Example
----|-------------|--------
factory.class | Factory class for source adapter. Currently only Kafka is supported. | com.kostassoid.materialist.KafkaSourceFactory
batch.size | Max # of messages in one batch | 1000
batch.wait.ms | Max time to wait before sending next batch | 1000
kafka.topc | Source topic name | changelog-topic
kafka.consumer.* | See [Kafka docs](http://kafka.apache.org/documentation.html#consumerconfigs) |

### target

Key | Description | Example
----|-------------|--------
factory.class | Factory class for target adapter. Currently only MongoDb is supported. | com.kostassoid.materialist.MongoDbTargetFactory
mongodb.connection | Connection string | mongodb://localhost:27017
mongodb.database | Database name | materialist

## Rebuilding views

When groupings settings have been changed, it is most likely that the views have to be rebuilt from scratch. To do this, simply remove collections from MongoDb and current offsets from ZooKeeper (using correct Zk host and Kafka group.id):

`zkCli.sh -server localhost:2181 rmr /kafka/consumers/materialist`

## Limitations

- Both key and value must be UTF-8 strings (no null keys obviously).
- Kafka consumer currently uses single thread. In fact, the whole processing is done in single thread, except for the Kafka/MongoDb drivers.
- no metrics reporting yet
- no key names conflict (same keys from different partitions) resolution yet

## License

Licensed under Apache 2.0 License.
