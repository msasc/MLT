/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.mlt.desktop.control;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalBorders;

import com.mlt.desktop.AWT;
import com.mlt.desktop.Alert;
import com.mlt.desktop.event.KeyHandler;
import com.mlt.desktop.event.Mask;
import com.mlt.desktop.event.MouseHandler;
import com.mlt.desktop.icon.IconArrow;
import com.mlt.desktop.layout.Alignment;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Direction;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.desktop.layout.Orientation;
import com.mlt.util.Chrono;
import com.mlt.util.Numbers;
import com.mlt.util.Resources;

/**
 * A date picker pane inspired in the JavaFX date picker.
 *
 * @author Miquel Sas
 */
public class DatePane extends GridBagPane {

	/**
	 * Button action.
	 */
	class ActionButton extends AbstractAction {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void actionPerformed(ActionEvent e) {

			/* Next month. */
			if (buttonNextMonth.isSource(e)) {
				move(0, 1, 0, 0, 0, 0);
				return;
			}

			/* Previous month. */
			if (buttonPrevMonth.isSource(e)) {
				move(0, -1, 0, 0, 0, 0);
				return;
			}

			/* Next year. */
			if (buttonNextYear.isSource(e)) {
				move(1, 0, 0, 0, 0, 0);
				return;
			}

			/* Previous year. */
			if (buttonPrevYear.isSource(e)) {
				move(-1, 0, 0, 0, 0, 0);
				return;
			}

			/* Body button. */
			for (int r = 0; r < ROWS; r++) {
				for (int c = 0; c < COLUMNS; c++) {
					if (bodyButtons[r][c].isSource(e)) {
						int currentDay = dateTime.getDayOfMonth();
						int buttonDay = Integer.parseInt(bodyButtons[r][c].getText());
						move(0, 0, buttonDay - currentDay, 0, 0, 0);
					}
				}
			}
		}
	}

