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
package eu.stratosphere.types.parser;

import eu.stratosphere.types.DateValue;

public class DateParser extends FieldParser<DateValue> {

	private DateValue result;
	private String format = null;

	@Override
	public int parseField(byte[] bytes, int startPos, int limit, char delim, DateValue reusable) {

		int i = startPos;
		final byte delByte = (byte) delim;

		while (i < limit && bytes[i] != delByte) {
			i++;
		}

		String str = new String(bytes, startPos, i-startPos);
		try {
			if (format!=null) this.result = new DateValue(str,format); //specified format;
			else this.result = new DateValue(str); //ISO 8061
			return (i == limit) ? limit : i+1;
		}
		catch (NumberFormatException e) {
			return -1;
		}
	}

	public void setFormat(String format) {
		this.format = format;
	}
	@Override
	public DateValue createValue() {
		return new DateValue();
	}

	@Override
	public DateValue getLastResult() {
		return result;
	}
}