package ru.vitkud.test;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class First {

	@Test
	public void assumptionFailed() throws Exception {
		System.out.println("First.assumptionFailed()");
		Assume.assumeTrue("First.assumptionFailed()", false);
	}

	@Test
	public void done() throws Exception {
		System.out.println("First.done()");
		Assert.assertTrue(true);
	}

	@Test
	public void exceptionFailed() throws Exception {
		System.out.println("First.exceptionFailed()");
		throw new Exception("First.exceptionFailed()");
	}

	@Test
	public void failed() throws Exception {
		System.out.println("First.failed()");
		Assert.fail("First.failed()");
	}

	@Test @Ignore
	public void ignored() throws InterruptedException {
		System.out.println("First.ignored()");
	}

	@Test(expected = Exception.class)
	public void noException() throws Exception {
		System.out.println("First.noException()");
	}
}
