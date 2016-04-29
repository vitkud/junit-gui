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
	public void aPause() throws InterruptedException {
		System.out.println("First.aPause()");
		Thread.sleep(5000L);
	}

	@Test
	public void assumptionFailed() {
		Assume.assumeTrue(false);
	}
	
	@Test
	public void foo() throws InterruptedException {
		Thread.sleep(3000L);
		Assume.assumeTrue(false);
		System.out.println("First.foo()");
		Thread.sleep(3000L);
	}

	@Test
	public void bar() throws InterruptedException {
		Thread.sleep(3000L);
		System.out.println("First.bar()");
		Thread.sleep(3000L);
	}

	@Test
	@Ignore
	public void ignored() throws InterruptedException {
		Thread.sleep(3000L);
		System.out.println("First.ignored()");
		Thread.sleep(3000L);
	}

	@Test
	public void sleep() throws InterruptedException {
		Thread.sleep(5000L);
	}

	@Test
	public void fail() {
		Assert.fail();
	}

	@Test
	public void exception() throws Exception {
		throw new Exception("Exception");
	}

	@Test(expected = Exception.class)
	public void noException() throws Exception {
	}
}
