/*
 * Copyright [2016] [vincentruan]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springhub.boot.curator.counter;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.RetryNTimes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author vincentruan
 * @version 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DistributedAtomicLongExampleTest {

    private static final int QTY = 5;
    private static final String PATH = "/examples/counter";

    @Autowired
    private CuratorFramework client;

    @Test
    public void testDistributedAtomicLong() throws Exception {
        List<DistributedAtomicLong> examples = Lists.newArrayList();
        ExecutorService service = Executors.newFixedThreadPool(QTY);
        for (int i = 0; i < QTY; ++i) {
            final DistributedAtomicLong count = new DistributedAtomicLong(client, PATH, new RetryNTimes(10, 10));

            examples.add(count);
            Callable<Void> task = () -> {
                try {
                    //Thread.sleep(rand.nextInt(1000));
                    AtomicValue<Long> value = count.increment();
                    //AtomicValue<Long> value = count.decrement();
                    //AtomicValue<Long> value = count.add((long)rand.nextInt(20));
                    System.out.println("succeed: " + value.succeeded());
                    if (value.succeeded())
                        System.out.println("Increment: from " + value.preValue() + " to " + value.postValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            };
            service.submit(task);
        }

        service.shutdown();
        service.awaitTermination(10, TimeUnit.MINUTES);

    }
}
