Kafka Consumer Lag Monitoring
====
[![Build Status](https://travis-ci.com/omarsmak/kafka-consumer-lag-monitoring.svg?token=ACVRSYGMw5EM3tmwPiBz&branch=master)](https://travis-ci.com/omarsmak/kafka-consumer-lag-monitoring)

A client tool that exports the consumer lag of a Kafka consumer group to different output implementations such as Prometheus or your terminal. It utlizes Kafka's AdminClient and Kafka's Consumer's client in order to fetch such 
metrics.
Consumer lag calculated as follows:
    
    sum(topic_offset_per_partition-consumer_offset_per_partition)

 
## Installation
This client requires at least Java 8 in order to run, soon it will be available as a Docker image.

## Usage
    java -jar kafka-consumer-lag-monitoring.jar -h                                                                                                                                               ✓  11097  15:25:30
        Usage: <main class> [-hV] -b=<kafkaBootstrapServers> -c=<kafkaConsumerClients>
                            [-i=<pollInterval>] [-m=<clientMode>] [-p=<httpPort>]
          -b, --bootstrap.servers=<kafkaBootstrapServers>
                                    A list of host/port pairs to use for establishing the
                                      initial connection to the Kafka cluster
          -c, --consumer.groups=<kafkaConsumerClients>
                                    A list of Kafka consumer groups or list ending with
                                      (*) to fetch all consumers with matching pattern, e.g: 'test_v*'
          -h, --help                Show this help message and exit.
          -i, --poll.interval=<pollInterval>
                                    Interval delay in ms to that refreshes the client lag
                                      metrics, default to 2000ms
          -m, --mode=<clientMode>   Mode to run client, possible values 'console' or
                                      'prometheus, default to 'console'
          -p, --http.port=<httpPort> Http port that is used to expose metrics in case
                                      prometheus mode is selected, default to 9000
          -V, --version             Print version information and exit.

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
so prometheus sever can scrap these metrics and expose them for example to grafana. It will expose the following metrics:
##### `kafka_consumer_group_offset{group, topic, partition}`
The latest committed offset of a consumer group in a given partition of a topic.

##### `kafka_consumer_group_partition_lag{group, topic, partition}`
The lag of a consumer group behind the head of a given partition of a topic. Calculated like this: `current_topic_offset_per_partition - current_consumer_offset_per_partition`.

##### `kafka_topic_latest_offsets{group, topic, partition}`
The latest committed offset of a topic in a given partition.

##### `kafka_consumer_group_total_lag{group, topic}`
The total lag of a consumer group behind the head of a topic. This gives the total lags over each partition, it provides good visibility but not a precise measurement since is not partition aware.

## Usage as Library 