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

package app.mlt.plaf.statistics;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mlt.db.Condition;
import com.mlt.db.Criteria;
import com.mlt.db.Field;
import com.mlt.db.Order;
import com.mlt.db.Persistor;
import com.mlt.db.Record;
import com.mlt.db.RecordIterator;
import com.mlt.db.Value;
import com.mlt.ml.data.Pattern;
import com.mlt.util.IO;

import app.mlt.plaf.DB;

/**
 * Generates files with patterns for training, test, and all the data.
 *
 * @author Miquel Sas
 */
public class TaskAveragesPatterns extends TaskAverages {

	private boolean calculated;
	private double trainingFactor;
	private Persistor persistor;
	private String aliasLabel;

	/**
	 * @param stats          The statistics.
	 * @param calculated     Calculated label flag.
	 * @param trainingFactor Training factor.
	 */
	public TaskAveragesPatterns(
		StatisticsAverages stats,
		boolean calculated,
		double trainingFactor) {

		super(stats);

		if (trainingFactor <= 0 || trainingFactor >= 1) {
			throw new IllegalArgumentException("Invalid training factor " + trainingFactor);
		}

		setId("averages-patterns");
		setTitle(stats.getLabel() + " - Generate pattern files");

		this.calculated = calculated;
		this.trainingFactor = trainingFactor;
		this.persistor = stats.getView(true, false, true, true, true).getPersistor();

		this.aliasLabel = calculated ? DB.FIELD_SOURCES_LABEL_CALC : DB.FIELD_SOURCES_LABEL_EDIT;
	}

	@Override
	protected long calculateTotalWork() throws Throwable {
		setTotalWork(persistor.count(getSelectCriteria()));
		return getTotalWork();
	}

	@Override
	protected void compute() throws Throwable {

		/* Calculate work. */
		calculateTotalWork();
		int totalWork = (int) getTotalWork();
		int workDone = 0;

		/* Build the list with indexes for training and test. */
		List<Integer> allIndexes = new ArrayList<>();
		for (int i = 0; i < totalWork; i++) {
			allIndexes.add(i);
		}
		List<Integer> trainIndexes = new ArrayList<>();
		List<Integer> testIndexes = new ArrayList<>();
		int trainCount = (int) (20.0 * trainingFactor);
		int testCount = 20 - trainCount;
		int train = 0;
		int test = 0;
		Random random = new Random();
		while (!allIndexes.isEmpty()) {
			int index = allIndexes.remove(random.nextInt(allIndexes.size()));
			if (train == trainCount && test == testCount) {
				train = 0;
				test = 0;
			}
			if (train < trainCount) {
				trainIndexes.add(index);
				train++;
			} else if (test < testCount) {
				testIndexes.add(index);
				test++;
			}
		}

		/* Files and buffers. */
		String fileName = stats.getPatternFileName(calculated, null);
		String fileNameTrain = stats.getPatternFileName(calculated, true);
		String fileNameTest = stats.getPatternFileName(calculated, false);
		File file = new File(stats.getPatternFilePath(), fileName);
		File fileTrain = new File(stats.getPatternFilePath(), fileNameTrain);
		File fileTest = new File(stats.getPatternFilePath(), fileNameTest);
		BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(file));
		BufferedOutputStream boTrain = new BufferedOutputStream(new FileOutputStream(fileTrain));
		BufferedOutputStream boTest = new BufferedOutputStream(new FileOutputStream(fileTest));

		/* Write input and output sizes, and number of patterns. */
		IO.writeInt(bo, stats.getPatternInputSize());
		IO.writeInt(bo, stats.getPatternOutputSize());
		IO.writeInt(bo, trainIndexes.size() + testIndexes.size());
		IO.writeInt(boTrain, stats.getPatternInputSize());
		IO.writeInt(boTrain, stats.getPatternOutputSize());
		IO.writeInt(boTrain, trainIndexes.size());
		IO.writeInt(boTest, stats.getPatternInputSize());
		IO.writeInt(boTest, stats.getPatternOutputSize());
		IO.writeInt(boTest, testIndexes.size());

		/* Iterate and write patterns. */
		int index = 0;
		RecordIterator iter = persistor.iterator(getSelectCriteria(), getSelectOrder());
		while (iter.hasNext()) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}
			
			Record rc = iter.next();
			
			/* Notify work done. */
			workDone++;
			update(rc.toString(DB.FIELD_BAR_TIME_FMT), workDone, totalWork);
			
			/* Pattern generation. */
			Pattern pattern = stats.getPattern(rc, calculated);
			double[] inputValues = pattern.getInputValues();
			double[] outputValues = pattern.getOutputValues();
			/* All patterns. */
			IO.writeDouble1A(bo, inputValues);
			IO.writeDouble1A(bo, outputValues);
			/* Train patterns. */
			if (trainIndexes.contains(index)) {
				IO.writeDouble1A(boTrain, inputValues);
				IO.writeDouble1A(boTrain, outputValues);
			}
			/* Test patterns. */
			if (testIndexes.contains(index)) {
				IO.writeDouble1A(boTest, inputValues);
				IO.writeDouble1A(boTest, outputValues);
			}
			/* Next index. */
			index++;
		}
		iter.close();
		bo.close();
		boTrain.close();
		boTest.close();
	}

	/**
	 * @return The first/last time with a label.
	 */
	private long getTime(boolean first) throws Throwable {

		Field ftime = persistor.getField(DB.FIELD_BAR_TIME);
		Field flabel = persistor.getField(aliasLabel);

		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldNE(flabel, new Value("")));

		Order order = new Order();
		order.add(ftime, first);

		long time = 0;
		RecordIterator iter = persistor.iterator(criteria, order);
		if (iter.hasNext()) {
			time = iter.next().getValue(DB.FIELD_BAR_TIME).getLong();
		}
		iter.close();

		return time;
	}

	private Criteria getSelectCriteria() throws Throwable {
		Field ftime = persistor.getField(DB.FIELD_BAR_TIME);
		long first = getTime(true);
		long last = getTime(false);
		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldGE(ftime, new Value(first)));
		criteria.add(Condition.fieldLE(ftime, new Value(last)));
		return criteria;
	}

	private Order getSelectOrder() {
		Field ftime = persistor.getField(DB.FIELD_BAR_TIME);
		Order order = new Order();
		order.add(ftime);
		return order;
	}
}
