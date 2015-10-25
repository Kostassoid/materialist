Materialist
===========

Builds and maintains materialized views from events stream stored in [Kafka](http://kafka.apache.org/) compacted topics (event sourcing). Allows to easily query and analyze data stored by applications which embraced the idea of distributed data (such as [Samza](https://samza.apache.org/)).

Currently supports [MongoDb](https://www.mongodb.org/) as a target storage. Hence, while any value formats are supported, only JSON allows to use full power of MongoDb querying engine.

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

Groupings define which keys must be included or excluded. They also allow to specify group name or group pattern (group == MongoDb collection). Multiple groupings are allowed.

Key | Description | Example
----|-------------|--------
group.name | Fixed group name (plain string) | billing
group.pattern | Group pattern (regex) to dinamically extract group name from key | ^project-(\w+)$
allow | A list of patterns (regex). If any matches the key the key is allowed, if omitted any key is allowed | [ "^this", "that$"]
exclude | A list of patterns (regex). If any matches the key the key is excluded, if omitted no keys are excluded | [ ".*-secret$ ]

### source

### target

## Limitations

- Both key and value must be UTF-8 strings (no null keys obviously).
- Kafka consumer currently uses single thread. In fact, the whole processing is done in single thread, except for the Kafka/MongoDb drivers.
- no metrics reporting yet
- no key names conflict (same keys from different partitions) resolution yet

## License

Licensed under Apache 2.0 License.
