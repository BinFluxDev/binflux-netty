
package test.benchmark;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import eu.binflux.netty.endpoint.client.AbstractClient;
import eu.binflux.netty.endpoint.server.AbstractServer;
import eu.binflux.netty.eventhandler.consumer.ReceiveEvent;
import eu.binflux.netty.serialization.PooledSerializer;
import eu.binflux.netty.serialization.serializer.FSTSerializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import test.StaticTest;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KotlinRequestTest extends AbstractBenchmark {

    public static AbstractServer server;
    public static AbstractClient client;

    public static AtomicInteger counter;
    public static AtomicInteger average;

    @BeforeClass
    public static void setupClass() {
        System.out.println("== Test KotlinRequest/Sec Behaviour == ");

        StaticTest.BUILDER.serializer(new PooledSerializer(FSTSerializer.class));

        server = StaticTest.BUILDER.build(54321);

        server.eventHandler().registerConsumer(ReceiveEvent.class, event -> {
            if(event.getObject() instanceof KotlinRequest) {
                KotlinRequest request = (KotlinRequest) event.getObject();
                assertEquals(request.getTestString(), StaticTest.testString);
                assertEquals(request.getTestLong(), StaticTest.testLong);
                assertEquals(request.getTestInt(), StaticTest.testInt);
                counter.getAndIncrement();
            }
        });

        client = StaticTest.BUILDER.build("localhost", 54321, 5);

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
            client.send(StaticTest.KOTLIN_REQUEST);
        }
        final long end = System.nanoTime();
        final long time = (end - start);
        int packetsPerSec = StaticTest.getPacketsPerSec(amount, time);
        StaticTest.adjustAverage(average, packetsPerSec);
        Thread.sleep(5000);
        assertEquals(amount, counter.get());
        System.out.println(amount + "/" + counter.get() + " successful in " + (time * (1 / 1000000000f)) + " seconds");
        System.out.println(packetsPerSec + " packets/sec");
        counter.set(0);
    }

}