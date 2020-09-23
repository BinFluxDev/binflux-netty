![Binflux-Netty](binflux-netty.png)

Binflux-Netty allows the use of different serialization libraries 
to automatically and efficiently transfer object graphs across the network by using [Netty](http://netty.io/).
The [original project](https://github.com/EsotericSoftware/kryonetty) was a fork of 
[KryoNetty](https://github.com/Koboo/kryonetty), with the goal of creating a production-ready & more modular version.

Simply explained: Send (almost) every object back and forth between client or server.

_____________________
## Documentation

### Table of content

* [EndpointBuilder](#how-the-endpointbuilder-works)
* [Server](#how-to-start-the-server)
* [Client](#how-to-connect-the-client)
* [Reconnect](#reconnecting-with-client)
* [Channel-Pool](#connection-pooling)
* [Events](#how-to-register-an-event)
* [Own Events](#creating-own-events)
* [Download](#add-as-dependecy)
* [Build From Source](#build-from-source)

_____________________
### How the EndpointBuilder works 
`EndpointBuilder` passes the configuration and options of the different components to the classes & references behind it. In this
example there are all options enabled to show. 

Handling-options:
* `logging(boolean value)` 
    * enables/disables usage of `LoggingHandler.class` (helpful for debugging)
    * default: false
* `eventExecutor(int size)` 
    * enables usage of `EventExecutorGroup.class`
    * sets threads of `EventExecutorGroup.class` 
    * default: false / 0
    
`eventExecutor` allow asynchronous processing of the handler on client & server side and its size. 
_____________________
IdleState-options:
* `idleState(int readTimeout, int writeTimeout)`
    * enables initialization of `KryoNettyIdleHandler.class`
    * default: false
    * extra: 0 = disabled
    * write-time: 15
    * read-time: 0

What does mean `ReadTimeout` and `WriteTimeout`?

If after the time (`writeTimeout` in seconds) no object has been transferred 
from the client to the server, a WriteTimeout is thrown.

If after the time (`readTimeout` in seconds) no object has been transferred 
from the server to the client, a ReadTimeout is thrown.

* `WriteTimeout`
    * default-action: `int 1` is sent after timeout.
* `ReadTimeout`
    * default-action: no further action

To become independent for both timeouts there is a `ReadTimeoutEvent` as well as a `WriteTimeoutEvent`.

(Note: only the client uses this options and events.)
_____________________
Thread-options:
* `clientWorkerSize(int workerSize)` 
    * sets the threads per core to worker-group of the client 
    * default: 2 
* `serverBossSize(int bossSize)` 
    * sets the threads per core to boss-group of the server  
    * default: 1
* `serverWorkerSize(int workerSize)` 
    * sets the threads per core to worker-group of the server  
    * default: 5 
    
Here the threads are set per processor core. If you have no idea what you are 
doing with this, you should leave it set by default.
_____________________
Serializer-options:
* `serializer(ISerializer serializer)` 
    * sets the specific `ISerializer`
    * default: `KryoSerializer` (class-independent)

_____________________
### How to start the server

To start the `EndpointServer`, the following call `start()` is all you need. 
The `EndpointServer` needs a configured `EndpointBuilder` instance & an `int port` as argument.

```java
    EndpointServer server = new EndpointServer(builder, 54321);
    server.start();
```

_____________________
### How to connect the client

The `EndpointClient` configuration works quite similar to the `EndpointServer`. 
The only difference is in the constructor, 
where the client needs `String host` & `int port` besides the `KryoNetty` instance.

```java
    EndpointClient client = new EndpointClient(kryoNetty, "localhost", 54321);
    client.start();
```

_____________________
### Reconnecting with `Client`

If you wanted to reconnect the `Client`, you can do it by that way:

```java
    EndpointClient client = new EndpointClient(builder, "localhost", 56566);
    client.start(); 
    // Client is now connected, so close Channel with:
    client.close();
    // Change connect-address with:
    client.setAddress(String host, int port);
    // Client is now disconnected, so reconnect with:
    client.start();
```

_____________________
### Connection-Pooling

The `PooledClient` is a kind of mixture of multiple `EndpointClient` instances. 
You can specify a pool size, which determines the number of connections of the client. 
Conclusion: poolSize = connections from client-to-server

```java
    int poolSize = 10; // 10 client-to-server connections
    PooledClient client = new PooledClient(builder, "localhost", 54321, poolSize);
```

The client does not have to be connected explicitly, 
because the pool automatically creates new connections if the current pool size does 
not correspond to the maximum pool size. This also means that a reconnect is handled automatically. 
However, there is the possibility to connect the maximum pool size in advance.

```java
    int poolSize = 10; // 10 client-to-server connections
    PooledClient client = new PooledClient(builder, "localhost", 54321, poolSize);
    client.start();
```

This way the pooled connections are already connected to the server if you call e.g. `send(Object object)`.

The events are combined on the `PooledServer` as well as on the `PooledClient`. 
If an event is registered, it will be thrown from all pooled channels, no matter if client or server.

The `PooledServer` works differently from the `PooledClient`. 
It opens multiple server sockets on one address:port `(e.g. 192.168.0.2:6666)` using the ChannelOptions `SO_REUSEPORT` & `SO_REUSEADDRESS`. 
This has the consequence that the `PooledServer` works only under some Linux distros. 
(Attention: Tested Debian 8, 9 & 10. Please check the "Epoll" support of your operating system) 
Behind this address:port the desired number of server sockets listen for new connections.

```java
    int poolSize = 10; // 10 server-sockets listening
    PooledServer server = new PooledServer(builder, 54321, poolSize);
    server.start();
```

If errors occur in one of the two pooling implementations, 
please report them in [Issues](https://github.com/BinfluxDev/binflux-netty/issues) as soon as possible so that they can be fixed.

_____________________
### How to register an Event

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

_____________________
### Creating own Events

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

After writing the class `SampleEvent`, the event can now be passed to the `EventHandler`.
The variable `endpoint` could be a `Server` or a `Client`

```java
endpoint.eventHandler().handleEvent(new SampleEvent("SampleString", 100, System.currentTimeMillis()));
```

Now all consumers of the `SampleEvent` are called and processed by the registered consumers.

Example Consumer of `SampleEvent`:
```java
endpoint.eventHandler().registerConsumer(SampleEvent.class, (event) -> System.out.println(event.toString()));
```

_____________________
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

_____________________
### Build from source

If you want to build `binflux-netty-{version}.jar` from source, clone this repository and run `./gradlew build`. 
The output-file will be in the directory: `/build/libs/binflux-netty-{version}.jar`
Gradle downloads the required dependencies and inserts all components into the output-file.
If you are interested in the build task, look at [build.gradle](https://github.com/BinfluxDev/binflux-netty/blob/master/build.gradle).

_____________________
