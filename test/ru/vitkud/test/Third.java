package ru.vitkud.test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		Third.InnerA.class,
		Third.InnerB.class,
		Third.InnerC.class,
		Third.InnerD.class})
final public class Third {
	private Third() { }

	private static Object tc = new Object();

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class InnerA {

		@Test
		public void testA1() throws IOException {
			assertNotNull(tc);
			// ...
		}

		@Test
		public void testA2() throws IOException {
			assertNotNull(tc);
			// ...
		}

	}

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class InnerB {

		@Test
		public void testB1() throws Exception {
			assertNotNull(tc);
			// ...
		}

		@Test
		public void testB2() throws Exception {
			assertNotNull(tc);
			// ...
		}
	}

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class InnerC {

		@Test
		public void testC1() throws Exception {
			assertNotNull(tc);
			// ...
		}

		@Test
		public void testC2() throws Exception {
			assertNotNull(tc);
			// ...
		}
	}

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class InnerD {

		@Test
		public void testD1() throws Exception {
			assertNotNull(tc);
			// ...
		}

		@Test
		public void testD2() throws Exception {
			assertNotNull(tc);
			// ...
		}

	}

}
