/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.mina.core;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.mina.core.BenchmarkFactory.Type;
import org.apache.mina.util.AvailablePortFinder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public abstract class BenchmarkBinaryTest {
    private int port;

    private BenchmarkServer server;

    private BenchmarkClient client;

    public abstract Type getClientType();

    public abstract Type getServerType();

    private static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of(100000, 10, 2 * 60),
                Arguments.of(100000, 1 * 1024, 2 * 60),
                Arguments.of(100000, 10 * 1024, 2 * 60),
                Arguments.of(100, 64 * 1024 * 1024, 10 * 60)
        );
    }

    @BeforeEach
    public void init() throws IOException {
        port = AvailablePortFinder.getNextAvailable();
        server = BenchmarkServerFactory.INSTANCE.get(getServerType());
        server.start(port);
        client = BenchmarkClientFactory.INSTANCE.get(getClientType());
    }

    @AfterEach
    public void shutdown() throws IOException {
        client.stop();
        server.stop();
    }

    /**
     * Send "numberOfMessages" messages to a server. Currently, 1 million, with two different
     * size, 10Ko and 64Ko.
     */
    @ParameterizedTest
    @MethodSource("parameters")
    public void benchmark(int numberOfMessages, int messageSize, int timeout) throws IOException, InterruptedException {
        byte[] data = new byte[messageSize + 4];
        data[0] = (byte) (messageSize >>> 24 & 255);
        data[1] = (byte) (messageSize >>> 16 & 255);
        data[2] = (byte) (messageSize >>> 8 & 255);
        data[3] = (byte) (messageSize & 255);

        CountDownLatch counter = new CountDownLatch(numberOfMessages);

        client.start(port, counter, data);
        boolean result = counter.await(timeout, TimeUnit.SECONDS);
        assertTrue(result, "Still " + counter.getCount() + " messages to send on a total of " + numberOfMessages);
    }
}
