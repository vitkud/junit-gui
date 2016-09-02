package ru.vitkud.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		Third.InnerA.class,
		Third.InnerFailBeforeClass.class,
		Third.InnerFailAfterClass.class,
		Third.InnerFailBeforeAndAfterClass.class,
		Third.InnerFailBeforeAndAfter.class,
		Third.InnerFailAfter.class,
		Third.InnerSpecial.class,
		Third.InnerSpecial_2.class,
		Third.InnerIgnored.class,
		Third.InnerAssumptionFailed.class,
})
final public class Third {
	private Third() { }

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("Third.setUpBeforeClass()");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("Third.tearDownAfterClass()");
	}

	@Before
	public void setUp() throws Exception {
		System.out.println("Third.setUp()");
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("Third.tearDown()");
	}

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class InnerA {

		@Test
		public void testA1() throws IOException {
			System.out.println("Third.InnerA.testA1()");
			assertNotNull(new Object());
		}

		@Test
		public void testA2() throws IOException {
			System.out.println("Third.InnerA.testA2()");
			assertNotNull(new Object());
		}

	}

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class InnerFailBeforeClass {

		@BeforeClass
		public static void setUpBeforeClass() throws Exception {
			System.out.println("Third.InnerFailBeforeClass.setUpBeforeClass()");
			fail("Third.InnerFailBeforeClass.setUpBeforeClass()");
		}

		@Test
		public void test() throws Exception {
			System.out.println("Third.InnerFailBeforeClass.test()");
		}

	}

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class InnerFailAfterClass {

		@AfterClass
		public static void tearDownAfterClass() throws Exception {
			System.out.println("Third.InnerFailAfterClass.tearDownAfterClass()");
			fail("Third.InnerFailAfterClass.tearDownAfterClass()");
		}

		@Test
		public void test() throws Exception {
			System.out.println("Third.InnerFailAfterClass.test()");
		}

	}

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class InnerFailBeforeAndAfterClass {

		@BeforeClass
		public static void setUpBeforeClass() throws Exception {
			System.out.println("Third.InnerFailBeforeAndAfterClass.setUpBeforeClass()");
			fail("Third.InnerFailBeforeAndAfterClass.setUpBeforeClass()");
		}

		@AfterClass
		public static void tearDownAfterClass() throws Exception {
			System.out.println("Third.InnerFailBeforeAndAfterClass.tearDownAfterClass()");
			fail("Third.InnerFailBeforeAndAfterClass.tearDownAfterClass()");
		}

		@Test
		public void test() throws Exception {
			System.out.println("Third.InnerFailBeforeAndAfterClass.test()");
		}

	}

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class InnerFailBeforeAndAfter {

		@Before
		public void setUp() throws Exception {
			System.out.println("Third.InnerFailBeforeAndAfter.setUp()");
			fail("Third.InnerFailBeforeAndAfter.setUp()");
		}

		@After
		public void tearDown() throws Exception {
			System.out.println("Third.InnerFailBeforeAndAfter.tearDown()");
			fail("Third.InnerFailBeforeAndAfter.tearDown()");
		}

		@Test
		public void test() throws Exception {
			System.out.println("Third.InnerFailBeforeAndAfter.test()");
		}

	}

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class InnerFailAfter {

		@After
		public void tearDown() throws Exception {
			System.out.println("Third.InnerFailAfter.tearDown()");
			fail("Third.InnerFailAfter.tearDown()");
		}

		@Test
		public void test() throws Exception {
			System.out.println("Third.InnerFailAfter.test()");
		}

		@Test
		public void testFail() throws Exception {
			System.out.println("Third.InnerFailAfter.testFail()");
			fail("Third.InnerFailAfter.testFail()");
		}

	}

	public static class InnerSpecial {

		static boolean evenCall = false;

		@Test
		public void failEvenCall() throws Exception {
			System.out.println("Third.InnerSpecial.failEvenCall()");
			evenCall = !evenCall;
			if (!evenCall) fail("Third.InnerSpecial.failEvenCall()");
		}

	}

	public static class InnerSpecial_2 extends InnerSpecial {
		
	}

	@RunWith(Suite.class)
	@Suite.SuiteClasses({InnerIgnored.InnerIgnoredInner.class})
	@Ignore
	public static class InnerIgnored {

		public static class InnerIgnoredInner {

			@Test
			public void test() throws Exception {
				System.out.println("Third.InnerIgnoredInner.test()");
			}
	
			@Test
			public void testFail() throws Exception {
				System.out.println("Third.InnerIgnoredInner.testFail()");
				fail("Third.InnerIgnoredInner.testFail()");
			}

		}

	}

	@RunWith(Suite.class)
	@Suite.SuiteClasses({InnerAssumptionFailed.InnerAssumptionFailedInner.class})
	public static class InnerAssumptionFailed {

		@BeforeClass
		public static void setUpBeforeClass() throws Exception {
			System.out.println("Third.InnerAssumptionFailed.setUpBeforeClass()");
			Assume.assumeTrue("Third.InnerAssumptionFailed.setUpBeforeClass()", false);
		}

		public static class InnerAssumptionFailedInner {

			@Test
			public void test() throws Exception {
				System.out.println("Third.InnerAssumptionFailed.InnerAssumptionFailedInner.test()");
			}
	
			@Test
			public void testFail() throws Exception {
				System.out.println("Third.InnerAssumptionFailed.InnerAssumptionFailedInner.testFail()");
				fail("Third.InnerAssumptionFailed.InnerAssumptionFailedInner.testFail()");
			}

		}

	}
}
