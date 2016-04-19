package ru.vitkud.test;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Second {

	@Test
	public void zTest() {
		System.out.println("Second.zTest()");
	}

	@Test
	public void test() {
		System.out.println("Second.test()");
	}

	@Test
	public void aTest() {
		System.out.println("Second.aTest()");
	}

}
