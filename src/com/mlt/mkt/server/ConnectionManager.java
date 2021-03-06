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
package com.mlt.mkt.server;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface responsible to manage connections.
 *
 * @author Miquel Sas
 */
public abstract class ConnectionManager {

	/** List of connection listeners. */
	private List<ConnectionListener> listeners = new ArrayList<>();

	/**
	 * Default constructor.s
	 */
	public ConnectionManager() {
		super();
	}

	/**
	 * Add a connection listener to receive connection events.
	 *
	 * @param listener The connection listener.
	 */
	public void addListener(ConnectionListener listener) {
		listeners.add(listener);
	}

	/**
	 * Notify listeners the connection event.
	 *
	 * @param e The event.
	 */
	public void notifyConnectionEvent(ConnectionEvent e) {
		for (ConnectionListener listener : listeners) {
			listener.status(e);
		}
	}

	/**
	 * Connect to the server, using the given string and password, for the given connection type.
	 * <p>
	 * It is the responsibility of the server implementation to ask for any additional information to connect, like for
	 * instance a PIN code.
	 *
	 * @param username       The user name.
	 * @param password       The password.
	 * @param connectionType The type of connection.
	 * @throws ServerException If a server error occurs.
	 */
	public abstract void connect(String username, String password, AccountType connectionType) throws ServerException;

	/**
	 * Disconnect from the server.
	 *
	 * @throws ServerException If a server error occurs.
	 */
	public abstract void disconnect() throws ServerException;

	/**
	 * Returns the connection type of the connection or null if not connected.
	 *
	 * @return The connection type.
	 */
	public abstract AccountType getAccountType();

	/**
	 * Returns a boolean indicating if the client is correctly connected to the server.
	 *
	 * @return A boolean indicating if the client is correctly connected to the server.
	 */
	public abstract boolean isConnected();

	/**
	 * Tries to reconnect to the server using the current client information.
	 *
	 * @throws ServerException If a server error occurs.
	 */
	public abstract void reconnect() throws ServerException;
}
