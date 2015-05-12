/*
 *
 *  * Copyright 2015 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 *
 */

package jcuda.jcublas.kernel;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class TestMultipleThreads {
	
	@Test
	public void testMultipleThreads() throws InterruptedException {
		int numThreads = 50;
		final INDArray array = Nd4j.rand(3000, 3000);
		final INDArray expected = array.dup().mmul(array).mmul(array).div(array).div(array);
		final AtomicInteger correct = new AtomicInteger();
		final CountDownLatch latch = new CountDownLatch(numThreads);
		
		ExecutorService executors = Executors.newCachedThreadPool();
		
		for(int x = 0; x< numThreads; x++) {
			executors.execute(new Runnable() {
				@Override
				public void run() {
					try
					{
						int total = 10000;
						int right = 0;
						for(int x = 0; x<total; x++) {
							INDArray actual = array.dup().mmul(array).mmul(array).div(array).div(array);
							if(expected.equals(actual)) right++;						
						}
						
						if(total == right)
							correct.incrementAndGet();
					} finally {
						latch.countDown();
					}
					
				}
			});
		}
		
		latch.await();
		
		assertEquals(numThreads, correct.get());
		
	}

}