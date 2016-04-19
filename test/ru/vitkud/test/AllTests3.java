package ru.vitkud.test;

import junit.framework.TestCase;

public class AllTests3 extends TestCase {

	public void testTest1() {
		System.out.println("AllTests3.testTest1");
	}

	public void testTest2() {
		System.out.println("AllTests3.testTest2");
	}

	public static junit.framework.Test suite() {
	    return new junit.framework.JUnit4TestAdapter(AllTests.class);
	}

}
