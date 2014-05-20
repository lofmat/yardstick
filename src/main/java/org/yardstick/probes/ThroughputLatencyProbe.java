/*
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.yardstick.probes;

import org.yardstick.*;

import java.util.*;

import static org.yardstick.BenchmarkUtils.*;

/**
* Probe that calculates throughput and average latency.
*/
public class ThroughputLatencyProbe implements BenchmarkExecutionAwareProbe {
    /** */
    public static final String INTERVAL = "BENCHMARK_PROBE_THROUGHPUT_INTERVAL";

    /** */
    public static final long DEFAULT_INTERVAL_IN_MSECS = 1_000;

    /** Operations executed. */
    private ThreadAgent[] agents;

    /** Collected points. */
    private Collection<BenchmarkProbePoint> collected = new ArrayList<>();

    /** Thread collecting probe points. */
    private Thread collectingThread;

    /** */
    private BenchmarkConfiguration cfg;

    /** {@inheritDoc} */
    @SuppressWarnings("BusyWait")
    @Override public void start(BenchmarkDriver drv, BenchmarkConfiguration cfg) throws Exception {
        this.cfg = cfg;

        agents = new ThreadAgent[cfg.threads()];

        for (int i = 0; i < agents.length; i++)
            agents[i] = new ThreadAgent();

        final long interval = interval(cfg);

        collectingThread = new Thread() {
            @Override public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        ThreadAgent collector = new ThreadAgent();

                        for (ThreadAgent agent : agents)
                            agent.collect(collector);

                        double latency = collector.execCnt == 0 ? 0 : (double)collector.totalLatency / collector.execCnt;

                        BenchmarkProbePoint pnt = new BenchmarkProbePoint(System.currentTimeMillis(),
                            new double[] {collector.execCnt, latency});

                        collectPoint(pnt);

                        Thread.sleep(interval);
                    }
                }
                catch (InterruptedException ignore) {
                    // No-op, exit probe thread.
                }
            }
        };

        collectingThread.start();

        println(cfg, ThroughputLatencyProbe.class.getSimpleName() + " is started.");
    }

    /** {@inheritDoc} */
    @Override public void stop() throws Exception {
        if (collectingThread != null) {
            collectingThread.interrupt();
            collectingThread.join();

            println(cfg, ThroughputLatencyProbe.class.getSimpleName() + " is stopped.");
        }
    }

    /** {@inheritDoc} */
    @Override public Collection<String> metaInfo() {
        return Arrays.asList("Time, msec", "Operations/sec", "Latency, nsec");
    }

    /** {@inheritDoc} */
    @Override public synchronized Collection<BenchmarkProbePoint> points() {
        Collection<BenchmarkProbePoint> ret = collected;

        collected = new ArrayList<>(ret.size() + 5);

        return ret;
    }

    /**
     * @param pnt Probe point.
     */
    private synchronized void collectPoint(BenchmarkProbePoint pnt) {
        collected.add(pnt);
    }

    /** {@inheritDoc} */
    @Override public void beforeExecute(int threadIdx) {
        agents[threadIdx].beforeExecute();
    }

    /** {@inheritDoc} */
    @Override public void afterExecute(int threadIdx) {
        agents[threadIdx].afterExecute();
    }

    /**
     * @param cfg Config.
     * @return Interval.
     */
    private static long interval(BenchmarkConfiguration cfg) {
        try {
            return Long.parseLong(cfg.customProperties().get(INTERVAL));
        }
        catch (NumberFormatException | NullPointerException ignored) {
            return DEFAULT_INTERVAL_IN_MSECS;
        }
    }

    /**
     *
     */
    private static class ThreadAgent {
        /** Total execution count by thread. */
        private long execCnt;

        /** Total latency by  */
        private long totalLatency;

        /** Last before execute timestamp. */
        private long beforeTs;

        /**
         *
         */
        public void beforeExecute() {
            beforeTs = System.nanoTime();
        }

        /**
         *
         */
        public void afterExecute() {
            long latency = System.nanoTime() - beforeTs;

            beforeTs = 0;

            synchronized (this) {
                execCnt++;
                totalLatency += latency;
            }
        }

        /**
         * @param other Thread agent.
         */
        public synchronized void collect(ThreadAgent other) {
            other.execCnt += execCnt;
            other.totalLatency += totalLatency;

            execCnt = 0;
            totalLatency = 0;
        }
    }
}
