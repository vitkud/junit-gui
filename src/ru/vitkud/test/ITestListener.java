package ru.vitkud.test;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public interface ITestListener {

	void testingStarts();
	void startTest(Description description);

	void addSuccess(Description description);
	void addError(Failure failure);
	void addFailure(Failure failure);

	void endTest(Description description);
	void testingEnds(Result result);

	void startSuite(Description description);
	void endSuite(Description description);

}
