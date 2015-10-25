Materialist
===========

Builds and maintains materialized views from events stream stored in Kafka compacted topics (event sourcing). Allows to easily query and analyze data stored by applications which embraced the idea of distributed data (such as Samza).

Currently supports MongoDb as a target storage. Hence, while any value formats are supported, only JSON allows to use full power of MongoDb querying engine.

## Usage

First, you might want to fork this repo to keep your config files nearby.

In `/src/main/resources` there're several .conf files (HOCON). By convention, `application.conf` contains common and default values. These values can be overrided in `env.*.conf` files (one for each environment, like 'qa', 'live', etc.).

To build and pack everything with dependencies, run:

`sbt pack`

After successful build `target/pack` should contain everything needed to run the service.

To use settings for specific environment set `ENV` variable before start.

To run the service, simply run:

`bin/start` (or `bin/start.bat` if you're using Windows)

## Configuration

### groupings

### source

### target

## Limitations

- Both key and value must be UTF-8 strings.
- Kafka consumer currently uses single thread. In fact, the whole processing is done in single thread, except for the Kafka/MongoDb drivers.
- no metrics reporting yet

## License

Licensed under Apache 2.0 License.
