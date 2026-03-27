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

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class CSVDataNormalizer {
    private String csvPath;
    private List<String> columnsToNormalize;
    private Map<String, List<String>> columnsWithLinkings;
    private boolean linkingsExist = false;
    private List<CSVRecord> allRecords = new ArrayList<>();
    private Map<String, Integer> headerMap = new HashMap<>();
    private final TSL log = TSL.get();
    private Map<String, Pair<Double, Double>> colsToMaxMinPairs = new HashMap<>();
    private Map<Integer, String> colNumToName = new HashMap<>();
    private int numCols = 0;
    private String[] headersInOrder;
    private String savePath;

    /**
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
        this.columnsToNormalize = new ArrayList<>();
        this.linkingsExist = true;
        readCSV();
        getAllMinMaxValues();
        this.savePath = pathToCSV;
    }

    /**
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
        try(BufferedWriter bw = Files.newBufferedWriter(Paths.get(this.savePath));
            CSVPrinter printer = new CSVPrinter(bw,
                    CSVFormat.DEFAULT.withHeader(this.headersInOrder))){
            for(CSVRecord rec : this.allRecords){
                List<String> writerCells = new ArrayList<>();
                for(int i = 0; i < this.numCols; ++i){
                    String colName = this.colNumToName.get(i);
                    if(columnsToNormalize.contains(colName)){
                        Double parsed = NumUtils.getDoubleFromStr(rec.get(colName));
                        if(parsed == null){
                            log.warn("CSVDataNormalizer.normalize: could not parse double" +
                                    " from column '" + colName + "'" +
                                    " at record " + rec.getRecordNumber() +
                                    ", raw value: '" + rec.get(colName) + "'." +
                                    " Using 0.0.");
                            parsed = 0.0;
                        }
                        double curVal = parsed;
                        Pair<Double, Double> maxMin = this.colsToMaxMinPairs.get(colName);

                        /*
                         * NOTE: Pair stores (max, min) — left()=max, right()=min.
                         * normalizeBetweenZeroOne(min, max, val) so args are intentionally
                         * reversed here. The math is correct but easy to misread.
                         * SUGGESTED FIX: Swap Pair construction to Pair(min, max) so
                         * left()=min and right()=max, making call sites self-documenting.
                         */
                        double normal = NumUtils.normalizeBetweenZeroOne(
                                maxMin.right(), maxMin.left(), curVal);
                        if(normal > 1.0){
                            log.warn(
                                    "Normalized value greater than 1.0: " + normal +
                                    " from curVal: " + curVal + " setting normal to 1."
                            );
                            normal = 1.0;
                        }
                        else if(normal < 0.0){
                            log.warn(
                                    "Normalized value less than 0.0: " + normal +
                                    " from curVal : " + curVal + " setting normal to 0."
                            );
                            normal = 0.0;
                        }
                        writerCells.add(Double.toString(normal));
                    }
                    else
                        writerCells.add(rec.get(i));
                }
                printer.printRecord(writerCells.toArray());
            }
            printer.flush();
        }
        catch(IOException e){
            log.die(e);
        }
    }

    private void getAllMinMaxValues(){
        if(this.linkingsExist){
            for(Map.Entry<String, List<String>> entry :
                    this.columnsWithLinkings.entrySet()){
                this.columnsToNormalize.add(entry.getKey());
                this.colsToMaxMinPairs.put(
                        entry.getKey(),
                        getMaxMinFromLinkedColumns(entry.getKey()));
            }
        }
        else {
            for(String col : this.columnsToNormalize)
                this.colsToMaxMinPairs.put(col, getMaxMinFromCol(col));
        }
    }

    private Pair<Double, Double> getMaxMinFromCol(String columnName) {
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        for(CSVRecord record : this.allRecords){
            Double parsed = NumUtils.getDoubleFromStr(record.get(columnName));
            if(parsed == null){
                log.warn("CSVDataNormalizer.getMaxMinFromCol: could not parse double" +
                        " from column '" + columnName + "'" +
                        " at record " + record.getRecordNumber() +
                        ", raw value: '" + record.get(columnName) + "'." +
                        " Skipping record for min/max calculation.");
                continue;
            }
            double val = parsed;
            if(val > max) max = val;
            if(val < min) min = val;
        }
        return new Pair<>(max, min);
    }

    private Pair<Double, Double> getMaxMinFromLinkedColumns(String columnName) {
        /*
         * SUSPECTED BUG: cols is the actual List stored in columnsWithLinkings, not a
         * copy. Calling cols.add(columnName) mutates the original map entry. If this
         * method is called multiple times (or columnsWithLinkings is reused), columnName
         * will accumulate as a duplicate in the list, causing redundant processing.
         * SUGGESTED FIX: Work on a copy of the list:
         *   List<String> cols = new ArrayList<>(this.columnsWithLinkings.get(columnName));
         */
        List<String> cols = this.columnsWithLinkings.get(columnName);
        cols.add(columnName);
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;

        for(String col : cols){
            Pair<Double, Double> maxMin = getMaxMinFromCol(col);
            if(maxMin.left() > max) max = maxMin.left();
            if(maxMin.right() < min) min = maxMin.right();
        }
        return new Pair<>(max, min);
    }

    private void readCSV(){
        try(CSVParser parser = new CSVParser(
                Files.newBufferedReader(Paths.get(this.csvPath)),
                CSVFormat.DEFAULT
                        .withHeader()
                        .withIgnoreHeaderCase()
                        .withTrim())){
            this.headerMap = parser.getHeaderMap();
            this.allRecords = parser.getRecords();
            reverseHeaderMap();
        }
        catch(IOException e){
            log.die(e);
        }
    }

    // NOTE: This works because there are no repeat values in the headerMap.
    // This is not a generalizable solution.
    private void reverseHeaderMap(){
        for(Map.Entry<String, Integer> entry : this.headerMap.entrySet()){
            this.colNumToName.put(entry.getValue(), entry.getKey());
            ++this.numCols;
        }
        this.headersInOrder = new String[this.numCols];
        for(int i = 0; i < this.numCols; ++i)
            this.headersInOrder[i] = this.colNumToName.get(i);
    }
}
