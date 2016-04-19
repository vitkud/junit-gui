package ru.vitkud.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	First.class,
	Second.class,
	Third.class})

public class AllTests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("AllTests.setUpBeforeClass()");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("AllTests.tearDownAfterClass()");
	}

	@Before
	public void setUp() throws Exception {
		System.out.println("AllTests.setUp()");
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("AllTests.tearDown()");
	}

}
