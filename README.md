![Binflux-Netty](binflux-netty.png)

Binflux-Netty allows the use of different serialization libraries 
to automatically and efficiently transfer object graphs across the network by using [Netty](http://netty.io/).
The [original project](https://github.com/EsotericSoftware/kryonetty) was a fork of 
[KryoNetty](https://github.com/Koboo/kryonetty), with the goal of creating a production-ready & more modular version.

Simply explained: Send (almost) every object back and forth between client or server.


## Overview

* [EndpointBuilder](#what-is-the-endpointbuilder)
* [Serialization](#serialization)
* [Server](#how-to-start-the-server)
* [Client](#how-to-start-the-client)
* [Reconnect](#reconnecting-with-client)
* [Channel-Pool](#connection-pooling)
* [Default Events](#default-events)
* [Register Events](#register-events)
* [Create Events](#creating-events)
* [Handle Events](#throwing-and-consuming-events)
* [Download](#add-as-dependecy)
* [Build From Source](#build-from-source)


## What is the EndpointBuilder
`EndpointBuilder` passes the options to the endpoints. 

#### Basic options:
* `logging(boolean value)` 
    * enables/disables usage of `LoggingHandler.class` (helpful for debugging)
    * default: false
* `eventExecutor(int size)` 
    * enables usage of `EventExecutorGroup.class`
    * sets threads of `EventExecutorGroup.class` 
    * default: false / 0
    
`eventExecutor` allow asynchronous processing of the handler on client & server side and its size. 

#### IdleState options:
* `idleState(int readTimeout, int writeTimeout)`
    * enables initialization of `KryoNettyIdleHandler.class`
    * default: false
    * extra: 0 = disabled
    * default-write-time: 15
    * default-read-time: 0

What does mean `ReadTimeout` and `WriteTimeout`?

If after the time (`writeTimeout` in seconds) no object has been transferred 
from the client to the server, a WriteTimeout is thrown.

If after the time (`readTimeout` in seconds) no object has been transferred 
from the server to the client, a ReadTimeout is thrown.

* `WriteTimeout`
    * default-action: `int 1` is sent after timeout.
* `ReadTimeout`
    * default-action: no further action

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
* `serializer(ISerializer serializer)` 
    * sets the specific `ISerializer`
    * default: `KryoSerializer` (class-independent)

## Building the endpoints:

Client:
* `build(String host, int port)`
    * Endpoint: `EndpointClient`
* `build(String host, int port, int poolSize)`
    * Endpoint: `PooledClient`
    
Server:
* `build(int port)`
    * Endpoint: `EndpointServer`
* `build(int port, int poolSize)`
    * Endpoint: `PooledServer`


## Serialization

Currently there are 3 serializers:

* `KryoSerializer` 
    * Link: [Kryo](https://github.com/EsotericSoftware/kryo)
    * Requirement: None
    * Optional: `KryoSerializer(int inputSize, int outputSize, int maxOutputSize)`
* `JavaSerializer`
    * Link: [Java](https://docs.oracle.com/javase/tutorial/jndi/objects/serial.html#:~:text=To%20serialize%20an%20object%20means,interface%20or%20its%20subinterface%2C%20java.)
    * Requirement: Must implement `Serializable`
* `FSTSerializer`
    * Link: [fast-serialization](https://github.com/RuedigerMoeller/fast-serialization)
    * Requirement: Must implement `Serializable`
* `HessianSerializer`
    * Link: [Hessian](http://hessian.caucho.com/)
    * Requirement: None
* `QuickserSerializer`
    * Link: [QuickSer](https://github.com/romix/quickser)
    * Requirement: None
    


## How to start the server

To start the `EndpointServer` call`start()`. 

```java
    EndpointServer server = builder.build(54321);
    server.start();
```


## How to start the client

The `EndpointClient` works quite similar to the `EndpointServer`, just call `start()`.

```java
    EndpointClient client = builder.build("localhost", 54321);
    client.start();
```


## Reconnecting with `Client`

If you wanted to reconnect a `Client`, you can do it by that way:

```java
    EndpointClient client = builder.build("localhost", 56566);
    client.start(); 
    // Client is now connected, so close Channel with:
    client.close();
    String newHost = "localhost";
    int newPort = 54322;
    // Change connect-address with:
    client.setAddress(newHost, newPort);
    // Client is now disconnected, so reconnect with:
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
    * server: new client connects 
    * client: client connects to server
* `DisconnectEvent`
    * server: client disconnects (server-side)
    * client: client disconnects (client-side)
* `ReceiveEvent`
    * server: receives new object from client
    * client: receives new object from server 
* `ErrorEvent`
    * server: somewhere an error was thrown
    * client: somewhere an error was thrown
* `ReadTimeoutEvent` (only client-side)
    * client: somewhere an error was thrown
* `WriteTimeoutEvent` (only client-side)
    * client: somewhere an error was thrown


## Register Events

To register an `ConsumerEvent` by using a `Consumer<? extends ConsumerEvent>`:

```java
    public class ConnectionConsumer implements Consumer<ConnectEvent> {
        @Override
        public void onEvent(ConnectEvent connectEvent) {
            ChannelHandlerContext ctx = event.getCtx();
            System.out.println("Server: Client connected: " + ctx.channel().remoteAddress());
        }
    }

    // To register an event to an endpoint
    server.eventHandler().registerConsumer(ConnectEvent.class, new ConnectionConsumer());
```

The register method expects this arguments:
* `registerConsumer(Class<? implements ConsumerEvent> class, Consumer<ConsumerEvent> consumer)`

You can also pass the consumer directly into the method.

```java
    server.eventHandler().registerConsumer(ConnectEvent.class, (event) -> {
       ChannelHandlerContext ctx = event.getCtx();
       System.out.println("Server: Client connected: " + ctx.channel().remoteAddress());
    });
```

Here an example to process an object which is fired via a `ReceiveEvent`.

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

To call the consumers of the event, you can pass the event to the `EventHandler` in an `AbstractEndpoint`.

Throw the event:
```java
endpoint.eventHandler().handleEvent(new SampleEvent("SampleString", 100, System.currentTimeMillis()));
```

Consume the event:
```java
endpoint.eventHandler().registerConsumer(SampleEvent.class, (event) -> System.out.println(event.toString()));
```


### Add as dependecy

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
        // or use
        compile group: 'com.github.BinfluxDev', name: 'binflux-netty', version: '1.0'
    }
```


### Build from source

If you want to build `binflux-netty-{version}.jar` from source, clone this repository and run `./gradlew build`. 
The output-file will be in the directory: `/build/libs/binflux-netty-{version}.jar`
Gradle downloads the required dependencies and inserts all components into the output-file.
If you are interested in the build task, look at [build.gradle](https://github.com/BinfluxDev/binflux-netty/blob/master/build.gradle).

