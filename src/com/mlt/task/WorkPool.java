package com.mlt.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.locks.ReentrantLock;

import com.mlt.util.Numbers;
import com.mlt.util.Strings;

/**
 * A work pool that buffers works, that can eventually throw an exception, to an execution pool.
 * 
 * @author Miquel Sas
 */
public class WorkPool {

	/**
	 * Executor thread.
	 */
	class Executor implements Runnable {

		boolean terminated = false;
		boolean idle = true;

		@Override
		public void run() {
			List<WorkWrapper> worksToExecute = new ArrayList<>();
			while (true) {

				/* Get pending works. */
				try {
					lockPool.lock();
					worksToExecute.addAll(works);
					works.clear();
					idle = worksToExecute.isEmpty();
				} finally {
					lockPool.unlock();
				}

				/* If there are pending works. */
				if (!worksToExecute.isEmpty()) {
					/* Do execute them. */
					pool.invokeAll(worksToExecute);
					/* Register exceptions. */
					List<Exception> exceptions = new ArrayList<>();
					for (WorkWrapper work : worksToExecute) {
						Exception exception = work.getException();
						if (exception != null) {
							exceptions.add(exception);
						}
					}
					try {
						lockPool.lock();
						WorkPool.this.exceptions.addAll(exceptions);
					} finally {
						lockPool.unlock();
					}
				}

				/* Clear list. */
				worksToExecute.clear();

				/* Check terminate orderly. */
				try {
					lockPool.lock();
					if (shutdown && works.isEmpty()) {
						break;
					}
				} finally {
					lockPool.unlock();
				}
			}

			/* Shutdown the pool. */
			pool.shutdown();

			/* Set the thread as terminated. */
			try {
				lockPool.lock();
				terminated = true;
			} finally {
				lockPool.unlock();
			}
		}
	}

	/**
	 * Task.
	 */
	public interface Work {
		void execute() throws Exception;
	}

	/**
	 * Work wrapper.
	 */
	class WorkWrapper implements Callable<Void> {

		Work work;
		Exception exception;
		ReentrantLock lockWork;

		WorkWrapper(Work work) {
			this.work = work;
			this.lockWork = new ReentrantLock();
		}

		@Override
		public Void call() throws Exception {
			try {
				work.execute();
			} catch (Exception exc) {
				try {
					lockWork.lock();
					exception = exc;
				} finally {
					lockWork.unlock();
				}
			}
			return null;
		}
		
		Exception getException() {
			try {
				lockWork.lock();
				return exception;
			} finally {
				lockWork.unlock();
			}
		}
	}

	/**
	 * Thread of the pool, named using the root name.
	 */
	class Worker extends ForkJoinWorkerThread {
		protected Worker(ForkJoinPool pool) {
			super(pool);
			int pad = Numbers.getDigits(pool.getParallelism());
			String index = Integer.toString(getPoolIndex());
			setName(name + "-EXEC-" + Strings.leftPad(index, pad, "0"));
		}
	}

	/**
	 * Worker thread factory.
	 */
	class WorkerFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
		@Override
		public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
			return new Worker(pool);
		}
	}

	/** List of works pending to execute. */
	private List<WorkWrapper> works;
	/** Fork join pool for execution. */
	private ForkJoinPool pool;
	/** Maximum number of pending works. */
	private int maxWorks;
	/** Root name of the pool. */
	private String name;
	/** Shared lock. */
	private ReentrantLock lockPool;
	/** Shutdown indicator. */
	private boolean shutdown;
	/** List of exceptions thrown by works. */
	private List<Exception> exceptions;
	/** Executor thread. */
	private Executor executor;

	/**
	 * Default constructor.
	 * 
	 * @param name The name.
	 */
	public WorkPool(String name) {
		this(name, 120, 24000);
	}

	/**
	 * Constructor.
	 * 
	 * @param name Root name.
	 * @param poolSize Pool size.
	 * @param maxWorks Maximum pending works.
	 */
	public WorkPool(String name, int poolSize, int maxWorks) {

		/* Initialize members. */
		this.name = name;
		this.maxWorks = maxWorks;
		this.works = new ArrayList<>();
		this.pool = new ForkJoinPool(poolSize, new WorkerFactory(), null, true);
		this.lockPool = new ReentrantLock();
		this.shutdown = false;
		this.exceptions = new ArrayList<>();

		/* Start the executor thread. */
		this.executor = new Executor();
		new Thread(this.executor, name + "-EXEC").start();
	}

	/**
	 * @return The list of exceptions.
	 */
	public List<Exception> getExceptions() {
		try {
			lockPool.lock();
			return Collections.unmodifiableList(exceptions);
		} finally {
			lockPool.unlock();
		}
	}

	/**
	 * @return A boolean that indicates if there are exceptions.
	 */
	public boolean isException() {
		try {
			lockPool.lock();
			return !exceptions.isEmpty();
		} finally {
			lockPool.unlock();
		}
	}

	/**
	 * Request a shutdown.
	 */
	public void shutdown() {
		try {
			lockPool.lock();
			shutdown = true;
		} finally {
			lockPool.unlock();
		}
	}

	/**
	 * @param work The work to submit.
	 * @return A boolean indicating that the work was correctly submitted.
	 */
	public boolean submit(Work work) {
		while (true) {
			try {
				lockPool.lock();

				/* Not admitted because a shutdown is in progress. */
				if (shutdown) {
					return false;
				}

				/* Wait for space. */
				if (works.size() < maxWorks) {
					works.add(new WorkWrapper(work));
					return true;
				}
			} finally {
				lockPool.unlock();
			}
		}
	}

	/**
	 * Request a shutdown and wait for executor termination.
	 */
	public void terminate() {
		shutdown();
		while (true) {
			try {
				lockPool.lock();
				if (executor.terminated) {
					return;
				}
			} finally {
				lockPool.unlock();
			}
		}
	}
	
	/**
	 * Wait until the executor is idle and there are no more works to execute.
	 */
	public void waitIdle() {
		while (true) {
			try {
				lockPool.lock();
				if (works.isEmpty() && executor.idle) {
					return;
				}
			} finally {
				lockPool.unlock();
			}
		}
	}
}
