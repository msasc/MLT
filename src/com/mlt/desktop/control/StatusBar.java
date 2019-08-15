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

import java.awt.EventQueue;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.border.Border;

import com.mlt.desktop.AWT;
import com.mlt.desktop.layout.Alignment;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.HorizontalFlowLayout;
import com.mlt.desktop.layout.Insets;

/**
 * A status bar displays labels and progress bars, with an optional label, accessed by key
 *
 * @author Miquel Sas
 */
public class StatusBar extends Pane {

	/**
	 * A label control.
	 */
	class LabelControl extends GridBagPane {

		/** Label to show the text. */
		Label label;

		/**
		 * Constructor.
		 * 
		 * @param name The component name.
		 */
		LabelControl(String name) {
			super();
			setName(name);
			setOpaque(StatusBar.this.isOpaque());
			setBackground(StatusBar.this.getBackground());

			label = new Label();
			label.setFont(StatusBar.this.getFont());
			add(label, new Constraints(Anchor.RIGHT, Fill.NONE, 0, 0, new Insets(0, 0, 0, 0)));
		}
	}

	/**
	 * A progress control.
	 */
	class ProgressControl extends GridBagPane {

		/** Label to show the text. */
		Label label;
		/** The progress bar to show the progress. */
		ProgressBar progress;

		/**
		 * Constructor.
		 * 
		 * @param name The component name.
		 */
		ProgressControl(String name) {
			super();
			setName(name);
			setOpaque(StatusBar.this.isOpaque());
			setBackground(StatusBar.this.getBackground());

			label = new Label();
			label.setFont(StatusBar.this.getFont());
			add(label, new Constraints(Anchor.RIGHT, Fill.NONE, 0, 0, new Insets(0, 0, 0, 5)));

			progress = new ProgressBar();
			progress.setFont(new Font(Font.DIALOG, Font.BOLD, 10));
			progress.setStringPainted(progressStringPainted);
			progress.setOpaque(StatusBar.this.isOpaque());
			progress.setPreferredSize(progress.getPreferredSize());
			progress.setMinimumSize(progress.getPreferredSize());
			if (progressBorder != null) {
				progress.setBorder(progressBorder);
			}
			add(progress, new Constraints(Anchor.RIGHT, Fill.HORIZONTAL, 1, 0, new Insets(0, 0, 0, 0)));
		}
	}

	/** List of components, instances of <em>LabelPane</em> or <em>ProgressPane</em>. */
	private List<Pane> components = new ArrayList<>();
	/** Progress string painted. */
	private boolean progressStringPainted = true;
	/** Progress bar border. */
	private Border progressBorder;

	/**
	 * Constructor.
	 */
	public StatusBar() {
		this(new Insets(5, 5, 5, 5));
	}

	/**
	 * Constructor.
	 * 
	 * @param statusBarInsets Status bar flow insets.
	 */
	public StatusBar(Insets statusBarInsets) {
		super();
		setLayout(new HorizontalFlowLayout(statusBarInsets));
		setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
	}

	/**
	 * Clear all components.
	 */
	public void clearStatusBar() {
		components.clear();
		layoutComponents();
	}

	/**
	 * Return the label with the given key or null if not exist.
	 * 
	 * @param key The key of the label.
	 * @return The label or null.
	 */
	private LabelControl getLabel(String key) {
		for (int i = 0; i < components.size(); i++) {
			Pane component = components.get(i);
			if (component.getName().equals(key)) {
				if (component instanceof LabelControl) {
					return (LabelControl) component;
				}
				throw new IllegalArgumentException(key + " is not a label");
			}
		}
		LabelControl label = new LabelControl(key);
		components.add(0, label);
		layoutComponents();
		return label;
	}

	/**
	 * Override to ensure a valid height when the are no components.
	 */
	@Override
	public Dimension getPreferredSize() {
		if (!isEmpty()) {
			return super.getPreferredSize();
		}
		Label label = new Label();
		label.setFont(getFont());
		double height = Label.getPreferredSize(label).getHeight();
		Insets borderInsets = new Insets(0, 0, 0, 0);
		if (getBorder() != null) {
			borderInsets = AWT.fromAWT(getBorder().getBorderInsets(getComponent()));
			height += borderInsets.getTop() + borderInsets.getBottom();
		}
		Insets statusInsets = ((HorizontalFlowLayout) getLayout()).getBorderInsets();
		height += statusInsets.getTop() + statusInsets.getBottom();
		return new Dimension(50, height);
	}

