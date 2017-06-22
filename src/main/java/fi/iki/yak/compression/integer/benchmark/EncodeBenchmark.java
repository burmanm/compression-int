/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.iki.yak.compression.integer.benchmark;

import java.util.concurrent.ThreadLocalRandom;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import fi.iki.yak.compression.integer.Simple8;
import fi.iki.yak.compression.integer.Simple8RLE;

/**
 * @author michael
 */
@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 10) // Reduce the amount of iterations if you start to see GC interference
public class EncodeBenchmark {

    @State(Scope.Benchmark)
    public static class DataGenerator {
        public long[] input;
        public long[] output;
        public long[] compressed;
        public long[] decompressed;
        public int amount = 0;

        @Setup(Level.Trial)
        public void setup() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            input = random.longs(0, 10000).limit(10000).toArray();
            output = new long[input.length];
            compressed = new long[input.length];
            amount = Simple8RLE.compress(input, compressed);
            decompressed = new long[input.length];
        }
    }

    @Benchmark
    @OperationsPerInvocation(10000)
    public void encodingBenchmark(DataGenerator dg, Blackhole bh) {
        bh.consume(Simple8RLE.compress(dg.input, dg.output));
    }

    @Benchmark
    @OperationsPerInvocation(10000)
    public void decodingBenchmark(DataGenerator dg, Blackhole bh) {
        Simple8RLE.decompress(dg.compressed, 0, dg.amount, dg.decompressed, 0);
    }
}
