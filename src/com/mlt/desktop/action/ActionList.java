/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.mlt.desktop.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Action list. Executes sequentially a list of actions. If the performing
 * action listener is an instance of action, then the property CAN CONTINUE is
 * requested to check whether the list should continue performing the following
 * actions.
 *
 * @author Miquel Sas
 */
public class ActionList extends Action {
	
	/** List of actions to execute. */
	private List<ActionListener> actions = new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public ActionList() {
		super();
	}

	/**
	 * Add an action to the list of actions to execute.
	 * 
	 * @param actionListener The action.
	 */
	public void add(ActionListener actionListener) {
		actions.add(actionListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		for (int i = 0; i < actions.size(); i++) {
			ActionListener action = actions.get(i);
			action.actionPerformed(e);
			if (action instanceof Action) {
				Action a = (Action) action;
				if (!a.getProperties().getBoolean(CAN_CONTINUE, true)) {
					break;
				}
			}
		}
	}

}
