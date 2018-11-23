package javalibs;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CSVDataNormalizer {
    private String csvPath;
    private List<String> columnsToNormalize;
    private Map<String, List<String>> columnsWithLinkings;
    private boolean linkingsExist = false;
    private List<CSVRecord> allRecords = new ArrayList();
    private Map<String, Integer> headerMap = new HashMap();
    private Map<String, List<String>> normalizedValues = new HashMap();
    private TSL log_ = TSL.get();
    private Map<String, Pair<Double, Double>> colsToMaxMinPairs = new HashMap();
    private Map<Integer, String> colNumToName = new HashMap();

    /**
     *
     * @param pathToCSV The path to the CSV file
     * @param columns A list of column names that need to be normalized. This assumes that no
     *                columnsToNormalize are linked. I.e. min and max values for each item in a column will
     *                come from the column they're already in
     */
    public CSVDataNormalizer(String pathToCSV, List<String> columns){
        this.csvPath = pathToCSV;
        this.columnsToNormalize = columns;
        readCSV();
        getAllMinMaxValues();
    }

    /**
     * We have a CSV file that has budget and revenue data for movies. It makes sense that the max
     * monetary value comes from either the budget or revenue column, same with the min value
     * -- otherwise, if there exists no budget that is greater than revenue, a revenue value with
     * a higher non-normalized value may be have a smaller normalized value.
     * @param pathToCSV The path to the CSV file
     * @param columnsWithLinkings A map of column names that map to a list of columnsToNormalize that are
     *                            linked together when determining max and min values for
     *                            normalization
     */
    public CSVDataNormalizer(String pathToCSV, Map<String, List<String>> columnsWithLinkings){
        this.csvPath = pathToCSV;
        this.columnsWithLinkings = columnsWithLinkings;
        this.linkingsExist = true;
        readCSV();
        getAllMinMaxValues();
    }

    /**
     * Will perform data normalization
     */
    public void normalize(){
        if(this.linkingsExist)
            normalizeWithLinkings();
        else
            normalizeWithoutLinkings();
    }

    /**
     * Normalized the columnsToNormalize and saves to a new CSV file
     * @param outputPath The path to the location for the new file
     */
    public void normalizeAndSave(String outputPath){

    }

    private void normalizeWithLinkings(){

    }

    private void normalizeWithoutLinkings(){

        for(CSVRecord rec : this.allRecords){

        }

        
        for(String col : this.columnsToNormalize){
            Pair<Double, Double> maxMin = getMaxMinFromCol(col);
            this.colsToMaxMinPairs.put(col, maxMin);
            double max = maxMin.left();
            double min = maxMin.right();

            // Now normalize each value
            for(CSVRecord record : this.allRecords) {
                System.out.println("Record num: " + record.getRecordNumber());

                double curVal = NumUtils.getDouble(record.get(col));
                double normalizedVal = NumUtils.normalizeBetweenZeroOne(min, max, curVal);
                if(normalizedVal > 1.0){
                    log_.warn("normalizedVal greater than 1.0: " + normalizedVal + " from curVal: "
                            + curVal + " setting normalizedVal to 1.");
                    normalizedVal = 1.0;
                }
                else if(normalizedVal < 0.0){
                    log_.warn("normalizedVal less than 0: " + normalizedVal + " from curVal: "
                            + curVal + " setting normalizedVal to 0.");
                    normalizedVal = 0.0;
                }

            }
            System.exit(0);
        }
    }

    private void getAllMinMaxValues(){
        if(this.linkingsExist){
            // Go through all of the columns that need to be normalized
            for(String column : this.columnsWithLinkings.keySet())
                this.colsToMaxMinPairs.put(column, getMaxMinFromLinkedColumns(column));
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
            double val = NumUtils.getDouble(record.get(columnName));
            // NOTE: Floating point errors aren't really that important here, don't waste time on
            // a proper floating point comparison
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
            CSVParser parser = new CSVParser(Files.newBufferedReader(Paths.get(this.csvPath)),
                    CSVFormat.DEFAULT
                    .withHeader()
                    .withIgnoreHeaderCase()
                    .withTrim()
            );

            // Get all headers in the CSV file so they can be used later when writing the file
            this.headerMap = parser.getHeaderMap();

            // Add them to the records list for later use
            this.allRecords = parser.getRecords();

            parser.close();

            reverseHeaderMap();
        }
        catch(IOException e){
            log_.logAndKill(e);
        }
    }

    // NOTE: This works because I know there are no repeat values in the hashmap. This is not a
    // generalizable solution.
    private void reverseHeaderMap(){
        for(String colName : this.headerMap.keySet())
            this.colNumToName.put(this.headerMap.get(colName), colName);
    }
}