	/**
	 * Change listener.
	 */
	class ChangeHandler implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			showInfo();
		}
	}

	/**
	 * Navigation key listener.
	 */
	class KeyListener extends KeyHandler {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_F1) {
				Alert.info("F1");
				return;
			}

			int keyCode = e.getKeyCode();
			boolean ctrl = Mask.check(e, Mask.CTRL);
			boolean alt = Mask.check(e, Mask.ALT);
			boolean shift = Mask.check(e, Mask.SHIFT);

			/* Year: Ctrl-Y and Shift-Ctrl-Y */
			if (ctrl && keyCode == KeyEvent.VK_Y) {
				move((shift ? -1 : 1), 0, 0, 0, 0, 0);
				return;
			}
			/* Month: Ctrl-M and Shift-Ctrl-M */
			if (ctrl && keyCode == KeyEvent.VK_M) {
				move(0, (shift ? -1 : 1), 0, 0, 0, 0);
				return;
			}
			/* Week: Ctrl-W and Shift-Ctrl-W */
			if (ctrl && keyCode == KeyEvent.VK_W) {
				move(0, 0, (shift ? -7 : 7), 0, 0, 0);
				return;
			}
			/* Day: Ctrl-D and Shift-Ctrl-D */
			if (ctrl && keyCode == KeyEvent.VK_D) {
				move(0, 0, (shift ? -1 : 1), 0, 0, 0);
				return;
			}
			/* Hour: Alt-H and Shift-Alt-H */
			if (alt && keyCode == KeyEvent.VK_H) {
				move(0, 0, 0, (shift ? -1 : 1), 0, 0);
				return;
			}
			/* Minute: Alt-M and Shift-Alt-M */
			if (alt && keyCode == KeyEvent.VK_M) {
				if (e.isMetaDown()) {
					System.out.println("Meta");
				}
				move(0, 0, 0, 0, (shift ? -1 : 1), 0);
				return;
			}
			/* Second: Alt-S and Shift-Alt-S */
			if (alt && keyCode == KeyEvent.VK_S) {
				move(0, 0, 0, 0, 0, (shift ? -1 : 1));
				return;
			}
			
			/* Left-Right: move day. */
			if (keyCode == KeyEvent.VK_RIGHT) {
				move(0, 0, 1, 0, 0, 0);
				return;
			}
			if (keyCode == KeyEvent.VK_LEFT) {
				move(0, 0, -1, 0, 0, 0);
				return;
			}
			/* Up-Down: move week. */
			if (keyCode == KeyEvent.VK_DOWN) {
				move(0, 0, 7, 0, 0, 0);
				return;
			}
			if (keyCode == KeyEvent.VK_UP) {
				move(0, 0, -7, 0, 0, 0);
				return;
			}
		}
	}

	/**
	 * Roll-over mouse handler for buttons.
	 */
	class MouseListener extends MouseHandler {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void mouseEntered(MouseEvent e) {
			JButton button = (JButton) e.getSource();
			if (button.isEnabled()) {
				int day = Integer.valueOf(button.getText());
				if (day != dateTime.getDayOfMonth()) {
					button.setBackground(backgroundRollover);
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void mouseExited(MouseEvent e) {
			JButton button = (JButton) e.getSource();
			if (button.isEnabled()) {
				int day = Integer.valueOf(button.getText());
				if (day != dateTime.getDayOfMonth()) {
					LocalDate date = LocalDate.of(
						dateTime.getYear(),
						dateTime.getMonth(),
						day);
					if (date.getDayOfWeek() == DayOfWeek.SATURDAY ||
						date.getDayOfWeek() == DayOfWeek.SUNDAY) {
						button.setBackground(backgroundWeekend);
					} else {
						button.setBackground(backgroundColor);
					}
				}
			}
		}
	}

	/**
	 * Slider listener.
	 */
	class SliderListener implements ChangeListener {
		SliderPane slider;

		public SliderListener(SliderPane slider) {
			this.slider = slider;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			if (slider.chrono == Chrono.HOUR) {
				move(0, 0, 0, slider.slider.getValue() - dateTime.getHour(), 0, 0);
			}
			if (slider.chrono == Chrono.MINUTE) {
				move(0, 0, 0, 0, slider.slider.getValue() - dateTime.getMinute(), 0);
			}
			if (slider.chrono == Chrono.SECOND) {
				move(0, 0, 0, 0, 0, slider.slider.getValue() - dateTime.getSecond());
			}
		}
	}

	/**
	 * Time slider pane.
	 */
	class SliderPane extends GridBagPane {
		/** Chrono time. */
		Chrono chrono;
		/** Label title. */
		Label label;
		/** Slider. */
		Slider slider;

		SliderPane(Chrono chrono, int value, int minimum, int maximum) {
			super();

			this.chrono = chrono;

			label = getHeaderLabel(Resources.getText(Chrono.getKeyShort(chrono)));

			slider = new Slider(Orientation.VERTICAL);
			slider.setMinimum(minimum);
			slider.setMaximum(maximum);
			slider.setValue(value);
			slider.setInverted(true);
			SwingUtilities.replaceUIActionMap(slider.getComponent(), null);
			SwingUtilities.replaceUIInputMap(slider.getComponent(), JComponent.WHEN_FOCUSED, null);

			add(label, new Constraints(Anchor.CENTER, Fill.HORIZONTAL, 0, 0, insetsGap));
			add(slider, new Constraints(Anchor.CENTER, Fill.BOTH, 0, 2, insetsNone));
		}
	}

	/**
	 * Mouse wheel listener.
	 */
	class WheelListener implements MouseWheelListener {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {

			/* Date controls. */
			if (displayDate) {

				/* Month source. */
				Control[] monthControls = new Control[] {
					buttonPrevMonth, buttonNextMonth, labelMonth
				};
				for (Control control : monthControls) {
					if (control.isSource(e)) {
						move(0, e.getWheelRotation(), 0, 0, 0, 0);
						return;
					}
				}

				/* Year source. */
				Control[] yearControls = new Control[] {
					buttonPrevYear, buttonNextYear, labelYear
				};
				for (Control control : yearControls) {
					if (control.isSource(e)) {
						move(e.getWheelRotation(), 0, 0, 0, 0, 0);
						return;
					}
				}

				/* Day labels. */
				for (int c = 0; c < COLUMNS; c++) {
					if (dayLabels[c].isSource(e)) {
						if (e.isControlDown()) {
							move(0, 0, 7 * e.getWheelRotation(), 0, 0, 0);
						} else {
							move(0, 0, e.getWheelRotation(), 0, 0, 0);
						}
						return;
					}
				}

				/* Day buttons. */
				for (int r = 0; r < ROWS; r++) {
					for (int c = 0; c < COLUMNS; c++) {
						if (bodyButtons[r][c].isSource(e)) {
							if (e.isControlDown()) {
								move(0, 0, 7 * e.getWheelRotation(), 0, 0, 0);
							} else {
								move(0, 0, e.getWheelRotation(), 0, 0, 0);
							}
							return;
						}
					}
				}
			}

			/* Time controls. */
			if (displayTime) {

				/* Slider panes. */
				for (SliderPane s : timeSliders) {
					if (s.isSource(e)) {
						if (s.chrono == Chrono.HOUR) {
							move(0, 0, 0, e.getWheelRotation(), 0, 0);
						}
						if (s.chrono == Chrono.MINUTE) {
							move(0, 0, 0, 0, e.getWheelRotation(), 0);
						}
						if (s.chrono == Chrono.SECOND) {
							move(0, 0, 0, 0, 0, e.getWheelRotation());
						}
						s.slider.requestFocus();
					}
				}
			}
		}
	}

	/** Columns. */
	private static final int COLUMNS = 8;
	/** Gap. */
	private static final int GAP = 5;
	/** Rows. */
	private static final int ROWS = 6;

	/** Button previous month. */
	private Button buttonPrevMonth;
	/** Button next month. */
	private Button buttonNextMonth;
	/** Label month. */
	private Label labelMonth;
	/** Button previous year. */
	private Button buttonPrevYear;
	/** Button next year. */
	private Button buttonNextYear;
	/** Label year. */
	private Label labelYear;

	/** Day labels. */
	private Label[] dayLabels;
	/** Day and week number buttons. */
	private Button[][] bodyButtons;

	/** List of slider panes. */
	private List<SliderPane> timeSliders = new ArrayList<>();

	/** Default background color, used by the main pane, the labels and buttons. */
	private Color backgroundColor = new Color(240, 240, 240);
	/** Background for the selected day. */
	private Color backgroundDay = new Color(190, 210, 225);
	/** Background rollover for day numbers. */
	private Color backgroundRollover = new Color(225, 235, 250);
	/** Background for the week number. */
	private Color backgroundWeek = new Color(230, 240, 250);
	/** Background for the week end number. */
	private Color backgroundWeekend = new Color(230, 230, 230);

	/** Font for day names. */
	private Font fontNames = new Font(Font.DIALOG, Font.BOLD, 12);
	/** Font for day numbers. */
	private Font fontNumbers = new Font(Font.DIALOG, Font.BOLD, 12);
	/** Font for header labels. */
	private Font fontHeaders = new Font(Font.DIALOG, Font.BOLD, 14);
	/** Font for the info label. */
	private Font fontInfo = new Font(Font.DIALOG, Font.BOLD, 16);

	/** List of date-time change listeners. */
	private List<ChangeListener> changeListeners = new ArrayList<>();

	/** Selected date-time. */
	private LocalDateTime dateTime;

	/** Default tiime chronos displayed (hour, minute and second) */
	private List<Chrono> timeChronos = new ArrayList<>();

	/** A boolean indicating whether the date should be displayed. */
	private boolean displayDate = true;
	/** A boolean indicating whether the time should be displayed. */
	private boolean displayTime = true;

	/** The label on top that display the date-time value. */
	private Label labelInfo;

	/** Widely used insets for gap. */
	private Insets insetsGap = new Insets(GAP, GAP, GAP, GAP);
	/** Widely used insets for none. */
	private Insets insetsNone = new Insets(0, 0, 0, 0);

	/**
	 * Constructor.
	 */
	public DatePane() {
		super();
		addChangeListener(new ChangeHandler());
		layout();
		addKeyListener(new KeyListener());
	}

	/**
	 * Add a change listener.
	 * 
	 * @param listener The listener.
	 */
	public void addChangeListener(ChangeListener listener) {
		changeListeners.add(listener);
	}

	/**
	 * Returns the label.
	 *
	 * @param font The font.
	 * @param text The text.
	 * @return The label.
	 */
	private Label getBorderLabel(Font font, String text) {
		Label label = new Label();
		label.setFont(font);
		label.setBackground(backgroundColor);
		label.setHorizontalAlignment(Alignment.CENTER);
		label.setVerticalAlignment(Alignment.CENTER);
		label.setBorder(new MetalBorders.TableHeaderBorder());
		label.setIconTextGap(0);
		if (text != null) label.setText(text);
		return label;
	}

	/**
	 * Return the body cell width.
	 *
	 * @return The width.
	 */
	private int getCellWidth() {
		Label name = new Label();
		name.setFont(fontNames);
		double widthName = Label.getPreferredSize(name, "WED").getWidth() + (4 * GAP);
		Label number = new Label();
		number.setFont(fontNumbers);
		double widthNumber = Label.getPreferredSize(number, "99").getWidth() + (4 * GAP);
		return (int) Math.max(widthName, widthNumber);
	}

	/**
	 * Return the current date.
	 *
	 * @return The date.
	 */
	public LocalDate getDate() {
		return dateTime.toLocalDate();
	}

	/**
	 * Returns the day name label.
	 *
	 * @param size The size.
	 * @param text The text.
	 * @return The label.
	 */
	private Label getDayNameLabel(Dimension size, String text) {
		Label label = getBorderLabel(fontNames, text);
		label.setMinimumSize(size);
		label.setPreferredSize(size);
		if (text != null) label.setText(text);
		return label;
	}

	/**
	 * Returns the day number button.
	 *
	 * @param size The size.
	 * @return The button.
	 */
	private Button getDayNumberButton(Dimension size) {
		Button button = new Button();
		button.setFont(fontNumbers);
		button.setText("");
		button.setMinimumSize(size);
		button.setPreferredSize(size);
		button.setOpaque(true);
		button.setBackground(backgroundColor);
		button.setIconTextGap(0);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.getComponent().addMouseListener(new MouseListener());
		return button;
	}

	/**
	 * Return the arrow button.
	 *
	 * @param direction The arrow direction.
	 * @param keyStroke The key stroke to launch it.
	 * @return The button.
	 */
	private Button getHeaderButton(Direction direction, KeyStroke keyStroke) {
		IconArrow icon = new IconArrow();
		icon.setDirection(direction);
		icon.setSize(21, 21);
		icon.setMarginFactors(0.20, 0.30, 0.20, 0.30);
		Button button = new Button();
		button.setText("");
		button.setToolTipText(AWT.toString(keyStroke));
		button.setIcon(icon);
		button.setIconTextGap(0);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
		button.setOpaque(true);
		button.setBackground(backgroundColor);
		return button;
	}

	/**
	 * Return the header label.
	 *
	 * @param text The text.
	 * @return The label.
	 */
	private Label getHeaderLabel(String text) {
		Label label = new Label();
		label.setFont(fontHeaders);
		label.setText(text);
		label.setHorizontalAlignment(Alignment.CENTER);
		label.setMargin(new Insets(2, 5, 2, 5));
		label.setBackground(backgroundColor);
		label.setOpaque(true);
		return label;
	}

	/**
	 * Return the display info.
	 * 
	 * @return The display info.
	 */
	private String getInfo() {
		StringBuilder b = new StringBuilder();
		if (displayDate) {
			b.append(dateTime.toLocalDate());
		}
		if (displayTime) {
			if (b.length() > 0) {
				b.append(" ");
			}
			b.append(dateTime.toLocalTime());
		}
		return b.toString();
	}

	/**
	 * Return the preferred dimension of the month label.
	 *
	 * @return the dimension.
	 */
	private Dimension getLabelMonthSize() {
		double width = 0;
		double height = 0;
		for (int month = 1; month <= 12; month++) {
			String smonth = Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault());
			Dimension size = Label.getPreferredSize(labelMonth, smonth);
			width = Math.max(width, size.getWidth());
			height = Math.max(height, size.getHeight());
		}
		return new Dimension(width, height);
	}

	/**
	 * Return the selected time.
	 * 
	 * @return The selected time.
	 */
	public LocalTime getTime() {
		return dateTime.toLocalTime();
	}

	/**
	 * Layout components.
	 */
	private void layout() {

		/* Set this pane background. */
		setBackground(backgroundColor);

		labelInfo = getBorderLabel(fontInfo, "");
		int width = (displayDate && displayTime ? 2 : 1);
		Insets insetsInfo = new Insets(GAP, GAP, 0, GAP);
		add(labelInfo, new Constraints(Anchor.TOP, Fill.HORIZONTAL, 0, 0, width, 1, insetsInfo));

		/* Add the date pane if required. */
		if (displayDate) {
			add(layoutDate(), new Constraints(Anchor.TOP, Fill.BOTH, 0, 1, insetsNone));
			setDate();
		}

		/* Add the time pane if required. */
		if (displayTime) {
			int x = (displayDate ? 1 : 0);
			add(layoutTime(), new Constraints(Anchor.TOP, Fill.VERTICAL, x, 1, insetsNone));
		}

		/* Set listeners. */
		setListeners();

		/* Show info. */
		showInfo();
	}

	/**
	 * Layout the date in a separate pane.
	 * 
	 * @return The pane with date controls.
	 */
	private GridBagPane layoutDate() {
		GridBagPane paneDate = new GridBagPane();

		/* Initialize year-month-day if required. */
		if (dateTime == null) {
			dateTime = LocalDateTime.now();
		}

		/* Cell sizes. */
		int cellWidth = getCellWidth();
		Dimension nameSize = new Dimension(cellWidth, (int) Numbers.round(cellWidth * 0.8, 0));
		Dimension numberSize = new Dimension(cellWidth, cellWidth);

		/* One action for all buttons. */
		ActionButton action = new ActionButton();

		/* Header pane. */
		GridBagPane header = new GridBagPane();

		String month = dateTime.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
		String year = Integer.toString(dateTime.getYear());
		KeyStroke keyPrevMonth = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0);
		KeyStroke keyNextMonth = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0);
		KeyStroke keyPrevYear =
			KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_DOWN_MASK);
		KeyStroke keyNextYear = KeyStroke.getKeyStroke(
			KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_DOWN_MASK);

		buttonPrevMonth = getHeaderButton(Direction.LEFT, keyPrevMonth);
		buttonPrevMonth.setAction(action);

		labelMonth = getHeaderLabel(month);
		labelMonth.setPreferredSize(getLabelMonthSize());

		buttonNextMonth = getHeaderButton(Direction.RIGHT, keyNextMonth);
		buttonNextMonth.setAction(action);

		buttonPrevYear = getHeaderButton(Direction.LEFT, keyPrevYear);
		buttonPrevYear.setAction(action);

		labelYear = getHeaderLabel(year);

		buttonNextYear = getHeaderButton(Direction.RIGHT, keyNextYear);
		buttonNextYear.setAction(action);

		header.add(buttonPrevMonth, new Constraints(Anchor.LEFT, Fill.NONE, 0, 0, insetsGap));
		header.add(labelMonth, new Constraints(Anchor.LEFT, Fill.NONE, 1, 0, insetsGap));
		header.add(buttonNextMonth, new Constraints(Anchor.LEFT, Fill.NONE, 2, 0, insetsGap));
		header.add(new Label(), new Constraints(Anchor.CENTER, Fill.HORIZONTAL, 3, 0, insetsGap));
		header.add(buttonPrevYear, new Constraints(Anchor.CENTER, Fill.NONE, 4, 0, insetsGap));
		header.add(labelYear, new Constraints(Anchor.CENTER, Fill.NONE, 5, 0, insetsGap));
		header.add(buttonNextYear, new Constraints(Anchor.CENTER, Fill.NONE, 6, 0, insetsGap));

		header.setMinimumSize(new Dimension(50, 33));

		paneDate.add(header, new Constraints(Anchor.TOP, Fill.HORIZONTAL, 0, 0, insetsNone));

		/* Body pane (day names and numbers). */
		GridBagPane body = new GridBagPane();

		/* Day names. */
		dayLabels = new Label[COLUMNS];
		for (int c = 0; c < COLUMNS; c++) {
			String day = null;
			if (c > 0) {
				day = DayOfWeek.of(c).getDisplayName(TextStyle.SHORT, Locale.getDefault());
			}
			Label label = getDayNameLabel(nameSize, day);
			Insets insets = new Insets(GAP, (c == 0 ? GAP : 0), 0, GAP);
			body.add(label, new Constraints(Anchor.CENTER, Fill.HORIZONTAL, c, 0, insets));
			dayLabels[c] = label;
		}

		/* Week and day numbers. */
		bodyButtons = new Button[ROWS][COLUMNS];
		for (int r = 0; r < ROWS; r++) {
			for (int c = 0; c < COLUMNS; c++) {
				bodyButtons[r][c] = getDayNumberButton(numberSize);
				bodyButtons[r][c].setAction(action);
			}
		}

		for (int r = 0; r < ROWS; r++) {
			for (int c = 0; c < COLUMNS; c++) {
				Button button = bodyButtons[r][c];
				Insets insets = new Insets((r == 0 ? GAP : 0), (c == 0 ? GAP : 0), GAP, GAP);
				body.add(button, new Constraints(Anchor.CENTER, Fill.BOTH, c, r + 1, insets));
			}
		}

		paneDate.add(body, new Constraints(Anchor.TOP, Fill.BOTH, 0, 1, insetsNone));
		return paneDate;
	}

	/**
	 * Layout the date in a separate pane.
	 * 
	 * @return The pane with date controls.
	 */
	private GridBagPane layoutTime() {
		GridBagPane paneTime = new GridBagPane();

		/* Initialize the time if required. */
		if (dateTime == null) {
			dateTime = LocalDateTime.now();
		}

		/* Set time chronos if not done. */
		if (timeChronos.isEmpty()) {
			timeChronos.add(Chrono.HOUR);
			timeChronos.add(Chrono.MINUTE);
			timeChronos.add(Chrono.SECOND);
		}

		/*
		 * Iterate time chronos building a label on top with the same height than the
		 * labels of the date header, and a slideron bottom.
		 */
		timeSliders.clear();
		for (int i = 0; i < timeChronos.size(); i++) {
			Chrono chrono = timeChronos.get(i);

			/* Current, minimum and maximum values. */
			int value = dateTime.get(chrono.getTemporalField());
			int minimum = (int) chrono.getTemporalField().range().getMinimum();
			int maximum = (int) chrono.getTemporalField().range().getMaximum();

			SliderPane slider = new SliderPane(chrono, value, minimum, maximum);
			Insets insets = new Insets(0, (i == 0 ? GAP * 2 : GAP / 2), 0, GAP / 2);
			paneTime.add(slider, new Constraints(Anchor.CENTER, Fill.VERTICAL, i, 1, insets));

			timeSliders.add(slider);
		}

		return paneTime;
	}

	/**
	 * Move the date-time.
	 * 
	 * @param years   Years to add or subtract.
	 * @param months  Months to add or subtract.
	 * @param days    Days to add or subtract.
	 * @param hours   Hours to add or subtract.
	 * @param minutes Minutes to add or subtract.
	 * @param seconds Seconds to add or subtract.
	 */
	private void move(int years, int months, int days, int hours, int minutes, int seconds) {

		/* Date movement. */
		if (displayDate) {
			if (years != 0 || months != 0 || days != 0) {
				moveDate(years, months, days);
			}
		}

		/* Time movement. */
		if (displayTime) {
			if (hours != 0 || minutes != 0 || seconds != 0) {
				moveTime(hours, minutes, seconds);
			}
		}
	}

	/**
	 * Move the date.
	 * 
	 * @param years  Years to add or subtract.
	 * @param months Months to add or subtract.
	 * @param days   Days to add or subtract.
	 */
	private void moveDate(int years, int months, int days) {
		LocalDate date = LocalDate.of(
			dateTime.getYear(),
			dateTime.getMonthValue(),
			dateTime.getDayOfMonth());
		if (years > 0) {
			date = date.plusYears(years);
		} else if (years < 0) {
			date = date.minusYears(Math.abs(years));
		}
		if (months > 0) {
			date = date.plusMonths(months);
		} else if (months < 0) {
			date = date.minusMonths(Math.abs(months));
		}
		if (days > 0) {
			date = date.plusDays(days);
		} else if (days < 0) {
			date = date.minusDays(Math.abs(days));
		}
		setDate(date);
	}

	/**
	 * Move the time.
	 * 
	 * @param hours   Hours to add or subtract.
	 * @param minutes Minutes to add or subtract.
	 * @param seconds Seconds to add or subtract.
	 */
	private void moveTime(int hours, int minutes, int seconds) {
		LocalDateTime time = LocalDateTime.of(
			dateTime.getYear(),
			dateTime.getMonth(),
			dateTime.getDayOfMonth(),
			dateTime.getHour(),
			dateTime.getMinute(),
			dateTime.getSecond());
		if (hours > 0) {
			time = time.plusHours(hours);
		} else {
			time = time.minusHours(Math.abs(hours));
		}
		if (minutes > 0) {
			time = time.plusMinutes(minutes);
		} else {
			time = time.minusMinutes(Math.abs(minutes));
		}
		if (seconds > 0) {
			time = time.plusSeconds(seconds);
		} else {
			time = time.minusSeconds(Math.abs(seconds));
		}
		setDateTime(time);
	}

	/**
	 * Notify changed.
	 */
	private void notifyChanged() {
		ChangeEvent e = new ChangeEvent(this);
		changeListeners.forEach(l -> l.stateChanged(e));
	}

	/**
	 * Set the selected date reflected into the buttons and labels.
	 */
	private void setDate() {
		if (!displayDate) {
			return;
		}

		/* Working date. */
		LocalDate date = null;

		/* Set the weeks. First day always in first week. */
		date = LocalDate.of(dateTime.getYear(), dateTime.getMonth(), 1);
		for (int r = 0; r < ROWS; r++) {
			int week = date.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
			bodyButtons[r][0].setText(Integer.toString(week));
			bodyButtons[r][0].setEnabled(false);
			bodyButtons[r][0].setBackground(backgroundWeek);
			date = date.plusWeeks(1);
		}

		/* Current day row and column. */
		int currentDayRow = -1;
		int currentDayColumn = -1;

		/* Current month. */
		date = LocalDate.of(dateTime.getYear(), dateTime.getMonth(), 1);
		boolean leapYear = date.isLeapYear();
		int lastDay = date.getMonth().length(leapYear);
		int row = 0;
		for (int day = 1; day <= lastDay; day++) {
			date = LocalDate.of(dateTime.getYear(), dateTime.getMonth(), day);
			int col = date.getDayOfWeek().getValue();
			if (day > 1 && col == 1) {
				row++;
			}
			bodyButtons[row][col].setText(Integer.toString(day));
			bodyButtons[row][col].setEnabled(true);
			if (day == dateTime.getDayOfMonth()) {
				bodyButtons[row][col].setBackground(backgroundDay);
				currentDayRow = row;
				currentDayColumn = col;
			} else {
				bodyButtons[row][col].setBackground(backgroundColor);
			}
		}

		/* Next month. */
		date = LocalDate.of(dateTime.getYear(), dateTime.getMonth(), 1).plusMonths(1);
		while (true) {
			int col = date.getDayOfWeek().getValue();
			if (col == 1) {
				row++;
			}
			if (row < ROWS) {
				bodyButtons[row][col].setText(Integer.toString(date.getDayOfMonth()));
				bodyButtons[row][col].setEnabled(false);
				bodyButtons[row][col].setBackground(backgroundColor);
				date = date.plusDays(1);
				continue;
			}
			break;
		}

		/* Previous month. */
		row = 0;
		date = LocalDate.of(dateTime.getYear(), dateTime.getMonth(), 1);
		int firstDayCol = date.getDayOfWeek().getValue();
		if (firstDayCol > 1) {
			for (int col = firstDayCol - 1; col >= 1; col--) {
				date = date.minusDays(1);
				bodyButtons[row][col].setText(Integer.toString(date.getDayOfMonth()));
				bodyButtons[row][col].setEnabled(false);
				bodyButtons[row][col].setBackground(backgroundColor);
			}
		}

		/* Week end background. */
		for (int r = 0; r < ROWS; r++) {
			if (currentDayRow != r || currentDayColumn != COLUMNS - 1) {
				bodyButtons[r][COLUMNS - 1].setBackground(backgroundWeekend);
			}
			if (currentDayRow != r || currentDayColumn != COLUMNS - 2) {
				bodyButtons[r][COLUMNS - 2].setBackground(backgroundWeekend);
			}
		}

		/* Year and month. */
		String month = dateTime.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
		labelMonth.setText(month);
		String year = Integer.toString(dateTime.getYear());
		labelYear.setText(year);

		/* Notify changed. */
		notifyChanged();
	}

	/**
	 * Set the date.
	 *
	 * @param year       Year.
	 * @param month      Month.
	 * @param dayOfMonth Day of month.
	 */
	public void setDate(int year, int month, int dayOfMonth) {
		setDate(LocalDate.of(year, month, dayOfMonth));
	}

	/**
	 * Set the date.
	 *
	 * @param date The date.
	 */
	public void setDate(LocalDate date) {
		LocalTime time = (dateTime != null ? dateTime.toLocalTime() : LocalTime.now());
		dateTime = LocalDateTime.of(date, time);
		setDate();
	}

	public void setDateTime(LocalDateTime dateTime) {
		setDate(dateTime.toLocalDate());
		setTime(dateTime.toLocalTime());
	}

	/**
	 * Set the listeners to date and time controls.
	 */
	private void setListeners() {
		WheelListener wheelListener = new WheelListener();

		if (displayDate) {
			buttonNextMonth.addMouseWheelListener(wheelListener);
			buttonNextYear.addMouseWheelListener(wheelListener);
			buttonPrevMonth.addMouseWheelListener(wheelListener);
			buttonPrevYear.addMouseWheelListener(wheelListener);
			labelMonth.addMouseWheelListener(wheelListener);
			labelYear.addMouseWheelListener(wheelListener);
			for (int c = 0; c < COLUMNS; c++) {
				dayLabels[c].addMouseWheelListener(wheelListener);
			}
			for (int r = 0; r < ROWS; r++) {
				for (int c = 0; c < COLUMNS; c++) {
					bodyButtons[r][c].addMouseWheelListener(wheelListener);
				}
			}
		}

		if (displayTime) {
			for (SliderPane s : timeSliders) {
				s.slider.addChangeListener(new SliderListener(s));
				s.addMouseWheelListener(wheelListener);
			}
		}
	}

	/**
	 * Set the selected time reflected into the sliders.
	 */
	private void setTime() {
		if (!displayTime) {
			return;
		}

		/* Scan time sliders. */
		for (SliderPane s : timeSliders) {
			if (s.chrono == Chrono.HOUR) {
				s.slider.setValue(dateTime.getHour());
			}
			if (s.chrono == Chrono.MINUTE) {
				s.slider.setValue(dateTime.getMinute());
			}
			if (s.chrono == Chrono.SECOND) {
				s.slider.setValue(dateTime.getSecond());
			}
		}

		/* Notify changed. */
		notifyChanged();
	}

	/**
	 * Set the time.
	 * 
	 * @param hour   Hour.
	 * @param minute Minute.
	 * @param second Second.
	 */
	public void setTime(int hour, int minute, int second) {
		setTime(LocalTime.of(hour, minute, second));
	}

	/**
	 * Set the time.
	 * 
	 * @param time The time.
	 */
	public void setTime(LocalTime time) {
		LocalDate date = (dateTime != null ? dateTime.toLocalDate() : LocalDate.now());
		dateTime = LocalDateTime.of(date, time);
		setTime();
	}

	/**
	 * Show date-time info.
	 */
	private void showInfo() {
		labelInfo.setText(getInfo());
	}
}
