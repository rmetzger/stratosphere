/***********************************************************************************************************************
 * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 **********************************************************************************************************************/

package eu.stratosphere.api.common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Set;

import eu.stratosphere.api.common.operators.GenericDataSink;
import eu.stratosphere.api.common.operators.Operator;
import eu.stratosphere.util.Visitable;
import eu.stratosphere.util.Visitor;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * This class encapsulates a single stratosphere job (an instantiated data flow), together with some parameters.
 * Parameters include the name and a default degree of parallelism. The job is referenced by the data sinks,
 * from which a traversal reaches all connected nodes of the job.
 */
public class Plan implements Visitable<Operator> {

	private static final int DEFAULT_PARALELLISM = -1;
	
	/**
	 * A collection of all sinks in the plan. Since the plan is traversed from the sinks to the sources, this
	 * collection must contain all the sinks.
	 */
	protected final Collection<GenericDataSink> sinks;

	/**
	 * The name of the job.
	 */
	protected final String jobName;

	/**
	 * The default parallelism to use for nodes that have no explicitly specified parallelism.
	 */
	protected int defaultParallelism = DEFAULT_PARALELLISM;
	
	/**
	 * The maximal number of machines to use in the job.
	 */
	protected int maxNumberMachines;

	protected HashMap<String, String> cacheFile = new HashMap<String, String>();

	// ------------------------------------------------------------------------

	/**
	 * Creates a new Stratosphere job with the given name, describing the data flow that ends at the
	 * given data sinks.
	 * <p>
	 * If not all of the sinks of a data flow are given to the plan, the flow might
	 * not be translated entirely. 
	 *  
	 * @param sinks The collection will the sinks of the job's data flow.
	 * @param jobName The name to display for the job.
	 */
	public Plan(Collection<GenericDataSink> sinks, String jobName) {
		this(sinks, jobName, DEFAULT_PARALELLISM);
	}

	/**
	 * Creates a new Stratosphere job with the given name and default parallelism, describing the data flow that ends
	 * at the given data sinks.
	 * <p>
	 * If not all of the sinks of a data flow are given to the plan, the flow might
	 * not be translated entirely.
	 *
	 * @param sinks The collection will the sinks of the job's data flow.
	 * @param jobName The name to display for the job.
	 * @param defaultParallelism The default degree of parallelism for the job.
	 */
	public Plan(Collection<GenericDataSink> sinks, String jobName, int defaultParallelism) {
		this.sinks = sinks;
		this.jobName = jobName;
		this.defaultParallelism = defaultParallelism;
	}

	/**
	 * Creates a new Stratosphere job with the given name, containing initially a single data sink.
	 * <p>
	 * If not all of the sinks of a data flow are given, the flow might
	 * not be translated entirely, but only the parts of the flow reachable by traversing backwards
	 * from the given data sinks.
	 * 
	 * @param sink The data sink of the data flow.
	 * @param jobName The name to display for the job.
	 */
	public Plan(GenericDataSink sink, String jobName) {
		this(sink, jobName, DEFAULT_PARALELLISM);
	}

	/**
	 * Creates a new Stratosphere job with the given name and default parallelism, containing initially a single data
	 * sink.
	 * <p>
	 * If not all of the sinks of a data flow are given, the flow might
	 * not be translated entirely, but only the parts of the flow reachable by traversing backwards
	 * from the given data sinks.
	 *
	 * @param sink The data sink of the data flow.
	 * @param jobName The name to display for the job.
	 * @param defaultParallelism The default degree of parallelism for the job.
	 */
	public Plan(GenericDataSink sink, String jobName, int defaultParallelism) {
		this.sinks = new ArrayList<GenericDataSink>();
		this.sinks.add(sink);
		this.jobName = jobName;
		this.defaultParallelism = defaultParallelism;
	}

	/**
	 * Creates a new Stratosphere job, describing the data flow that ends at the
	 * given data sinks. The display name for the job is generated using a timestamp.
	 * <p>
	 * If not all of the sinks of a data flow are given, the flow might
	 * not be translated entirely, but only the parts of the flow reachable by traversing backwards
	 * from the given data sinks. 
	 *  
	 * @param sinks The collection will the sinks of the data flow.
	 */
	public Plan(Collection<GenericDataSink> sinks) {
		this(sinks, DEFAULT_PARALELLISM);
	}

