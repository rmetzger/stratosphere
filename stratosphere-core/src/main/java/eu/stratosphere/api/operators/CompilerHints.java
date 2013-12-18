/***********************************************************************************************************************
 *
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
 *
 **********************************************************************************************************************/

package eu.stratosphere.api.operators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import eu.stratosphere.api.operators.util.FieldSet;

/**
 * A class encapsulating compiler hints describing the behavior of the user function behind the contract.
 * If set, the optimizer will use them to estimate the sizes of the intermediate results that are processed
 * by the user functions encapsulated by the PACTs. No matter how these numbers are set, the compiler will
 * always generate a valid plan. But if these numbers are set adequately, the plans generated by the
 * PACT compiler may be significantly faster.
 * <p>
 * The numbers given by the user need not be absolutely accurate. Most importantly, they should give information whether
 * the data volume increases or decreases during the operation.
 * <p>
 * The following numbers can be declared:
 * <ul>
 * <li>The key cardinality. This describes the number of distinct keys produced by the user function. For example, if
 * the function takes records containing dates from the last three years and the functions puts the date as the key,
 * then you can set this field to roughly 1100, as that is the number of different dates. If the key cardinality is
 * unknown, this property is <code>-1</code>.</li>
 * <li>The selectivity on the key/value pairs. This factor describes how the number of keys/value pairs is changed by
 * the function encapsulated in the contract. For example, if a function declares a selectivity of 0.5, it declares that
 * it produces roughly half as many key/value pairs as it consumes. Its default value is <code>-1.0</code>, indicating
 * that the selectivity is unknown.</li>
 * <li>The average number of values per distinct key. <code>-1.0</code> by default, indicating that this value is
 * unknown.</li>
 * <li>The average number of bytes per record. <code>-1.0</code> by default, indicating that this value is unknown.</li>
 * </ul>
 */
public class CompilerHints {

	private float avgRecordsEmittedPerStubCall = -1.0f;

	private float avgBytesPerRecord = -1.0f;
	
	private Map<FieldSet, Long> distinctCounts = new HashMap<FieldSet, Long>();

	private Map<FieldSet, Float> avgNumRecordsPerDistinctFields = new HashMap<FieldSet, Float>();

	private Set<FieldSet> uniqueFields;

	// --------------------------------------------------------------------------------------------
	//  Basic Record Statistics
	// --------------------------------------------------------------------------------------------
	
	/**
	 * Gets the average number of bytes per record (key/value pair) for the
	 * contract containing these hints.
	 * 
	 * @return The average number of bytes per record, or -1.0, if unknown.
	 */
	public float getAvgBytesPerRecord() {
		return avgBytesPerRecord;
	}

	/**
	 * Sets the average number of bytes per record (key/value pair) for the
	 * contract containing these hints.
	 * 
	 * @param avgBytes
	 *        The average number of bytes per record.
	 */
	public void setAvgBytesPerRecord(float avgBytes) {
		if(avgBytes < 0) {
			throw new IllegalArgumentException("Average Bytes per Record must be  >= 0!");
		}
		this.avgBytesPerRecord = avgBytes;
	}

	/**
	 * Gets the average number of emitted records per stub call.
	 * 
	 * @return The average number of emitted records per stub call.
	 */
	public float getAvgRecordsEmittedPerStubCall() {
		return avgRecordsEmittedPerStubCall;
	}

	/**
	 * Sets the average number of emitted records per stub call. 
	 * 
	 * @param avgRecordsEmittedPerStubCall
	 *        The average number of emitted records per stub call to set.
	 */
	public void setAvgRecordsEmittedPerStubCall(float avgRecordsEmittedPerStubCall) {
		if(avgRecordsEmittedPerStubCall < 0) {
			throw new IllegalArgumentException("Average Number of Emitted Records per Function Call must be >= 0!");
		}
		this.avgRecordsEmittedPerStubCall = avgRecordsEmittedPerStubCall;
	}
	
	// --------------------------------------------------------------------------------------------
	//  Column (Group) Statistics
	// --------------------------------------------------------------------------------------------
	
	/**
	 * Gets the count of distinct values for the given set of fields.
	 * 
	 * @param fieldSet The set of fields for which the count is requested. 
	 * @return The count of distinct values for the given set of fields or -1, if unknown.
	 */
	public long getDistinctCount(FieldSet fieldSet) {
		Long estimate;
		if ((estimate = distinctCounts.get(fieldSet)) == null) {
			estimate = -1L;
		}
		return estimate;
	}

