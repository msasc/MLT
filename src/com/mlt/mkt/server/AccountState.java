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
package com.mlt.mkt.server;

/**
 * Enumerates possible account states.
 *
 * @author Miquel Sas
 */
public enum AccountState {
	/**
	 * Account OK, trading is allowed.
	 */
	OK,
	/**
	 * Margin call triggered, closing positions by force. Entering new orders is
	 * prohibited.
	 */
	MARGIN_CLOSING,
	/**
	 * Margin call status. Entering new orders is prohibited.
	 */
	MARGIN_CALL,
	/**
	 * Account acting as usually, but margin call doesn't close positions.
	 */
	OK_NO_MARGIN_CALL,
	/**
	 * Disabled. Entering new orders is prohibited.
	 */
	DISABLED,
	/**
	 * Account is blocked. Entering new orders is prohibited.
	 */
	BLOCKED;
}
