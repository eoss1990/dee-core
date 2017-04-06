package com.seeyon.v3x.dee;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DirectoryWatcher extends TimerTask implements FileFilter {
	protected static Log log = LogFactory.getLog(DirectoryWatcher.class);
	protected boolean _isDebugEnabled;
	protected boolean _isTraceEnabled;
	protected Pattern _pattern;
	protected File _directoryToWatch;

	/**
	 * Creates new DirectoryWatcher that watches for the given directory and
	 * uses the given Pattern to filter files in the directory.
	 * 
	 * @param directoryToWatch
	 *            directory to be watched.
	 * @param pattern
	 *            Pattern to filter files in the directory. If <em>null</em>,
	 *            then the default Pattern will be used. The default Pattern
	 *            allows all files.
	 * @see #accept(java.io.File)
	 */
	public DirectoryWatcher(String directoryToWatch, Pattern pattern) {
		this(new File(directoryToWatch), pattern);
	}

	/**
	 * Creates new DirectoryWatcher that watches for the given directory and
	 * uses the given Pattern to filter files in the directory.
	 *
	 * @param directoryToWatch
	 *            directory to be watched. * @param pattern Pattern to filter
	 *            files in the directory. If <em>null</em>, then the default
	 *            Pattern will be used. * The default Pattern allows all files.
	 *            * @see #accept(File)
	 */
	public DirectoryWatcher(File directoryToWatch, Pattern pattern) {
		_directoryToWatch = directoryToWatch;
		_pattern = pattern;
	}

	public void run() {
		File[] files = _directoryToWatch.listFiles(this);
		// Get list of files.
		if (files == null || files.length == 0) {
			return;
		}
		if (preProcess(_directoryToWatch)) {
			for (File file : files) {
				try {
					processFile(file);
				} catch (Exception e) {
					log.error(e);
				}
			}
			postProcess();
		}
	}

	/**
	 * This method is called before the {@link #processFile(java.io.File)} is invoked.
	 * This implementation checks whether current logger is enabled for the
	 * DEBUG and TRACE levels and always returns <em>true</em>.
	 *
	 * @param directoryToWatch
	 *            directory being watched.
	 * @return <em>true</em> to allow invocation of {@link #processFile(java.io.File)}.
	 */
	protected boolean preProcess(File directoryToWatch) {
		_isDebugEnabled = log.isDebugEnabled();
		_isTraceEnabled = log.isTraceEnabled();
		return true;
	}

	/**
	 * Processes the supplied file.
	 *
	 * @param file
	 *            file being processed. Should not be <em>null</em>.
	 * @return modified file.
	 * @throws Exception
	 *             if operation fails.
	 */
	protected abstract File processFile(File file) throws Exception;

	/**
	 * This method is called after the {@link #processFile(java.io.File)} is invoked.
	 * This implementation does nothing. Note that if {@link #preProcess(java.io.File)}
	 * returns <em>false</em>, this method will not be invoked.
	 */
	protected void postProcess() {
	}

	/**
	 * Tests whether or not the specified abstract pathname should be included
	 * in a pathname list. This implementation accepts files (not directories)
	 * which name matches pattern that was supplied in constructor. If no
	 * pattern was supplies, then all files (not directories) will match.
	 * 
	 * @param pathname
	 *            the abstract pathname to be tested.
	 * @return <code>true</code> if and only if <code>pathname</code> should be
	 *         included.
	 */
	public boolean accept(File pathname) {
		boolean res;
		if (pathname.isFile()) {
			if (_pattern != null) {
				String fileName = pathname.getName();
				Matcher m = _pattern.matcher(fileName);
				res = m.matches();
			} else {
				res = true;
			}
		} else {
			res = false;
		}
		if (_isTraceEnabled) {
			String fileName = pathname.getName();
			if (res) {
				log.trace("DirectoryWatcher: File " + fileName + " is allowed.");
			} else {
				log.trace("DirectoryWatcher: File " + fileName
						+ " is disallowed.");
			}
		}
		return res;
	}

}