	/**
	 * Sets the count of distinct value combinations for the given set of fields.
	 * 
	 * @param fieldSet The set of fields for which the count is specified. 
	 * @param count The number of distinct value combinations for the specified set of fields.
	 */
	public void setDistinctCount(FieldSet fieldSet, long count) {
		if(count < 0) {
			throw new IllegalArgumentException("Cardinality must be >= 0!");
		}
		this.distinctCounts.put(fieldSet, count);
	}

	/**
	 * Returns all specified distinct counts.
	 * 
	 * @return all specified distinct counts.
	 */
	public Map<FieldSet, Long> getDistinctCounts() {
		return distinctCounts;
	}
	
	/**
	 * Gets the average number of records per distinct field set from the contract containing these hints.
	 * 
	 * @return The average number of records per distinct field set or -1.0, if not set.
	 */
	public float getAvgNumRecordsPerDistinctFields(FieldSet columnSet) {
		Float avg;
		if ((avg = avgNumRecordsPerDistinctFields.get(columnSet)) == null) {
			avg = -1.0F;
		}
		return avg;
	}
	
	/**
	 * Gets the average number of records for all specified field sets.
	 * 
	 * @return The average number of records for all specified field sets.
	 */
	public Map<FieldSet, Float> getAvgNumRecordsPerDistinctFields() {
		return avgNumRecordsPerDistinctFields;
	}

	/**
	 * Sets the average number of records per distinct field set for the contract containing these hints.
	 * 
	 * @param avgNumRecords
	 *        The average number of records per distinct field set to set.
	 */
	public void setAvgNumRecordsPerDistinctFields(FieldSet fieldSet, float avgNumRecords) {
		if(avgNumRecords < 0) {
			throw new IllegalArgumentException("Average Number of Values per distinct Values must be >= 0");
		}
		this.avgNumRecordsPerDistinctFields.put(fieldSet, avgNumRecords);
	}
	
	// --------------------------------------------------------------------------------------------
	//  Uniqueness
	// --------------------------------------------------------------------------------------------

	/**
	 * Gets the FieldSets that are unique
	 * 
	 * @return List of FieldSet that are unique
	 */
	public Set<FieldSet> getUniqueFields() {
		return this.uniqueFields;
	}
	
	/**
	 * Adds a FieldSet to be unique
	 * 
	 * @param uniqueFieldSet The unique FieldSet
	 */
	public void addUniqueField(FieldSet uniqueFieldSet) {
		if (this.uniqueFields == null) {
			this.uniqueFields = new HashSet<FieldSet>();
		}
		this.uniqueFields.add(uniqueFieldSet);
	}
	
	/**
	 * Adds a field as having only unique values.
	 * 
	 * @param field The field with unique values.
	 */
	public void addUniqueField(int field) {
		if (this.uniqueFields == null) {
			this.uniqueFields = new HashSet<FieldSet>();
		}
		this.uniqueFields.add(new FieldSet(field));
	}
	
	/**
	 * Adds multiple FieldSets to be unique
	 * 
	 * @param uniqueFieldSets A set of unique FieldSet
	 */
	public void addUniqueFields(Set<FieldSet> uniqueFieldSets) {
		if (this.uniqueFields == null) {
			this.uniqueFields = new HashSet<FieldSet>();
		}
		this.uniqueFields.addAll(uniqueFieldSets);
	}
	
	public void clearUniqueFields() {
		this.uniqueFields = null;
	}
	
	// --------------------------------------------------------------------------------------------
	//  Miscellaneous
	// --------------------------------------------------------------------------------------------
	
	protected void copyFrom(CompilerHints source) {
		this.avgRecordsEmittedPerStubCall = source.avgRecordsEmittedPerStubCall;
		this.avgBytesPerRecord = source.avgBytesPerRecord;
		this.distinctCounts.putAll(source.distinctCounts);
		this.avgNumRecordsPerDistinctFields.putAll(source.avgNumRecordsPerDistinctFields);
		
		if (source.uniqueFields != null && source.uniqueFields.size() > 0) {
			if (this.uniqueFields == null) {
				this.uniqueFields = new HashSet<FieldSet>();
			} else {
				this.uniqueFields.clear();
			}
			this.uniqueFields.addAll(source.uniqueFields);
		}
	}
}
