package javalibs;
/**
 * Copyright (javalibs.c) 2018 Sean Grimes. All rights reserved.
 * @author Sean Grimes, sean@seanpgrimes.com
 * @since 12/20/18
 * License: MIT License
 */


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings({"unchecked", "SpellCheckingInspection", "unused", "UnusedAssignment",
        "ConstantConditions"})
public class CSVDataNormalizer {
    private String csvPath;
    private List<String> columnsToNormalize;
    private Map<String, List<String>> columnsWithLinkings;
    private boolean linkingsExist = false;
    private List<CSVRecord> allRecords = new ArrayList();
    private Map<String, Integer> headerMap = new HashMap();
    private TSL log_ = TSL.get();
    private Map<String, Pair<Double, Double>> colsToMaxMinPairs = new HashMap();
    private Map<Integer, String> colNumToName = new HashMap();
    private int numCols = 0;
    private String[] headersInOrder;
    private String savePath;

    /**
     *
     * @param pathToCSV The path to the CSV file
     * @param columns A list of column names that need to be normalized. This assumes
     *                that no columns are dependent. I.e. min and max values for each
     *                item in a column will come from the column they're already in
     */
    public CSVDataNormalizer(String pathToCSV, List<String> columns){
        this.csvPath = pathToCSV;
        this.columnsToNormalize = columns;
        readCSV();
        getAllMinMaxValues();
        this.savePath = pathToCSV;
    }

    /**
     * See above for full description
     * @param existingCSV The path to the CSV file
     * @param savePath Path to save the normalized CSV
     * @param columns A list of column names that need to be normalized. This assumes that
     *                no columns are dependent. I.e. min and max values for each item
     *                in a column will come from the column they're already in
     */
    public CSVDataNormalizer(String existingCSV, String savePath, List<String> columns){
        this(existingCSV, columns);
        this.savePath = savePath;
    }

    /**
     * We have a CSV file that has budget and revenue data for movies. It makes sense
     * that the max monetary value comes from either the budget or revenue column,
     * same with the min value -- otherwise, if there exists no budget that is greater
     * than revenue, a revenue value with a higher non-normalized value may be have a
     * smaller normalized value.
     * @param pathToCSV The path to the CSV file
     * @param columnsWithLinkings A map of column names that map to a list of columns that
     *                            are linked together when determining max and min values
     *                            for normalization
     */
    public CSVDataNormalizer(
            String pathToCSV, Map<String, List<String>> columnsWithLinkings){
        this.csvPath = pathToCSV;
        this.columnsWithLinkings = columnsWithLinkings;
        this.columnsToNormalize = new ArrayList();
        this.linkingsExist = true;
        readCSV();
        getAllMinMaxValues();
        this.savePath = pathToCSV;
    }

    /**
     * See above for full description.
     * @param existingCSV Path to the existing CSV file
     * @param savePath Path to save the normalized CSV
     * @param columnsWithLinkings Map of column names that map to a list of columns
     *                            that are linked together when determining max and min
     *                            values for normalization
     */
    public CSVDataNormalizer(String existingCSV, String savePath,
                             Map<String, List<String>> columnsWithLinkings) {
        this(existingCSV, columnsWithLinkings);
        this.savePath = savePath;
    }

    public void normalize(){
        BufferedWriter bw = null;
        CSVPrinter printer = null;

        try {
            bw = Files.newBufferedWriter(Paths.get(this.savePath));
            printer = new CSVPrinter(bw, CSVFormat.DEFAULT
                    .withHeader(this.headersInOrder));
        }
        catch(IOException e){
            log_.die(e);
        }

        for(CSVRecord rec : this.allRecords){
            List<String> writerCells = new ArrayList<>();
            for(int i = 0; i < this.numCols; ++i){
                String colName = this.colNumToName.get(i);
                if(columnsToNormalize.contains(colName)){
                    double curVal = NumUtils.getDoubleFromStr(rec.get(colName));
                    Pair<Double, Double> maxMin = this.colsToMaxMinPairs.get(colName);
                    double normal = NumUtils.normalizeBetweenZeroOne(maxMin.right(),
                            maxMin.left(), curVal);
                    if(normal > 1.0){
                        log_.warn(
                                "Normalized value greater than 1.0: " + normal +
                                " from curVal: " + curVal + " setting normal to 1."
                        );
                        normal = 1.0;
                    }
                    else if(normal < 0.0){
                        log_.warn(
                                "Normalized value less than 0.0: " + normal +
                                        " from curVal : " +  curVal
                                        + " setting normal to 0."
                        );
                        normal = 0.0;
                    }

                    writerCells.add(Double.toString(normal));
                }
                else
                    writerCells.add(rec.get(i));
            }
            try {
                printer.printRecord(writerCells.toArray());
            }
            catch(IOException e){
                log_.die(e);
            }
        }
        try {
            printer.flush();
        }
        catch(IOException e){
            log_.die(e);
        }
    }

    private void getAllMinMaxValues(){
        if(this.linkingsExist){
            // Go through all of the columns that need to be normalized
            for(String column : this.columnsWithLinkings.keySet()) {
                this.columnsToNormalize.add(column);
                this.colsToMaxMinPairs.put(column, getMaxMinFromLinkedColumns(column));
            }
        }
        else {
            for (String col : this.columnsToNormalize)
                this.colsToMaxMinPairs.put(col, getMaxMinFromCol(col));
        }
    }

    private Pair getMaxMinFromCol(String columnName) {
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for(CSVRecord record : this.allRecords){
            double val = NumUtils.getDoubleFromStr(record.get(columnName));
            // NOTE: Floating point errors aren't really that important here, don't waste
            // time on a proper floating point comparison
            if(val > max) max = val;
            if(val < min) min = val;
        }

        return new Pair(max, min);
    }

    private Pair getMaxMinFromLinkedColumns(String columnName) {
        List<String> cols = this.columnsWithLinkings.get(columnName);
        // Also need to look at the primary column name
        cols.add(columnName);
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        for(String col : cols){
            Pair<Double, Double> maxMin = getMaxMinFromCol(col);
            if(maxMin.left() > max) max = maxMin.left();
            if(maxMin.right() < min) min = maxMin.right();
        }

        return new Pair(max, min);
    }

    private void readCSV(){
        try {
            CSVParser parser = new CSVParser(
                    Files.newBufferedReader(Paths.get(this.csvPath)),
                    CSVFormat.DEFAULT
                    .withHeader()
                    .withIgnoreHeaderCase()
                    .withTrim()
            );

            // Get all headers in the CSV file so they can be used later when writing
            // the file
            this.headerMap = parser.getHeaderMap();

            // Add them to the records list for later use
            this.allRecords = parser.getRecords();

            parser.close();

            reverseHeaderMap();
        }
        catch(IOException e){
            log_.die(e);
        }
    }

    // NOTE: This works because I know there are no repeat values in the hashmap. This
    // is not a generalizable solution.
    private void reverseHeaderMap(){
        for(String colName : this.headerMap.keySet()) {
            this.colNumToName.put(this.headerMap.get(colName), colName);
            ++this.numCols;
        }

        this.headersInOrder = new String[this.numCols];

        for(int i = 0; i < this.numCols; ++i)
            this.headersInOrder[i] = this.colNumToName.get(i);
    }
}
