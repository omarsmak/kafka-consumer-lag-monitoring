Kafka Consumer Lag Monitoring - Lightweight and Cloud Native Ready
====
[![Build Status](https://travis-ci.com/omarsmak/kafka-consumer-lag-monitoring.svg?token=ACVRSYGMw5EM3tmwPiBz&branch=master)](https://travis-ci.com/omarsmak/kafka-consumer-lag-monitoring)
 ![Download](https://maven-badges.herokuapp.com/maven-central/com.omarsmak.kafka/consumer-lag-monitoring/badge.svg) 


A client tool that exports the consumer lag of a Kafka consumer group to different implementations such as Prometheus or your terminal. It utlizes Kafka's AdminClient and Kafka's Consumer's client in order to fetch such 
metrics.
Consumer lag calculated as follows:
    
    sum(topic_offset_per_partition-consumer_offset_per_partition)


#### What is Consumer Lag and why is important?
Quoting this [article](https://sematext.com/blog/kafka-consumer-lag-offsets-monitoring/):
> What is Kafka Consumer Lag? Kafka Consumer Lag is the indicator of how much lag there is between Kafka producers and consumers....

> Why is Consumer Lag Important? Many applications today are based on being able to process (near) real-time data. Think about performance monitoring system like Sematext Monitoring or log management service like Sematext Logs. They continuously process infinite streams of near real-time data. If they were to show you metrics or logs with too much delay – if the Consumer Lag were too big – they’d be nearly useless.  This Consumer Lag tells us how far behind each Consumer (Group) is in each Partition.  **The smaller the lag the more real-time the data consumption**.

In summary, consumer lag tells us 2 things:
* The closer the lag to 0, the more confidence we are on processing messages nearer to real-time. Therefore, it _could_ indicate that our consumers are processing messages in a healthy manner.
* The further the lag from 0, the less confidence we are on processing messages nearer to real-time. Therefore, it _could_ indicate that our consumers are not processing messages in a healthy manner.

### Supported Kafka Versions
Since this client uses Kafka Admin Client and Kafka Consumer client version of *2+*, therefore this client supportes Kafka brokers from version **0.10.2+**.

## Features
* Rich metrics that show detailed consumer lags on both levels, on the **consumer group level** and on the **consumer member level** for more granularity.
* Metrics are available for both, **console and Prometheus**. 
* **Very fast** due to the *native compilation* by GraalVM Native Image.
* Highly configurable through either properties configurations or through environment variables.
* Configurable logging through log4j, currently supports JSON as well the standard logging.
* Ready to use thin Docker images either for *Jar* or *native* application for your cloud deployments such as **Kubernetes**.
* The tool is also available as **maven package** in case you want to be embedded it into your application.

## Changelog
#### 0.1.1
- Issue #29: Publish the artifacts in Maven Central instead of bintray
- Update Kafka clients to version `2.8.0`.
#### 0.1.0
**Major Release:**
- Issue #27: Refactor the client in order to minimize the usage of dependencies and remove any reflections.
- Issue #24: Support native compilation via GraalVM Native Image.
- Issue #15: Configurable log4j support for either JSON or standard logging.
- Issue #14: Support of configurations through environment variables.
- Update all the dependencies to the latest version.
#### 0.0.8: 
- Issue #23: Extend Lag stats on consumer member level.
- Issue #20: Support consumer group and topic deletion on the fly.
- Issue #21: Change default port to 9739
#### 0.0.7:
- Issue #17: Now this client will show newly joined consumer groups as well **without the need to restart the client**. You should start it once and it will always refresh the consumer groups list according to the poll interval.
- Kafka client updated to version 2.5.0.

#### 0.0.6:
- Issue #8: Support configuration file as parameter
- Kafka client updated to version 2.4.1.

 
## Installation and Usage
#### Native Application
You can downland the latest release of the Native application from [here](https://github.com/omarsmak/kafka-consumer-lag-monitoring/releases), currently it only supports **Mac** and **Linux**. An example from Prometheus component:
```
./kafka-consumer-lag-monitoring-prometheus-0.1.0 config.properties
``` 

**Note to Mac users**: You will need to verify the application, to do this, run:
```
xattr -r -d com.apple.quarantine kafka-consumer-lag-monitoring-prometheus-0.1.0
```

#### Uber JAR
You can downland the latest release of the Uber JAR from [here](https://github.com/omarsmak/kafka-consumer-lag-monitoring/releases). This client requires at least Java 8 in order to run. You can run it like this for example from Console component:  
```
java -jar kafka-consumer-lag-monitoring-console-0.1.0-all.jar -b kafka1:9092,kafka2:9092,kafka3:9092 -c "my_awesome_consumer_group_01" -p 5000
```

#### Docker
There two types of docker images:
1. Docker images based on the **native application**: This docker image is built using the natively compiled application, the benefit is, you will get **faster** and **small** image which 
is beneficial for your cloud native environment. However, since the native compilation is pretty new to this client, is still an evolving work.
2. Docker images based on the **Uber Jar**: This docker image is built using the uber Jar. Although it might be slower and larger, it is the more stable than the Docker native images but is still optimized to run in container orchestration frameworks
such as kubernetes as efficient as possible.

Example:
```
docker run omarsmak/kafka-consumer-lag-monitoring-prometheus-native -p 9739:9739  \
-e kafka_bootstrap_servers=localhost:9092 \
-e kafka_retry_backoff.ms = 200 \
-e monitoring_lag_consumer_groups="test*" \
-e monitoring_lag_prometheus_http_port=9739 \
-e monitoring_lag_logging_rootLogger_appenderRef_stdout_ref=LogToConsole \
-e monitoring_lag_logging_rootLogger_level=info
```

## Usage
### Console Component:
This mode will print the consumer lag per partition and the total lag among all partitions and continuously refreshing the metrics per the value of `--poll.interval` startup parameter. It accepts the following parameters:
 ```
./kafka-consumer-lag-monitoring-console-0.1.0 -h    
                                                                                                                                                                                                   130 ↵ omaral-safi@Omars-MBP-2
Usage: kafka-consumer-lag-monitoring-console [-hV] [-b=<kafkaBootstrapServers>]
       [-c=<kafkaConsumerGroups>] [-f=<kafkaPropertiesFile>] [-p=<pollInterval>]
Prints the kafka consumer lag to the console.
  -b, --bootstrap.servers=<kafkaBootstrapServers>
                  A list of host/port pairs to use for establishing the initial
                    connection to the Kafka cluster
  -c, --consumer.groups=<kafkaConsumerGroups>
                  A list of Kafka consumer groups or list ending with star (*)
                    to fetch all consumers with matching pattern, e.g: 'test_v*'
  -f, --properties.file=<kafkaPropertiesFile>
                  Optional. Properties file for Kafka AdminClient
                    configurations, this is the typical Kafka properties file
                    that can be used in the AdminClient. For more info, please
                    take a look at Kafka AdminClient configurations
                    documentation.
  -h, --help      Show this help message and exit.
  -p, --poll.interval=<pollInterval>
                  Interval delay in ms to that refreshes the client lag
                    metrics, default to 2000ms
  -V, --version   Print version information and exit.
```

An example output:
```
./kafka-consumer-lag-monitoring-console-0.1.0 -b kafka1:9092,kafka2:9092,kafka3:9092 -c "my_awesome_consumer_group_01" -p 5000
        Consumer group: my_awesome_consumer_group_01
        ==============================================================================
        
        Topic name: topic_example_1
        Total topic offsets: 211132248
        Total consumer offsets: 187689403
        Total lag: 23442845
        
        Topic name: topic_example_2
        Total topic offsets: 15763247
        Total consumer offsets: 15024564
        Total lag: 738683
        
        Topic name: topic_example_3
        Total topic offsets: 392
        Total consumer offsets: 392
        Total lag: 0
        
        Topic name: topic_example_4
        Total topic offsets: 24572
        Total consumer offsets: 24570
        Total lag: 2
        
        Topic name: topic_example_5
        Total topic offsets: 430
        Total consumer offsets: 430
        Total lag: 0
        
        Topic name: topic_example_6
        Total topic offsets: 6342
        Total consumer offsets: 6335    
        Total lag: 7
```
 
##### Example Usage Native Application:
```
./kafka-consumer-lag-monitoring-console-0.1.0 -c "test*" -b localhost:9092 -p 500
```

##### Example Usage Uber Jar Application:
```
java -jar kafka-consumer-lag-monitoring-console-0.1.0-all.jar -c "test*" -b localhost:9092 -p 500
```

##### Example Usage Docker Native Application:
```
docker run omarsmak/kafka-consumer-lag-monitoring-console-native -c "test*" -b localhost:9092 -p 500
```

##### Example Usage Docker Uber Jar Application:
```
docker run omarsmak/kafka-consumer-lag-monitoring-console -c "test*" -b localhost:9092 -p 500
```

### Prometheus Component:
In this mode, the tool will start an http server on a port that being set in `monitoring.lag.prometheus.http.port` config and it will expose an endpoint that is reachable via `localhost:<http.port>/metrics` or `localhost:<http.port>/prometheus` 
so prometheus server can scrap these metrics and expose them for example to grafana. You will need to pass the configuration as properties file or via environment variables. An example config file:
```
kafka.bootstrap.servers=localhost:9092
kafka.retry.backoff.ms = 200
monitoring.lag.consumer.groups=group-a-*,*-b-*,*-c
monitoring.lag.consumer.groups.exclude=*test*
monitoring.lag.prometheus.http.port=9772
monitoring.lag.logging.rootLogger.appenderRef.stdout.ref=LogToConsole
monitoring.lag.logging.rootLogger.level=info
```
This will include consumer groups that either start with `group-a-`, contain `-b-`, or end with `-c`, excluding those containing `test`.

You can then run it like the following:
##### Example Usage Native Application:
```
./kafka-consumer-lag-monitoring-prometheus-0.1.0 config.proprties
```

##### Example Usage Uber Jar Application:
```
java -jar kafka-consumer-lag-monitoring-prometheus-0.1.0-all.jar config.proprties
```

##### Example Usage Docker Native Application:
For Docker, we will use the environment variables instead:
```
docker run omarsmak/kafka-consumer-lag-monitoring-prometheus-native -p 9739:9739  \
-e kafka_bootstrap_servers=localhost:9092 \
-e kafka_retry_backoff.ms = 200 \
-e monitoring_lag_consumer_groups="test*" \
-e monitoring_lag_prometheus_http_port=9739 \
-e monitoring_lag_logging_rootLogger_appenderRef_stdout_ref=LogToConsole \
-e monitoring_lag_logging_rootLogger_level=info 
```

##### Example Usage Docker Uber Jar Application:
For Docker, we will use the environment variables instead:
```
docker run omarsmak/kafka-consumer-lag-monitoring-prometheus -p 9739:9739  \
-e kafka_bootstrap_servers=localhost:9092 \
-e kafka_retry_backoff.ms = 200 \
-e monitoring_lag_consumer_groups="test*" \
-e monitoring_lag_prometheus_http_port=9739 \
-e monitoring_lag_logging_rootLogger_appenderRef_stdout_ref=LogToConsole \
-e monitoring_lag_logging_rootLogger_level=info
```

**Note:** By default, port `9739` is exposed by the docker image, hence you **should avoid** overrding the client's HTTP port through the client's startup arguments (`--http.port`) as described below when you run the client through docker container and leave it to the default of `9739`. However you can still change the corresponding docker mapped port to anything of your choice. 

##### Exposed Metrics:
##### `kafka_consumer_group_offset{group, topic, partition}`
The latest committed offset of a consumer group in a given partition of a topic.

##### `kafka_consumer_group_partition_lag{group, topic, partition}`
The lag of a consumer group behind the head of a given partition of a topic. Calculated like this: `current_topic_offset_per_partition - current_consumer_offset_per_partition`.

##### `kafka_topic_latest_offsets{group, topic, partition}`
The latest committed offset of a topic in a given partition.

##### `kafka_consumer_group_total_lag{group, topic}`
The total lag of a consumer group behind the head of a topic. This gives the total lags from all partitions over each topic, it provides good visibility but not a precise measurement since is not partition aware.

##### `kafka_consumer_group_member_lag{group, member, topic}`
The total lag of a consumer group member behind the head of a topic. This gives the total lags over each consumer member within consumer group.

##### `kafka_consumer_group_member_partition_lag{group, member, topic, partition}`
The lag of a consumer member within consumer group behind the head of a given partition of a topic.
 

#### Configuration
Majority of the components here, for example `Prometheus` components supports two types of configurations:
1. **Application Properties File**: You can provide the application a config properties file as argument e.g: `./kafka-consumer-lag-monitoring-prometheus-0.1.0 config.properties`, this is an example config:

        ```
        kafka.bootstrap.servers=localhost:9092
        kafka.retry.backoff.ms = 200
        monitoring.lag.consumer.groups=test*
        monitoring.lag.prometheus.http.port=9772
        monitoring.lag.logging.rootLogger.appenderRef.stdout.ref=LogToConsole
        monitoring.lag.logging.rootLogger.level=info
        ```
    Note here the application accepts configs with two prefixes:
    - `kafka.`: Use the `kafka` prefix for any config related to Kafka admin client, these configs are basically the same configs that you will find here: https://kafka.apache.org/documentation/#adminclientconfigs/.
    - `monitoring.lag.` : Use the `monitoring.lag` prefix to pass any config specific to this client, you will take a look which configs that the client will accept later.

2. **Environment Variables**: You can as well pass the configs as environment variables, this is useful when running the application in environment like Docker, for example:

        ```
        docker run --rm -p 9739:9739 \
        -e monitoring_lag_logging_rootLogger_appenderRef_stdout_ref=LogToConsole \
        -e monitoring_lag_consumer_groups="test-*" \
        -e kafka_bootstrap_servers=host.docker.internal:9092  \
        omarsmak/kafka-consumer-lag-monitoring-prometheus-native:latest 
        ```    
     Similar to the application properties file, it supports `kafka` and `monitoring.lag`. However, you will need to replace all dot `.` with underscore `_` for all the configs, for example the config `kafka.bootstrap.servers` its environment equivalent is `kafka_bootstrap_servers`.
     
#### Available Configurations
- `monitoring.lag.consumer.groups` : A list of Kafka consumer groups or list ending with star (\*\) to fetch all consumers with matching pattern, e.g: `test_v*`.
- `monitoring.lag.poll.interval` : Interval delay in ms to that refreshes the client lag metrics, default to 2000ms.
- `monitoring.lag.prometheus.http.port` : Http port that is used to expose metrics in case, default to 9739.


#### Logging
The client ships with Log4j bindings and supports JSON and standard logging. The default log4j properties that it uses:
```
# Log to console
appender.console.type = Console
appender.console.name = LogToConsole
appender.console.layout.type = PatternLayout
appender.console.layout.pattern = [%-5level] %d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %c{1} - %msg%n

# Log to console as JSON
appender.json.type = Console
appender.json.name = LogInJSON
appender.json.layout.type = JsonLayout
appender.json.layout.complete = true
appender.json.layout.compact = false

rootLogger.level = info
rootLogger.appenderRef.stdout.ref = LogInJSON
```
By default, `LogInJSON` is enabled. However, you can customtize all of this by providing these configurations prefixed with `monitoring.lag.logging.`. For example, to enable the standard logging, you will need to 
add this config `monitoring.lag.logging.rootLogger.appenderRef.stdout.ref=LogToConsole` or as environment variable: `monitoring_lag_logging_rootLogger_appenderRef_stdout_ref=LogToConsole`.

**Note**: When configuring the logging through the environment variables, note that the configuration are **case sensitive**.
                                                                                

## Usage as Library 
If you want to use this client embedded into your application, you can achieve that by adding a dependency to this tool in your `pom.xml` or `gradle.build` as explained below:
#### Maven
```
<dependency>
  <groupId>com.omarsmak.kafka</groupId>
  <artifactId>consumer-lag-monitoring</artifactId>
  <version>0.1.1</version>
</dependency>
```

#### Gradle
```
compile 'com.omarsmak.kafka:consumer-lag-monitoring:0.1.1'

```

### Usage
#### Java
```
import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient;
import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClientFactory;
import org.apache.kafka.clients.admin.AdminClientConfig;

import java.util.Properties;

public class ConsumerLagClientTest {
    
    public static void main(String[] args){
        // Create a Properties object to hold the Kafka bootstrap servers
        final Properties properties = new Properties();
        properties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka1:9092");
        
        // Create the client, we will use the Java client 
        final KafkaConsumerLagClient kafkaConsumerLagClient = KafkaConsumerLagClientFactory.create(properties);
        
        // Print the lag of a Kafka consumer
        System.out.println(kafkaConsumerLagClient.getConsumerLag("awesome-consumer"));
    }
}
```

#### Kotlin
```
import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClientFactory
import org.apache.kafka.clients.admin.AdminClientConfig
import java.util.Properties

object ConsumerLagClientTest {

    @JvmStatic
    fun main(arg: Array<String>) {
        // Create a Properties object to hold the Kafka bootstrap servers
        val properties = Properties().apply {
            this[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = "kafka1:9092"
        }

        // Create the client, we will use the Kafka AdminClient Java client
        val kafkaConsumerLagClient = KafkaConsumerLagClientFactory.create(properties)

        // Print the lag of a Kafka consumer
        println(kafkaConsumerLagClient.getConsumerLag("awesome-consumer"))
    }
}
```

## Build The Project
Run `./gradlew clean build` on the top project folder which is as result, it will run all tests and build the Uber jar.


## Project Sponsors
[![Alt text](./jetbrains.svg)](https://www.jetbrains.com/?from=kafka-consumer-lag-monitoring)