	/**
	 * Return the progress with the given key or null if not exist.
	 * 
	 * @param key The key of the progress.
	 * @return The progress or null.
	 */
	private ProgressControl getProgress(String key) {
		for (int i = 0; i < components.size(); i++) {
			Pane component = components.get(i);
			if (component.getName().equals(key)) {
				if (component instanceof ProgressControl) {
					return (ProgressControl) component;
				}
				throw new IllegalArgumentException(key + " is not a progress");
			}
		}
		ProgressControl progress = new ProgressControl(key);
		components.add(0, progress);
		layoutComponents();
		return progress;
	}

	/**
	 * Layout the components and repaint.
	 */
	private void layoutComponents() {
		EventQueue.invokeLater(() -> {
			removeAll();
			for (int i = 0; i < components.size(); i++) {
				Pane component = components.get(i);
				add(component);
			}
			repaint();
		});
	}

	/**
	 * Remove the label with the given key.
	 * 
	 * @param key The key of the label.
	 */
	public void removeLabel(String key) {
		LabelControl label = getLabel(key);
		if (label != null) {
			components.remove(label);
			layoutComponents();
			revalidate();
		}
	}

	/**
	 * Remove the progress with the given key.
	 * 
	 * @param key The key of the progress.
	 */
	public void removeProgress(String key) {
		ProgressControl progress = getProgress(key);
		if (progress != null) {
			components.remove(progress);
			layoutComponents();
		}
	}

	/**
	 * Set the horizontal alignment of components.
	 * 
	 * @param alignment The alignment.
	 */
	public void setHorizontalAlignment(Alignment alignment) {
		HorizontalFlowLayout layout = (HorizontalFlowLayout) getLayout();
		layout.setHorizontalAlignment(alignment);
	}

	/**
	 * Set the text to the label identified by the key. If not exists, one is created.
	 * 
	 * @param key  The key of the component.
	 * @param icon The icon.
	 * @param text The text.
	 */
	public void setLabel(String key, Icon icon, String text) {
		final LabelControl label = getLabel(key);
		EventQueue.invokeLater(() -> {
			label.label.setIcon(icon);
			label.label.setText(text);
		});
	}

	/**
	 * Set the text to the label identified by the key. If not exists, one is created.
	 * 
	 * @param key  The key of the component.
	 * @param text The text.
	 */
	public void setLabel(String key, String text) {
		setLabel(key, null, text);
	}

	/**
	 * Set the progress with the given key. If not exist, one is created.
	 * 
	 * @param key       The key of the progress.
	 * @param icon      Optional icon.
	 * @param text      Optional text.
	 * @param workDone  Work done.
	 * @param totalWork Total work.
	 */
	public void setProgress(String key, Icon icon, String text, int workDone, int totalWork) {
		final ProgressControl progress = getProgress(key);
		EventQueue.invokeLater(() -> {
			progress.label.setIcon(icon);
			progress.label.setText(text);
			progress.progress.setTotalWork(totalWork);
			progress.progress.setWorkDone(workDone);
		});
	}

	/**
	 * Set the progress with the given key. If not exist, one is created.
	 * 
	 * @param key       The key of the progress.
	 * @param workDone  Work done.
	 * @param totalWork Total work.
	 */
	public void setProgress(String key, int workDone, int totalWork) {
		setProgress(key, null, null, workDone, totalWork);
	}

	/**
	 * Set the progress with the given key. If not exist, one is created.
	 * 
	 * @param key       The key of the progress.
	 * @param text      Optional text.
	 * @param workDone  Work done.
	 * @param totalWork Total work.
	 */
	public void setProgress(String key, String text, int workDone, int totalWork) {
		setProgress(key, null, text, workDone, totalWork);
	}

	/**
	 * Set the progress as indeterminate.
	 * 
	 * @param key           The key of the progress.
	 * @param indeterminate A boolean.
	 */
	public void setProgressIndeterminate(String key, boolean indeterminate) {
		setProgressIndeterminate(key, null, indeterminate);
	}

	/**
	 * Set the progress as indeterminate.
	 * 
	 * @param key           The key of the progress.
	 * @param text          The progress text.
	 * @param indeterminate A boolean.
	 */
	public void setProgressIndeterminate(String key, String text, boolean indeterminate) {
		final ProgressControl progress = getProgress(key);
		EventQueue.invokeLater(() -> {
			if (!progress.progress.isIndeterminate()) {
				progress.progress.setTotalWork(0);
				progress.progress.setWorkDone(0);
				progress.progress.setStringPainted(false);
				progress.progress.setIndeterminate(indeterminate);
			}
			progress.label.setText(text);
		});
	}

	/**
	 * Set the progress bars borders.
	 * 
	 * @param progressBorder The border.
	 */
	public void setProgressBorder(Border progressBorder) {
		this.progressBorder = progressBorder;
	}

	/**
	 * Set the progress string painted.
	 * 
	 * @param progressStringPainted A boolean.
	 */
	public void setProgressStringPainted(boolean progressStringPainted) {
		this.progressStringPainted = progressStringPainted;
	}

}
