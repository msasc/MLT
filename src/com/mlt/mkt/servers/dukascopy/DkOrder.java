package com.mlt.mkt.servers.dukascopy;

import java.util.ArrayList;
import java.util.List;

import com.dukascopy.api.ICloseOrder;
import com.dukascopy.api.IFillOrder;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.OfferSide;
import com.mlt.mkt.server.Order;
import com.mlt.mkt.server.OrderCommand;
import com.mlt.mkt.server.OrderState;
import com.mlt.mkt.server.ServerException;

/**
 * Dukascopy order implementation.
 *
 * @author Miquel Sas
 */
public class DkOrder implements Order {

	/** Dukascopy order. */
	private IOrder order;

	/**
	 * Constructor.
	 * 
	 * @param order The Dukascopy order interface.
	 */
	public DkOrder(IOrder order) {
		super();
		this.order = order;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws ServerException {
		try {
			order.close();
		} catch (JFException e) {
			throw new ServerException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close(double amount) throws ServerException {
		try {
			order.close(amount);
		} catch (JFException e) {
			throw new ServerException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close(double amount, double price) throws ServerException {
		try {
			order.close(amount, price);
		} catch (JFException e) {
			throw new ServerException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close(double amount, double price, double slippage) throws ServerException {
		try {
			order.close(amount, price, slippage);
		} catch (JFException e) {
			throw new ServerException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getAmount() {
		return order.getAmount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Split> getCloseHistory() {
		List<ICloseOrder> closes = order.getCloseHistory();
		List<Split> splits = new ArrayList<>();
		for (ICloseOrder close : closes) {
			Split split = new Split(close.getTime(), close.getPrice(), close.getAmount());
			splits.add(split);
		}
		return splits;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getClosePrice() {
		return order.getClosePrice();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCloseTime() {
		return order.getCloseTime();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getComment() {
		return order.getComment();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCommission() {
		return order.getCommission();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCommissionInUSD() {
		return order.getCommissionInUSD();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCreationTime() {
		return order.getCreationTime();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Split> getFillHistory() {
		List<IFillOrder> fills = order.getFillHistory();
		List<Split> splits = new ArrayList<>();
		for (IFillOrder fill : fills) {
			Split split = new Split(fill.getTime(), fill.getPrice(), fill.getAmount());
			splits.add(split);
		}
		return splits;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getFillTime() {
		return order.getFillTime();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getExpirationTime() {
		return order.getGoodTillTime();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return order.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instrument getInstrument() {
		return DkCore.fromDkInstrument(order.getInstrument());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLabel() {
		return order.getLabel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getOpenPrice() {
		return order.getOpenPrice();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OrderCommand getOrderCommand() {
		return DkCore.fromDkOrderCommand(order.getOrderCommand(), isClosed());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OrderState getOrderState() {
		return DkCore.fromDkOrderState(order.getState());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getOriginalAmount() {
		return order.getOriginalAmount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getProfitLoss() {
		return order.getProfitLossInAccountCurrency();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getProfitLossInPips() {
		return order.getProfitLossInPips();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getProfitLossInUSD() {
		return order.getProfitLossInUSD();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getRequestedAmount() {
		return order.getRequestedAmount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getStopLossPrice() {
		return order.getStopLossPrice();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfferSide getStopLossSide() {
		return DkCore.fromDkOfferSide(order.getStopLossSide());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getTakeProfitPrice() {
		return order.getTakeProfitPrice();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getTrailingStep() {
		return order.getTrailingStep();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isClosed() {
		return (order.getClosePrice() > 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLong() {
		return order.isLong();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isShort() {
		return !isLong();
	}

}
