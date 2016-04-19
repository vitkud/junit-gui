package ru.vitkud.test;

import org.junit.Assume;
import org.junit.FixMethodOrder;
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

}
