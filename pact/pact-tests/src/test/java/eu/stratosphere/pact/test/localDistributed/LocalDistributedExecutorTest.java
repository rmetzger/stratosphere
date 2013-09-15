/***********************************************************************************************************************
 *
 * Copyright (C) 2012 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/
package eu.stratosphere.pact.test.localDistributed;

import java.io.File;
import java.io.FileWriter;

import org.junit.Assert;
import org.junit.Test;

import eu.stratosphere.nephele.util.Logging;
import eu.stratosphere.pact.clients.examples.LocalExecutorTest;
import eu.stratosphere.pact.example.wordcount.WordCount;


public class LocalDistributedExecutorTest {

	@Test
	public void testLocalDistributedExecutorWithWordCount() {
		Logging.initialize();
		try {
			// set up the files
			File inFile = File.createTempFile("wctext", ".in");
			File outFile = File.createTempFile("wctext", ".out");
			inFile.deleteOnExit();
			outFile.deleteOnExit();
			
			FileWriter fw = new FileWriter(inFile);
			fw.write(LocalExecutorTest.TEXT);
			fw.close();
			
			// run WordCount
			WordCount wc = new WordCount();
			LocalDistributedExecutor.run( wc.getPlan("4", "file://" + inFile.getAbsolutePath(), "file://" + outFile.getAbsolutePath()), 3);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		
	}
}
