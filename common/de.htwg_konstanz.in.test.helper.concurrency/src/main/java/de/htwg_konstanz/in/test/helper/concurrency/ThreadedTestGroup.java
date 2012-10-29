package de.htwg_konstanz.in.test.helper.concurrency;
import org.junit.internal.runners.model.EachTestNotifier;

public class ThreadedTestGroup extends ThreadGroup {

  private final EachTestNotifier notifier;

  public ThreadedTestGroup(EachTestNotifier notifier, String name) {
    super(name);
    this.notifier = notifier;
  }

  public void uncaughtException(Thread t, Throwable e) {
      notifier.addFailure(e);
  }
}
