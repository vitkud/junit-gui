package ru.vitkud.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Second {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("Second.setUpBeforeClass()");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("Second.tearDownAfterClass()");
	}

	@Before
	public void setUp() throws Exception {
		System.out.println("Second.setUp()");
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("Second.tearDown()");
	}

	@Test
	public void assumFailed() throws Exception {
		System.out.println("Second.assumFailed()");
		Assume.assumeTrue("Second.assumFailed()", false);
	}

	@Test
	public void hTestSleep3s() throws Exception {
		System.out.println("Second.hTestSleep3s()");
		Thread.sleep(3000L);
	}

	@Test @Ignore("S")
	public void ignoredS() throws Exception {
		System.out.println("Second.ignoredS()");
	}

	@Test
	public void test() throws Exception {
		System.out.println("Second.test()");
	}

}
