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
package com.mlt.mkt.data;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

import com.mlt.util.Numbers;

/**
 * Financial instrument definition.
 *
 * @author Miquel Sas
 */
public class Instrument {

	/** Instrument unique identifier or code. */
	private String id;
	/** Instrument description. */
	private String description;
	/** The value of a pip, e.g. 0.0001 for the currency pair EUR/USD. */
	private double pipValue;
	/** The scale of a pip. */
	private int pipScale;
	/** Tick value, e.g., 0.00001 for the currency pair EUR/USD. */
	private double tickValue;
	/** The tick or minimum value scale. */
	private int tickScale;
	/** The volume scale. */
	private int volumeScale;
	/** Primary currency. */
	private Currency primaryCurrency;
	/** Secondary currency. */
	private Currency secondaryCurrency;

	/**
	 * Default constructor.
	 */
	public Instrument() {
		super();
	}

	/**
	 * Constructor assigning the fields.
	 *
	 * @param id                Identifier or code.
	 * @param description       Description.
	 * @param primaryCurrency   The primary currency code.
	 * @param secondaryCurrency The secondary currency code.
	 * @param pipValue          Pip value.
	 * @param pipScale          The pip scale.
	 * @param tickValue         Tick or minimum value.
	 * @param tickScale         Tick scale.
	 * @param volumeScale       The volume scale if applicable, otherwise -1.
	 */
	public Instrument(String id, String description, Currency primaryCurrency, Currency secondaryCurrency,
	double pipValue, int pipScale, double tickValue, int tickScale, int volumeScale) {

		super();

		this.id = id;
		this.description = description;
		this.primaryCurrency = primaryCurrency;
		this.secondaryCurrency = secondaryCurrency;
		this.pipValue = pipValue;
		this.pipScale = pipScale;
		this.tickValue = tickValue;
		this.tickScale = tickScale;
		this.volumeScale = volumeScale;
	}

	/**
	 * Returns a string key that uniquely identifies this instrument.
	 *
	 * @return The string key used in maps.
	 */
	public String getKey() {
		StringBuilder b = new StringBuilder();
		b.append(getId());
		b.append(", ");
		b.append(getPipValueAsBigDecimal().toPlainString());
		b.append(", ");
		b.append(getTickValueAsBigDecimal().toPlainString());
		b.append(", ");
		b.append(getPrimaryCurrency());
		b.append("/");
		b.append(getSecondaryCurrency());
		return b.toString();
	}

	/**
	 * Returns the identifier or code.
	 *
	 * @return The identifier.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the identifier or id.
	 *
	 * @param id The identifier or id.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the description.
	 *
	 * @return The description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description The description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the primary currency.
	 *
	 * @return The primary currency.
	 */
	public Currency getPrimaryCurrency() {
		return primaryCurrency;
	}

	/**
	 * Sets the primary currency.
	 *
	 * @param primaryCurrency The primary currency.
	 */
	public void setPrimaryCurrency(Currency primaryCurrency) {
		this.primaryCurrency = primaryCurrency;
	}

	/**
	 * Returns the secondary currency.
	 *
	 * @return The primary currency.
	 */
	public Currency getSecondaryCurrency() {
		return secondaryCurrency;
	}

	/**
	 * Sets the primary currency.
	 *
	 * @param secondaryCurrency The primary currency.
	 */
	public void setSecondaryCurrency(Currency secondaryCurrency) {
		this.secondaryCurrency = secondaryCurrency;
	}

	/**
	 * Returns the pip value.
	 *
	 * @return The pip value.
	 */
	public double getPipValue() {
		return pipValue;
	}

	/**
	 * Sets the pip value.
	 *
	 * @param pipValue The pip value.
	 */
	public void setPipValue(double pipValue) {
		this.pipValue = pipValue;
	}

	/**
	 * Returns the pip scale.
	 *
	 * @return The pip scale.
	 */
	public int getPipScale() {
		return pipScale;
	}

	/**
	 * Sets the pip scale.
	 *
	 * @param pipScale The pip scale.
	 */
	public void setPipScale(int pipScale) {
		this.pipScale = pipScale;
	}

	/**
	 * Returns the tick value.
	 *
	 * @return The tick or minimum value.
	 */
	public double getTickValue() {
		return tickValue;
	}

	/**
	 * Sets the tick value.
	 *
	 * @param tickValue The tick value.
	 */
	public void setTickValue(double tickValue) {
		this.tickValue = tickValue;
	}

	/**
	 * Returns the tick or minimum value scale.
	 *
	 * @return The tick or minimum value scale.
	 */
	public int getTickScale() {
		return tickScale;
	}

	/**
	 * Sets the tick or minimum value scale.
	 *
	 * @param tickScale The tick or minimum value scale.
	 */
	public void setTickScale(int tickScale) {
		this.tickScale = tickScale;
	}

	/**
	 * Returns the volume scale.
	 *
	 * @return The volume scale.
	 */
	public int getVolumeScale() {
		return volumeScale;
	}

	/**
	 * Sets the volume scale.
	 *
	 * @param volumeScale The volume scale.
	 */
	public void setVolumeScale(int volumeScale) {
		this.volumeScale = volumeScale;
	}

	/**
	 * Returns the pip value as a big decimal with scale.
	 *
	 * @return The pip value as a big decimal with scale.
	 */
	public BigDecimal getPipValueAsBigDecimal() {
		return getPipValueAsBigDecimal(getPipValue());
	}

	/**
	 * Returns the pip value as a big decimal with scale.
	 *
	 * @param pipValue The pip value.
	 * @return The pip value as a big decimal with scale.
	 */
	public BigDecimal getPipValueAsBigDecimal(double pipValue) {
		return Numbers.getBigDecimal(pipValue, getPipScale());
	}

	/**
	 * Returns the tick value as a big decimal with scale.
	 *
	 * @return The tick value as a big decimal with scale.
	 */
	public BigDecimal getTickValueAsBigDecimal() {
		return getTickValueAsBigDecimal(getTickValue());
	}

	/**
	 * Returns the tick value as a big decimal with scale.
	 *
	 * @param tickValue The tick value.
	 * @return The tick value as a big decimal with scale.
	 */
	public BigDecimal getTickValueAsBigDecimal(double tickValue) {
		return Numbers.getBigDecimal(tickValue, getTickScale());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Instrument) {
			Instrument instrument = (Instrument) obj;
			if (getId().equals(instrument.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + Objects.hashCode(this.id);
		hash = 37 * hash
		+ (int) (Double.doubleToLongBits(this.pipValue) ^ (Double.doubleToLongBits(this.pipValue) >>> 32));
		hash = 37 * hash + this.pipScale;
		hash = 37 * hash
		+ (int) (Double.doubleToLongBits(this.tickValue) ^ (Double.doubleToLongBits(this.tickValue) >>> 32));
		hash = 37 * hash + this.tickScale;
		hash = 37 * hash + Objects.hashCode(this.primaryCurrency);
		hash = 37 * hash + Objects.hashCode(this.secondaryCurrency);
		return hash;
	}

	/**
	 * Returns a string representation of the instrument.
	 *
	 * @return A string representation.
	 */
	@Override
	public String toString() {
		return getId();
	}

	/**
	 * Check if the instrument has valid primary and secondary currencies.
	 *
	 * @return A boolean.
	 */
	public boolean hasCurrency() {
		return getPrimaryCurrency() != null && getSecondaryCurrency() != null;
	}
}