	/**
	 * Creates a new Stratosphere job with the given default parallelism, describing the data flow that ends at the
	 * given data sinks. The display name for the job is generated using a timestamp.
	 * <p>
	 * If not all of the sinks of a data flow are given, the flow might
	 * not be translated entirely, but only the parts of the flow reachable by traversing backwards
	 * from the given data sinks.
	 *
	 * @param sinks The collection will the sinks of the data flow.
	 * @param defaultParallelism The default degree of parallelism for the job.
	 */
	public Plan(Collection<GenericDataSink> sinks, int defaultParallelism) {
		this(sinks, "Stratosphere Job at " + Calendar.getInstance().getTime(), defaultParallelism);
	}

	/**
	 * Creates a new Stratosphere Job with single data sink.
	 * The display name for the job is generated using a timestamp.
	 * <p>
	 * If not all of the sinks of a data flow are given to the plan, the flow might
	 * not be translated entirely. 
	 * 
	 * @param sink The data sink of the data flow.
	 */
	public Plan(GenericDataSink sink) {
		this(sink, DEFAULT_PARALELLISM);
	}

	/**
	 * Creates a new Stratosphere Job with single data sink and the given default parallelism.
	 * The display name for the job is generated using a timestamp.
	 * <p>
	 * If not all of the sinks of a data flow are given to the plan, the flow might
	 * not be translated entirely.
	 *
	 * @param sink The data sink of the data flow.
	 * @param defaultParallelism The default degree of parallelism for the job.
	 */
	public Plan(GenericDataSink sink, int defaultParallelism) {
		this(sink, "Stratosphere Job at " + Calendar.getInstance().getTime(), defaultParallelism);
	}

	// ------------------------------------------------------------------------

	/**
	 * Adds a data sink to the set of sinks in this program.
	 * 
	 * @param sink The data sink to add.
	 */
	public void addDataSink(GenericDataSink sink) {
		if (!this.sinks.contains(sink)) {
			this.sinks.add(sink);
		}
	}

	/**
	 * Gets all the data sinks of this job.
	 * 
	 * @return All sinks of the program.
	 */
	public Collection<GenericDataSink> getDataSinks() {
		return this.sinks;
	}

	/**
	 * Gets the name of this job.
	 * 
	 * @return The name of the job.
	 */
	public String getJobName() {
		return this.jobName;
	}

	/**
	 * Gets the maximum number of machines to be used for this job.
	 * 
	 * @return The maximum number of machines to be used for this job.
	 */
	public int getMaxNumberMachines() {
		return this.maxNumberMachines;
	}

	/**
	 * Sets the maximum number of machines to be used for this job.
	 * 
	 * @param maxNumberMachines The the maximum number to set.
	 */
	public void setMaxNumberMachines(int maxNumberMachines) {
		this.maxNumberMachines = maxNumberMachines;
	}
	
	/**
	 * Gets the default degree of parallelism for this job. That degree is always used when an operator
	 * is not explicitly given a degree of parallelism.
	 *
	 * @return The default parallelism for the plan.
	 */
	public int getDefaultParallelism() {
		return this.defaultParallelism;
	}
	
	/**
	 * Sets the default degree of parallelism for this plan. That degree is always used when an operator
	 * is not explicitly given a degree of parallelism.
	 *
	 * @param defaultParallelism The default parallelism for the plan.
	 */
	public void setDefaultParallelism(int defaultParallelism) {
		this.defaultParallelism = defaultParallelism;
	}
	
	/**
	 * Gets the optimizer post-pass class for this job. The post-pass typically creates utility classes
	 * for data types and is specific to a particular data model (record, tuple, Scala, ...)
	 *
	 * @return The name of the class implementing the optimizer post-pass.
	 */
	public String getPostPassClassName() {
		return "eu.stratosphere.compiler.postpass.RecordModelPostPass";
	}
	
	// ------------------------------------------------------------------------

	/**
	 * Traverses the job depth first from all data sinks on towards the sources.
	 * 
	 * @see Visitable#accept(Visitor)
	 */
	@Override
	public void accept(Visitor<Operator> visitor) {
		for (GenericDataSink sink : this.sinks) {
			sink.accept(visitor);
		}
	}

	/**
	 *  register cache files in program level
	 * @param filePath The files must be stored in a place that can be accessed from all workers (most commonly HDFS)
	 * @param name user defined name of that file
	 */
	public void registerCachedFile(String filePath, String name) throws RuntimeException{
		if (!this.cacheFile.containsKey(name)) {
			this.cacheFile.put(name, filePath);
		} else {
			throw new RuntimeException("cache file " + name + "already exists!");
		}
	}

	/**
	 * return the registered caches files
	 * @return Set of (name, filePath) pairs
	 */
	public Set<Entry<String,String>> getCachedFiles() {
		return this.cacheFile.entrySet();
	}
}
