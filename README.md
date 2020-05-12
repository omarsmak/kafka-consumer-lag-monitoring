Kafka Consumer Lag Monitoring
====
[![Build Status](https://travis-ci.com/omarsmak/kafka-consumer-lag-monitoring.svg?token=ACVRSYGMw5EM3tmwPiBz&branch=master)](https://travis-ci.com/omarsmak/kafka-consumer-lag-monitoring)
[ ![Download](https://api.bintray.com/packages/omarsmak/kafka/consumer-lag-monitoring/images/download.svg) ](https://bintray.com/omarsmak/kafka/consumer-lag-monitoring/_latestVersion)


A client tool that exports the consumer lag of a Kafka consumer group to different output implementations such as Prometheus or your terminal. It utlizes Kafka's AdminClient and Kafka's Consumer's client in order to fetch such 
metrics.
Consumer lag calculated as follows:
    
    sum(topic_offset_per_partition-consumer_offset_per_partition)


#### What is Consumer Lag and why is important?
Quoting this [article](https://sematext.com/blog/kafka-consumer-lag-offsets-monitoring/):
> What is Kafka Consumer Lag? Kafka Consumer Lag is the indicator of how much lag there is between Kafka producers and consumers....

> Why is Consumer Lag Important? Many applications today are based on being able to process (near) real-time data. Think about performance monitoring system like Sematext Monitoring or log management service like Sematext Logs. They continuously process infinite streams of near real-time data. If they were to show you metrics or logs with too much delay – if the Consumer Lag were too big – they’d be nearly useless.  This Consumer Lag tells us how far behind each Consumer (Group) is in each Partition.  **The smaller the lag the more real-time the data consumption**.

In summary, consumer lag tells us 2 things:
* The closer the lag to 0, the more confidnce we are on processing messages nearer to real-time. Therefore, it _could_ indicate that our consumers are processing messages in a healthy manner.
* The further the lag from 0, the less confidnce we are on processing messages nearer to real-time. Therefore, it _could_ indicate that our consumers are not processing messages in a healthy manner.

### Supported Kafka Versions
Since this client uses Kafka Admin Client and Kafka Consumer client version of *2+*, therefore this client supportes Kafka brokders from version **0.10.2+**.
 
## Installation and Usage
#### Uber JAR
You can downland the latest release of the Uber JAR from [here](https://github.com/omarsmak/kafka-consumer-lag-monitoring/releases). This client requires at least Java 8 in order to run. You can run it like this for example: 
```
java -jar kafka-consumer-lag-monitoring.jar -b kafka1:9092,kafka2:9092,kafka3:9092 -c "my_awesome_consumer_group_01" -m "console" -i 5000
```

#### Docker
This client is available as well in [docker hub](https://hub.docker.com/r/omarsmak/kafka-consumer-lag-monitoring), the docker image is built on top of Java 11 JRE image and optimized to run in container orchestration frameworks
 such as kubernetes as efficient as possible. Assuming you want to run it locally and you have docker daemon installed, you can run it like this for example:
 ```
 docker run -p 9000:9000 --rm omarsmak/kafka-consumer-lag-monitoring:latest -b kafka1:9092,kafka2:9092,kafka3:9092 -c "my_awesome_consumer_group_01" -m "prometheus" -i 5000 -p 9000
 ```
 **Note:** By default, port `9000` is exposed by the docker image, hence you **should avoid** overrding the client's HTTP port through the client's startup arguments (`--http.port`) as described below when you run the client through docker container and leave it to the default of `9000`. However you can still change the corresponding docker mapped port to anything of your choice. 

#### Kubernetes
Currently usage of environment variables are not directly supported. To support container orchestration, an entrypoint script is used. Provide required arguments as "args" in kubernetes deployments.

You can use placeholders in the arg command and fill these settings by environment variables, secrets or configmaps.

```
args: ["-b", "$(BOOTSTRAP_SERVERS)","-m", "$(MODE)","-c", "$(CONSUMER_GROUPS)","-i", "$(POLL_INTERVAL)", "-p", "$(HTTP_PORT)"]
```

## Changelog
#### 0.0.7:
- Issue #17: Now this client will show newly joined consumer groups as well **without the need to restart the client**. You should start it once and it will always refresh the consumer groups list according to the poll interval.
- Kafka client updated to version 2.5.0.

#### 0.0.6:
- Issue #8: Support configuration file as parameter
- Kafka client updated to version 2.4.1.


## Usage
    java -jar kafka-consumer-lag-monitoring.jar -h                                                                                                                                              
        Usage: <main class> [-hV] [-b=<kafkaBootstrapServers>]
                            -c=<kafkaConsumerClients> [-f=<kafkaPropertiesFile>]
                            [-i=<pollInterval>] [-m=<clientMode>] [-p=<httpPort>]
          -b, --bootstrap.servers=<kafkaBootstrapServers>
                                    A list of host/port pairs to use for establishing
                                      the initial connection to the Kafka cluster
          -c, --consumer.groups=<kafkaConsumerClients>
                                    A list of Kafka consumer groups or list ending with
                                      star (*) to fetch all consumers with matching
                                      pattern, e.g: 'test_v*'
          -f, --kafka.properties.file=<kafkaPropertiesFile>
                                    Optional. Properties file for Kafka AdminClient
                                      configurations, this is the typical Kafka
                                      properties file that can be used in the
                                      AdminClient. For more info, please take a look at
                                      Kafka AdminClient configurations documentation.
          -h, --help                Show this help message and exit.
          -i, --poll.interval=<pollInterval>
                                    Interval delay in ms to that refreshes the client
                                      lag metrics, default to 2000ms
          -m, --mode=<clientMode>   Mode to run client, possible values 'console' or
                                      'prometheus', default to 'console'
          -p, -http.port=<httpPort> Http port that is used to expose metrics in case
                                      prometheus mode is selected, default to 9000
          -V, --version             Print version information and exit.
          
#### New in version 0.0.6: 
Now the client has the ability to accept a properties file with the admin client/consumer configuration that you typically use with Kafka Clients. This option can be accessible through the flag `-f`. example:

    java -jar kafka-consumer-lag-monitoring.jar -f my/path/file.properties -c "my_awesome_consumer_group_01" -m "prometheus" -i 5000      
    
To learn more about the configuration that you can use here, please refer the following documentations:

Kafka AdminClient configs: https://kafka.apache.org/documentation/#adminclientconfigs  

Kafka Consumer configs: https://kafka.apache.org/documentation/#consumerconfigs

### Console Mode
This mode will print the consumer lag per partition and the total lag among all partitions and continuously refreshing the metrics per the value of `--poll.interval` startup parameter. Example output:  

    java -jar kafka-consumer-lag-monitoring.jar -b kafka1:9092,kafka2:9092,kafka3:9092 -c "my_awesome_consumer_group_01" -m "console" -i 5000
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
        
       
### Prometheus Mode       
In this mode, the tool will start an http server on a port that being set in `--http.port` startup parameter and it will expose an endpoint that is reachable via `localhost:<http.port>/metrics` or `localhost:<http.port>/prometheus` 
so prometheus server can scrap these metrics and expose them for example to grafana. 

    java -jar kafka-consumer-lag-monitoring.jar -b kafka1:9092,kafka2:9092,kafka3:9092 -c "my_awesome_consumer_group_01" -m "prometheus" -i 5000

It will expose the following metrics:
##### `kafka_consumer_group_offset{group, topic, partition}`
The latest committed offset of a consumer group in a given partition of a topic.

##### `kafka_consumer_group_partition_lag{group, topic, partition}`
The lag of a consumer group behind the head of a given partition of a topic. Calculated like this: `current_topic_offset_per_partition - current_consumer_offset_per_partition`.

##### `kafka_topic_latest_offsets{group, topic, partition}`
The latest committed offset of a topic in a given partition.

##### `kafka_consumer_group_total_lag{group, topic}`
The total lag of a consumer group behind the head of a topic. This gives the total lags from all partitions over each topic, it provides good visibility but not a precise measurement since is not partition aware.

## Usage as Library 
If you want to use this client embedded into your application, you can achieve that by adding a [dependency](https://bintray.com/omarsmak/kafka/consumer-lag-monitoring) to this tool in your `pom.xml` or `gradle.build` as explained below:
#### Maven
In your pom file add `jcenter` artifactory under `<repositories>..</repositories>`:
```
<repositories>
    <repository>
      <id>jcenter</id>
      <url>https://jcenter.bintray.com/</url>
    </repository>
</repositories>
```
and under `<dependencies>..</dependencies>`:
```
<dependency>
  <groupId>com.omarsmak.kafka</groupId>
  <artifactId>consumer-lag-monitoring</artifactId>
  <version>0.0.7</version>
</dependency>
```

#### Gradle
In your `build.gradle`, under repositories add the following:
```
repositories {
    jcenter()
}
```
and under `dependencies` the following: 
```
compile 'com.omarsmak.kafka:consumer-lag-monitoring:0.0.7'

```
**Note:** Since [bintray jcenter](https://bintray.com/bintray/jcenter) is shadowing all maven central packages, you don't need to include both.

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
