package vn.com.vng.zalopay.data.taskqueue;

/**
 * Inject dependencies into tasks of any kind.
 *
 * @param <T> The type of tasks to inject.
 */
public interface TaskInjector<T extends Task> {
  void injectMembers(T task);
}
