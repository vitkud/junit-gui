package ru.vitkud.test;

import org.junit.Assume;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class First {

	@Test
	public void foo() {
		Assume.assumeTrue(false);
		System.out.println("First.foo()");
	}

	@Test
	public void bar() {
		System.out.println("First.bar()");
	}

	@Test
	@Ignore
	public void ignored() {
		System.out.println("First.ignored()");
	}

	@Test
	public void sleep() throws InterruptedException {
		Thread.sleep(5000L);
	}

}
