package de.htwg_konstanz.in.test.helper.concurrency;

import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class MultiThreadedExceptionsRunner extends BlockJUnit4ClassRunner {

    public MultiThreadedExceptionsRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
    }
     
    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        EachTestNotifier eachNotifier= makeNotifier(method, notifier);
        if (method.getAnnotation(Ignore.class) != null) {
            runIgnored(eachNotifier);
        } else {
            runNotIgnored(method, eachNotifier);
        }
    }

    private void runNotIgnored(final FrameworkMethod method,
            final EachTestNotifier eachNotifier) {
        eachNotifier.fireTestStarted();
        ThreadedTestGroup g = new ThreadedTestGroup(eachNotifier, method.getName());
        Thread t = new Thread(g, method.getName()) {
            public void run() {
                try {
                    methodBlock(method).evaluate();
                } catch (AssumptionViolatedException e) {
                    eachNotifier.addFailedAssumption(e);
                } catch (Throwable e) {
                    eachNotifier.addFailure(e);
                } finally {
                    eachNotifier.fireTestFinished();
                }                
            };
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void runIgnored(EachTestNotifier eachNotifier) {
        eachNotifier.fireTestIgnored();
    }
    
    private EachTestNotifier makeNotifier(FrameworkMethod method,
            RunNotifier notifier) {
        Description description = describeChild(method);
        return new EachTestNotifier(notifier, description);
    }

}
