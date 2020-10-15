![Binflux-Netty](binflux-netty.png)

Binflux-Netty allows the use of different serialization libraries 
to automatically and efficiently transfer object graphs across the network by using [Netty](http://netty.io/).
The [original project](https://github.com/EsotericSoftware/kryonetty) was a fork of 
[KryoNetty](https://github.com/Koboo/kryonetty), with the goal of creating a production-ready & more modular version.

Simply explained: Send (almost) every object back and forth between client or server.


## Overview

This overview offers a simple step by step guide to get started with binflux-netty.

#### Endpoint
  * [EndpointBuilder](#what-is-the-endpointbuilder)
    * [Basic](#basic-options)
    * [IdleState](#idlestate-options)
    * [Netty](#netty-options)
    * [Serialization](#serializer-options)
  * [Endpoints](#building-the-endpoints)
    * [Build Endpoints](#building-the-endpoints)
    * [Server](#how-to-start-the-server)
    * [Client](#how-to-start-the-client)
    * [Reconnect](#reconnecting-with-endpointclient)
    * [Channel-Pool](#connection-pooling)

#### Events
  * [Default Events](#default-events)
  * [Register Events](#register-events)
  * [Create Events](#creating-events)
  * [Handle Events](#throwing-and-consuming-events)

#### Build and Download
  * [Download](#add-as-dependency)  
  * [Build From Source](#build-from-source)


## What is the EndpointBuilder
`EndpointBuilder` passes options to endpoints. Create new Builder-instance:

```java
EndpointBuilder builder = EndpointBuilder.newBuilder();
```

#### Basic options:
* `logging(boolean value)` 
    * enables/disables netty-built-in `LoggingHandler.class` (helpful for debugging)
    * default: disabled
* `packetProtocol(boolean value)` 
    * enables/disables built-in packet-protocol
    * default: disabled
* `eventExecutor(int threadPoolSize)` 
    * enables `EventExecutorGroup.class`
    * thread-pooledSerializer-size * cores
    * default: disabled
    
`eventExecutor` allow asynchronous processing of the handler on client & server side and its size. 

#### IdleState options:
* `idleState(int readTimeout, int writeTimeout)`
    * enables initialization of `NettyIdleHandler.class`
    * default: disabled 
    * default-write-time: 15
    * default-read-time: 0 (= disabled)

What does mean `ReadTimeout` and `WriteTimeout`?

If after the time (`writeTimeout` in seconds) no object has been transferred 
from the client to the server, a WriteTimeout is thrown.

If after the time (`readTimeout` in seconds) no object has been transferred 
from the server to the client, a ReadTimeout is thrown.

* `WriteTimeout`
    * default-action: `int 1` is sent after timeout.
* `ReadTimeout`
    * default-action: no action

(Note: only the client throws this events.)

#### Netty options:
* `clientWorkerSize(int workerSize)` 
    * sets the threads per core to worker-group of the client 
    * default: 2 
* `serverBossSize(int bossSize)` 
    * sets the threads per core to boss-group of the server  
    * default: 1
* `serverWorkerSize(int workerSize)` 
    * sets the threads per core to worker-group of the server  
    * default: 5 
    
If you have no idea what you are doing with this, you should leave it set by default.

#### Serializer options:
* `serializer(SerializerPool pool)` 
    * sets the specific `SerializerPool` with `Serialization`
    * default: `KryoSerialization`

For better performance the serializations are pooled with a `SerializerPool`. 

Example usage:
```java
endpointBuilder.serializer(new SerializerPool(KryoSerialization.class));
```

See more about serialization: [binflux-serilization](https://github.com/BinfluxDev/binflux-serilization)

## Building the endpoints:

To build the client:
* `build(String host, int port)`
    * Endpoint: `EndpointClient` (`extends AbstractClient`)
* `build(String host, int port, int poolSize)`
    * Endpoint: `PooledClient` (`extends AbstractClient`)
    
To build the server:
* `build(int port)`
    * Endpoint: `EndpointServer` (`extends AbstractServer`)
* `build(int port, int poolSize)`
    * Endpoint: `PooledServer` (`extends AbstractServer`)

## How to start the server

To start the `EndpointServer` call`start()`. 

```java
EndpointServer server = builder.build(54321);
server.start();
```

## How to start the client

To start the `EndpointClient` call `start()`.

```java
EndpointClient client = builder.build("localhost", 54321);
client.start();
```


## Reconnecting with EndpointClient

If you want to reconnect a `EndpointClient`, do this:

* Connect Endpoint
```java
EndpointClient client = builder.build("localhost", 54321);
client.start(); 
```
* Close it - not `stop()`
```java
client.close();
```

* (optional) Change address
```java
client.setAddress("localhost", 12345);
```
* Start again
```java
client.start();
```

## Connection-Pooling

The `PooledClient` opens N (`= poolSize`) channels to the server.

```java
int poolSize = 10; // 10 client-to-server connections
PooledClient client = builder.build("localhost", 54321, poolSize);
```

By using the `start()`-call, the `PooledClient` fills the channel pool with 
the maximum number of channels. As soon as a channel is closed and needed again (many `send`-calls) 
the client restarts the channel automatically.

```java
int poolSize = 10; // 10 client-to-server connections
PooledClient client = builder.build("localhost", 54321, poolSize);
client.start();
```

The events are combined on the `PooledServer` & `PooledClient`. 
If an event is registered, it will be thrown from all pooled channels.

The `PooledServer` works differently from the `PooledClient`. 
It opens multiple server sockets on one address:port `(e.g. 192.168.0.2:6666)` using the ChannelOptions `SO_REUSEPORT` & `SO_REUSEADDRESS`. 
This has the consequence that the `PooledServer` works only under OSes with `Epoll`-transport. 
(Attention: Tested Debian 8, 9 & 10. Please check the "Epoll" support of your operating system) 
Behind this address:port the desired number of server sockets listen for new connections.

```java
int poolSize = 10; // 10 server-sockets listening
PooledServer server = new PooledServer(builder, 54321, poolSize);
server.start();
```

## Default Events

The event system is completely `Consumer<T>` based. There are some default events:

* `ConnectEvent`
    * server/client: channel connects 
* `DisconnectEvent`
    * server/client: channel disconnects
* `ReceiveEvent`
    * server/client: receives object from client/server
* `ErrorEvent`
    * server/client: exception occured
* `ReadTimeoutEvent` 
    * client: read-timeout
* `WriteTimeoutEvent`
    * client: write-timeout 
    
* `EndpointStartEvent`
    * server/client: started
* `EndpointStopEvent`
    * server/client: stopped
* `EndpointClosedEvent`
    * server/client: closed


## Register Events

Register an `ConsumerEvent` by using a `Consumer<ConnectEvent>`:

```java
public class ConnectionConsumer implements Consumer<ConnectEvent> {
    @Override
    public void onEvent(ConnectEvent connectEvent) {
        ChannelHandlerContext ctx = event.getCtx();
        System.out.println("Server: Client connected: " + ctx.channel().remoteAddress());
    }
}
```

Register an event to an endpoint:

```java
server.eventHandler().registerConsumer(ConnectEvent.class, new ConnectionConsumer());
```

Syntax of `registerConsumer`:
* `registerConsumer(Class<? implements ConsumerEvent> class, Consumer<ConsumerEvent> consumer)`

Pass consumer directly into method:

```java
server.eventHandler().registerConsumer(ConnectEvent.class, (event) -> {
   ChannelHandlerContext ctx = event.getCtx();
   System.out.println("Server: Client connected: " + ctx.channel().remoteAddress());
});
```

Example-consumer of `ReceiveEvent`:

```java
public class ReceiveConsumer implements Consumer<ReceiveEvent> {
    @Override
    public void onEvent(ReceiveEvent event) {
        ChannelHandlerContext ctx = event.getCtx();
        Object object = event.getObject();
        System.out.println("Server: Client received: " + ctx.channel().remoteAddress() + "/" + object);
        if(object instanceof Boolean) {
            Boolean result = (Boolean) object;
            System.out.println("Result is: " + result);
        }
    }
}
```


## Creating Events

If you want to create your own event and let the clients or servers handle it, an event could look like this:

```java
public class SampleEvent implements ConsumerEvent {

    private String string;
    private Integer value;
    private Long timeStamp;

    public SampleEvent(String string, Integer value, Long timeStamp) {
        this.string = string;
        this.value = value;
        this.timeStamp = timeStamp;
    } 
    
    public String getString() {
        return this.string;
    }

    public Integer getValue() {
        return this.value;
    }

    public Long getTimeStamp() {
        return this.timeStamp;
    }   

    @Override
    public String toString() {
        return "SampleEvent(string=" + this.string + "; " +
         "value=" + this.value + "; " +
          "timeStamp=" + this.timeStamp + ")";
    }
}
```


## Throwing and Consuming Events

To call the consumers of the event, you can pass the event to the `EventHandler` in an `Endpoint`.

Throw events:
```java
endpoint.eventHandler().handleEvent(new SampleEvent("SampleString", 100, System.currentTimeMillis()));
```

Consume/Register events:
```java
endpoint.eventHandler().registerConsumer(SampleEvent.class, (event) -> System.out.println(event.toString()));
```

## Add as dependency

Add `jitpack.io` as repository. 

```java
repositories {
    maven { url 'https://jitpack.io' }
}
```

And add it as dependency. (e.g. `1.0` is the release-version)
```java
dependencies {
    implementation 'com.github.BinfluxDev:binflux-netty:1.0'
}
```


## Build from source

* Clone repository
* Run `./gradlew buildNetty`
* Output `/build/libs/binflux-netty-{version}-all.jar`
* Build task [build.gradle](https://github.com/BinfluxDev/binflux-netty/blob/master/build.gradle)