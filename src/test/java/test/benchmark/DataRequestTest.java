
package test.benchmark;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import eu.binflux.netty.endpoint.client.AbstractClient;
import eu.binflux.netty.endpoint.server.AbstractServer;
import eu.binflux.netty.eventhandler.consumer.ReceiveEvent;
import eu.binflux.serial.core.SerializerPool;
import eu.binflux.serial.fst.FSTSerialization;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import test.StaticTest;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class DataRequestTest extends AbstractBenchmark {

    public static AbstractServer server;
    public static AbstractClient client;

    public static AtomicInteger counter;
    public static AtomicInteger average;

    @BeforeClass
    public static void setupClass() {
        System.out.println("== Test KotlinRequest/Sec Behaviour == ");

        StaticTest.BUILDER
                .serializer(new SerializerPool(FSTSerialization.class));

        server = StaticTest.BUILDER.build(54321);

        server.eventHandler().registerConsumer(ReceiveEvent.class, event -> {
            if(event.getObject() instanceof DataRequest) {
                DataRequest request = (DataRequest) event.getObject();
                assertEquals(request.getString(), StaticTest.testString);
                assertEquals(request.gettLong(), StaticTest.testLong);
                assertEquals(request.gettInt(), StaticTest.testInt);
                counter.getAndIncrement();
            }
        });

        client = StaticTest.BUILDER
                .build("localhost", 54321);

        average = new AtomicInteger();
        counter = new AtomicInteger();

        assertTrue(server.start());
        assertTrue(client.start());

    }

    @AfterClass
    public static void afterClass() {
        assertTrue(client.stop());
        assertTrue(server.stop());
        System.out.println(average.get() + " packets/sec in average");
        System.out.println("== Finished KotlinRequest/Sec Behaviour == ");
    }

    @Test
    public void testStringPerSec() throws Exception {
        Thread.sleep(250);
        final long start = System.nanoTime();
        int amount = 5_000;
        for (int i = 0; i < amount; i++) {
            client.send(StaticTest.DATA_REQUEST);
        }
        final long end = System.nanoTime();
        final long time = (end - start);
        int packetsPerSec = StaticTest.getPacketsPerSec(amount, time);
        StaticTest.adjustAverage(average, packetsPerSec);
        Thread.sleep(5_000);
        assertEquals(amount, counter.get());
        System.out.println(amount + "/" + counter.get() + " successful in " + (time * (1 / 1000000000f)) + " seconds");
        System.out.println(packetsPerSec + " packets/sec");
        counter.set(0);
    }

}
