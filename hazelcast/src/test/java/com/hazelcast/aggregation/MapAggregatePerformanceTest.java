package com.hazelcast.aggregation;

import com.hazelcast.aggregation.impl.DoubleAverageAggregator;
import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelTest.class})
public class MapAggregatePerformanceTest extends HazelcastTestSupport {

    @Test
    @Ignore("needs 10G of heap to run")
    public void doubleAvg_10millionValues_1node_primitiveValue() {
        IMap<Long, Double> map = getMapWithNodeCount(1);
        //  map.addIndex("__VALUE", true);

        System.err.println("Initialising");

        int elementCount = 10000000;
        double value = 0;
        Map<Long, Double> values = new HashMap<Long, Double>(elementCount);
        for (long i = 0L; i < elementCount; i++) {
            values.put(i, value++);
        }

        System.err.println("Putting");
        long putStart = System.currentTimeMillis();
        map.putAll(values);
        long putStop = System.currentTimeMillis();
        System.err.println("Finished putting " + (putStop - putStart) + " millis");

        System.err.println("Executing bare metal");
        long start1 = System.currentTimeMillis();

        int count = 0;
        double sum = 0d;
        for (Double d : values.values()) {
            sum += d;
            count++;
        }
        Double avg1 = sum / ((double) count);
        long stop1 = System.currentTimeMillis();
        System.err.println("Finished avg in " + (stop1 - start1) + " millis avg=" + avg1);

        for (int i = 0; i < 10; i++) {
            System.gc();
        }

        for (int i = 0; i < 10; i++) {
            System.err.println("Executing aggregation");
            long start = System.currentTimeMillis();
            Double avg = map.aggregate(new DoubleAverageAggregator<Long, Double>());
            long stop = System.currentTimeMillis();
            System.err.println("\nFinished avg in " + (stop - start) + " millis avg=" + avg);
            System.err.println("------------------------------------------");
        }

    }

    @Test
    @Ignore("needs 10G of heap to run")
    public void doubleAvg_10millionValues_1node_objectValue() {
        IMap<Long, Person> map = getMapWithNodeCount(1);
        // map.addIndex("age", true);

        System.err.println("Initialising");

        int elementCount = 10000000;
        double value = 0;
        Map<Long, Person> values = new HashMap<Long, Person>(elementCount);
        for (long i = 0L; i < elementCount; i++) {
            values.put(i, new Person(value++));
        }

        System.err.println("Putting");
        long putStart = System.currentTimeMillis();
        map.putAll(values);
        long putStop = System.currentTimeMillis();
        System.err.println("Finished putting " + (putStop - putStart) + " millis");

        System.err.println("Executing bare metal");
        long start1 = System.currentTimeMillis();

        int count = 0;
        double sum = 0d;
        for (Person p : values.values()) {
            sum += p.age;
            count++;
        }
        Double avg1 = sum / ((double) count);
        long stop1 = System.currentTimeMillis();
        System.err.println("Finished avg in " + (stop1 - start1) + " millis avg=" + avg1);

        for (int i = 0; i < 10; i++) {
            System.gc();
        }


        for (int i = 0; i < 10; i++) {
            System.err.println("Executing aggregation");
            long start = System.currentTimeMillis();
            Double avg = map.aggregate(new DoubleAverageAggregator<Long, Person>("age"));
            long stop = System.currentTimeMillis();
            System.err.println("\nFinished avg in " + (stop - start) + " millis avg=" + avg);
            System.err.println("------------------------------------------");
        }

    }

    private <K, V> IMap<K, V> getMapWithNodeCount(int nodeCount) {
        if (nodeCount < 1) {
            throw new IllegalArgumentException("node count < 1");
        }

        TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(nodeCount);


        Config config = new Config();
        // config.setProperty("hazelcast.partition.count", "3");
        MapConfig mapConfig = new MapConfig();
        mapConfig.setName("aggr");
        mapConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
        mapConfig.setBackupCount(0);
        config.addMapConfig(mapConfig);

        config.setProperty("hazelcast.query.predicate.parallel.evaluation", "true");
        config.setProperty("hazelcast.logging.type", "log4j");

        HazelcastInstance instance = factory.newInstances(config)[0];
        return instance.getMap("aggr");
    }

    public static class Person implements DataSerializable {

        public double age;

        public Person() {

        }

        public Person(double age) {
            this.age = age;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeDouble(age);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            age = in.readDouble();
        }
    }


}
