package com.elderresearch.commons.jri;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;

import lombok.val;

/**
 * Data structure to convert hash map of values to an R data frame. Columns are created the first time a value
 * is added with the column name. If other columns already exist, the new column is filled so that it is "even"
 * (has the same number of rows) as the existing columns.
 * 
 * @author <a href="dimeo@elderresearch.com">John Dimeo</a>
 * @since Sep 24, 2020
 */
public class RDataFrameBuilder {
    private Map<String, RColumn<?>> builders = new LinkedHashMap<>();
    private int row;
    
    public void setRow(int row) {
		this.row = row;
	}

    /**
     * Sets the value of the current row. If the column name is new, a new column will be added
     * and the value specified by @{code na} will be filled in until the current row.
     *
     * @param colName the column name
     * @param value the value
     * @param na the default value to fill a column so that it is even with other columns already added
     * before adding the new value
     */
    public void setValue(String colName, double value, double na) {
        setValue(colName, value, na, DoubleColumn::new);
    }

    /**
     * Sets the value of the current row. If the column name is new, a new column will be added
     * and the value specified by @{code na} will be filled in until the current row.
     *
     * @param colName the column name
     * @param value the value
     * @param na the default value to fill a column so that it is even with other columns already added
     * before adding the new value
     */
    public void setValue(String colName, String value, String na) {
        setValue(colName, value, na, StringColumn::new);
    }

    @SuppressWarnings("unchecked")
	private <T> void setValue(String colName, T value, T na, Supplier<RColumn<T>> newCol) {
        RColumn<T> col = (RColumn<T>) builders.computeIfAbsent(colName, $ -> newCol.get());
        while (col.size() < row) { col.add(na); }
        col.add(value);
        row = Math.max(row, col.size() - 1);
    }

    /**
     * Converts this data frame to an {@link REXP} for use in the R engine.
     *
     * @return this data frame as a {@link REXP}
     * @throws REXPMismatchException
     *             if there was a problem converting the data frame
     */
    public REXP toREXP() throws REXPMismatchException {
        val colData = new REXP[builders.size()];
        val colName = new String[builders.size()];
        int c = 0;
        for (val e : builders.entrySet()) {
            colData[c] = e.getValue().toREXP();
            colName[c] = e.getKey();
            c++;
        }
        return REXP.createDataFrame(new RList(colData, colName));
    }

    private abstract static class RColumn<T> {
    	protected List<T> list = new LinkedList<>();

        public void add(T value) { list.add(value); }
        public int size() { return list.size(); }
        public abstract REXP toREXP();
    }

    private static class DoubleColumn extends RColumn<Double> {
        @Override public REXP toREXP() { return new REXPDouble(ArrayUtils.toPrimitive(list.toArray(Double[]::new))); }
    }

    private static class StringColumn extends RColumn<String> {
    	@Override public REXP toREXP() { return new REXPString(list.toArray(String[]::new)); }   
    }
}