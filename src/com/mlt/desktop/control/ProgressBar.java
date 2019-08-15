/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.mlt.desktop.control;

import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicProgressBarUI;

import com.mlt.desktop.AWT;
import com.mlt.desktop.layout.Orientation;
import com.mlt.util.Numbers;
import java.awt.EventQueue;
import java.awt.Graphics;

/**
 * Progress bar control.
 *
 * @author Miquel Sas
 */
public class ProgressBar extends Control {

	/**
	 * Progress bar UI.
	 */
	private class ProgressBarUI extends BasicProgressBarUI {

		/**
		 * Change listener to call on changes values, not only exact percentages of change.
		 */
		class ChangeHandler implements ChangeListener {

			/** Cached percentage. */
			private double previousPercent = -1;

			@Override
			public void stateChanged(ChangeEvent e) {
				double maximum = progressBar.getMaximum();
				double minimum = progressBar.getMinimum();
				if (maximum - minimum == 0)
					return;
				double value = progressBar.getValue();
				double percent = Numbers.round(Math.min((value - minimum) / (maximum - minimum), 1.0), 4);
				if (percent != previousPercent) {
					previousPercent = percent;
					progressBar.repaint();
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void installListeners() {
			super.installListeners();
			progressBar.removeChangeListener(changeListener);
			progressBar.addChangeListener(new ChangeHandler());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void paintIndeterminate(Graphics g, JComponent c) {
			super.paintIndeterminate(g, c);
		}
	}

	/**
	 * Constructor.
	 */
	public ProgressBar() {
		this(Orientation.HORIZONTAL);
	}

	/**
	 * Constructor.
	 *
	 * @param orientation Orientation.
	 */
	public ProgressBar(Orientation orientation) {
		super();
		setComponent(new JProgressBar(AWT.toAWT(orientation)));
		getComponent().setUI(new ProgressBarUI());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final JProgressBar getComponent() {
		return (JProgressBar) super.getComponent();
	}

	/*
	 * Specific progress bar functionality.
	 */
	/**
	 * Check indeterminate.
	 *
	 * @return A boolean.
	 * @see javax.swing.JProgressBar#isIndeterminate()
	 */
	public boolean isIndeterminate() {
		return getComponent().isIndeterminate();
	}

	/**
	 * Set indeterminate.
	 *
	 * @param indeterminate A boolean.
	 * @see javax.swing.JProgressBar#setIndeterminate(boolean)
	 */
	public void setIndeterminate(boolean indeterminate) {
		EventQueue.invokeLater(() -> {
			getComponent().setIndeterminate(indeterminate);
		});
	}

	/**
	 * Return if the percentage string should be painted.
	 *
	 * @return A boolean.
	 * @see javax.swing.JProgressBar#isStringPainted()
	 */
	public boolean isStringPainted() {
		return getComponent().isStringPainted();
	}

	/**
	 * Set if the percentage string should be painted.
	 *
	 * @param b A boolean.
	 * @see javax.swing.JProgressBar#setStringPainted(boolean)
	 */
	public void setStringPainted(boolean b) {
		getComponent().setStringPainted(b);
	}

	/**
	 * Set the work done.
	 *
	 * @param workDone The work done.
	 * @see javax.swing.JProgressBar#setValue(int)
	 */
	public void setWorkDone(int workDone) {
		getComponent().setValue(workDone);
	}

	/**
	 * Set the total work.
	 *
	 * @param totalWork Total work.
	 * @see javax.swing.JProgressBar#setMaximum(int)
	 */
	public void setTotalWork(int totalWork) {
		getComponent().setMaximum(totalWork);
	}

}
