/***********************************************************************************************************************
 *
 * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
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

package eu.stratosphere.pact.common.type.base;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import eu.stratosphere.nephele.services.memorymanager.DataInputView;
import eu.stratosphere.nephele.services.memorymanager.DataOutputView;
import eu.stratosphere.pact.common.type.CopyableValue;
import eu.stratosphere.pact.common.type.Key;
import eu.stratosphere.pact.common.type.DeNormalizableKey;

/**
 * Integer base type for PACT programs that implements the Key interface.
 * PactInteger encapsulates a Java primitive int.
 * 
 * @see eu.stratosphere.pact.common.type.Key
 */
public class PactInteger implements Key, DeNormalizableKey, CopyableValue<PactInteger> {
	
	private int value;

	/**
	 * Initializes the encapsulated int with 0.
	 */
	public PactInteger() {
		this.value = 0;
	}

	/**
	 * Initializes the encapsulated int with the provided value.
	 * 
	 * @param value Initial value of the encapsulated int.
	 */
	public PactInteger(int value) {
		this.value = value;
	}
	
	/**
	 * Returns the value of the encapsulated int.
	 * 
	 * @return the value of the encapsulated int.
	 */
	public int getValue() {
		return this.value;
	}

	/**
	 * Sets the encapsulated int to the specified value.
	 * 
	 * @param value
	 *        the new value of the encapsulated int.
	 */
	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.valueOf(this.value);
	}

	// --------------------------------------------------------------------------------------------
	
	@Override
	public void read(DataInput in) throws IOException {
		this.value = in.readInt();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(this.value);
	}

	// --------------------------------------------------------------------------------------------
	
	@Override
	public int compareTo(final Key o) {
		if (!(o instanceof PactInteger))
			throw new ClassCastException("Cannot compare " + o.getClass().getName() + " to PactInteger!");

		final int other = ((PactInteger) o).value;

		return this.value < other ? -1 : this.value > other ? 1 : 0;
	}

	@Override
	public int hashCode() {
		return this.value;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof PactInteger) {
			return ((PactInteger) obj).value == this.value;
		}
		return false;
	}
	
	// --------------------------------------------------------------------------------------------

	@Override
	public int getMaxNormalizedKeyLen() {
		return 4;
	}

	@Override
	public void copyNormalizedKey(byte[] target, int offset, int len) {
		if (len == 4) {
			// default case, full normalized key
			int highByte = ((value >>> 24) & 0xff);
			highByte -= Byte.MIN_VALUE;
			target[offset    ] = (byte) highByte;
			target[offset + 1] = (byte) ((value >>> 16) & 0xff);
			target[offset + 2] = (byte) ((value >>>  8) & 0xff);
			target[offset + 3] = (byte) ((value       ) & 0xff);
		}
		else if (len <= 0) {
		}
		else if (len < 4) {
			int highByte = ((value >>> 24) & 0xff);
			highByte -= Byte.MIN_VALUE;
			target[offset    ] = (byte) highByte;
			len--;
			for (int i = 1; len > 0; len--, i++) {
				target[offset + i] = (byte) ((value >>> ((3-i)<<3)) & 0xff);
			}
		}
		else {
			int highByte = ((value >>> 24) & 0xff);
			highByte -= Byte.MIN_VALUE;
			target[offset    ] = (byte) highByte;
			target[offset + 1] = (byte) ((value >>> 16) & 0xff);
			target[offset + 2] = (byte) ((value >>>  8) & 0xff);
			target[offset + 3] = (byte) ((value       ) & 0xff);
			for (int i = 4; i < len; i++) {
				target[offset + i] = 0;
			}
		}
	}
	
	@Override
	public void readFromNormalizedKey(byte[] source, int offset, int len) {
		if (len == 4) {
			// the only allowed case
			value = 0;
			value |= (((source[offset    ] - Byte.MIN_VALUE) & 0xFF) << 24);
			value |= ((source[offset + 1] & 0xFF) << 16);
			value |= ((source[offset + 2] & 0xFF) <<  8);
			value |= ((source[offset + 3] & 0xFF)      );
		}
		else {
			throw new IllegalArgumentException("We can only read from normalized keys if the have full length.");
		}
	}

	// --------------------------------------------------------------------------------------------
	
	@Override
	public int getBinaryLength() {
		return 4;
	}
	
	@Override
	public void copyTo(PactInteger target) {
		target.value = this.value;
	}

	@Override
	public void copy(DataInputView source, DataOutputView target) throws IOException {
		target.write(source, 4);
	}
}
