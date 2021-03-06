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

package org.yardstickframework;

/**
 * Probe that is notified before and after each test sample execution.
 */
public interface BenchmarkExecutionAwareProbe extends BenchmarkProbe {
    /**
     * Before test sample execution callback.
     *
     * @param threadIdx Thread index.
     */
    public void beforeExecute(int threadIdx);

    /**
     * After test sample execution callback.
     *
     * @param threadIdx Thread index.
     */
    public void afterExecute(int threadIdx);
}
