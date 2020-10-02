package test.benchmark;

import java.io.Serializable;

public class RandomRequest implements Serializable {

	String testString;
	long testLong;
	byte[] testBytes;

	public RandomRequest() {
	}

	public RandomRequest(String testString, long testLong, byte[] testBytes) {
		this.testString = testString;
		this.testLong = testLong;
		this.testBytes = testBytes;
	}

	public String getTestString() {
		return testString;
	}

	public long getTestLong() {
		return testLong;
	}

	public byte[] getTestBytes() {
		return testBytes;
	}

}