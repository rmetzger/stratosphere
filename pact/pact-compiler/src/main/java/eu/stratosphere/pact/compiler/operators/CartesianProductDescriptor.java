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

package eu.stratosphere.pact.compiler.operators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.stratosphere.pact.compiler.dataproperties.GlobalProperties;
import eu.stratosphere.pact.compiler.dataproperties.PartitioningProperty;
import eu.stratosphere.pact.compiler.dataproperties.RequestedGlobalProperties;
import eu.stratosphere.pact.compiler.dataproperties.RequestedLocalProperties;
import eu.stratosphere.pact.compiler.plan.TwoInputNode;
import eu.stratosphere.pact.compiler.plan.candidate.Channel;
import eu.stratosphere.pact.compiler.plan.candidate.DualInputPlanNode;

/**
 *
 */
public abstract class CartesianProductDescriptor extends OperatorDescriptorDual
{
	/* (non-Javadoc)
	 * @see eu.stratosphere.pact.compiler.dataproperties.DriverPropertiesDual#createPossibleGlobalProperties()
	 */
	@Override
	protected List<GlobalPropertiesPair> createPossibleGlobalProperties() {
		ArrayList<GlobalPropertiesPair> pairs = new ArrayList<GlobalPropertiesPair>();
		{ // replicate second
			RequestedGlobalProperties any1 = new RequestedGlobalProperties();
			RequestedGlobalProperties replicated2 = new RequestedGlobalProperties();
			replicated2.setFullyReplicated();
			pairs.add(new GlobalPropertiesPair(any1, replicated2));
		}
		{ // replicate first
			RequestedGlobalProperties replicated1 = new RequestedGlobalProperties();
			replicated1.setFullyReplicated();
			RequestedGlobalProperties any2 = new RequestedGlobalProperties();
			pairs.add(new GlobalPropertiesPair(replicated1, any2));
		}
		return pairs;
	}
	
	/* (non-Javadoc)
	 * @see eu.stratosphere.pact.compiler.dataproperties.DriverPropertiesDual#createPossibleLocalProperties()
	 */
	@Override
	protected List<LocalPropertiesPair> createPossibleLocalProperties() {
		// all properties are possible
		return Collections.singletonList(new LocalPropertiesPair(
			new RequestedLocalProperties(), new RequestedLocalProperties()));
	}

	/* (non-Javadoc)
	 * @see eu.stratosphere.pact.compiler.dataproperties.DriverPropertiesDual#instantiate(eu.stratosphere.pact.compiler.plan.candidate.Channel, eu.stratosphere.pact.compiler.plan.candidate.Channel, eu.stratosphere.pact.compiler.plan.TwoInputNode)
	 */
	@Override
	public DualInputPlanNode instantiate(Channel in1, Channel in2, TwoInputNode node) {
		return new DualInputPlanNode(node, in1, in2, getStrategy());
	}
	
	/* (non-Javadoc)
	 * @see eu.stratosphere.pact.compiler.operators.OperatorDescriptorDual#computeGlobalProperties(eu.stratosphere.pact.compiler.dataproperties.GlobalProperties, eu.stratosphere.pact.compiler.dataproperties.GlobalProperties)
	 */
	@Override
	public GlobalProperties computeGlobalProperties(GlobalProperties in1, GlobalProperties in2) {
		GlobalProperties gp = GlobalProperties.combine(in1, in2);
		if (gp.getUniqueFieldCombination() != null && gp.getUniqueFieldCombination().size() > 0 &&
					gp.getPartitioning() == PartitioningProperty.RANDOM)
		{
			gp.setAnyPartitioning(gp.getUniqueFieldCombination().iterator().next().toFieldList());
		}
		gp.clearUniqueFieldCombinations();
		return gp;
	}
}