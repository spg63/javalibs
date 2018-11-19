package javalibs;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CSVDataNormalizer {
    private String csvPath;
    private List<String> columns;
    private Map<String, List<String>> columnsWithLinkings;
    private boolean linkingsExist = false;
    private ArrayList<CSVRecord> allRecords = new ArrayList();
    private ArrayList<String> allHeaders = null;
    private TSL log_ = TSL.get();

    /**
     *
     * @param pathToCSV The path to the CSV file
     * @param columns A list of column names that need to be normalized. This assumes that no
     *                columns are linked. I.e. min and max values for each item in a column will
     *                come from the column they're already in
     */
    public CSVDataNormalizer(String pathToCSV, List<String> columns){
        this.csvPath = pathToCSV;
        this.columns = columns;
        readCSV();
    }

    /**
     * We have a CSV file that has budget and revenue data for movies. It makes sense that the max
     * monetary value comes from either the budget or revenue column, same with the min value
     * -- otherwise, if there exists no budget that is greater than revenue, a revenue value with
     * a higher non-normalized value may be have a smaller normalized value.
     * @param pathToCSV The path to the CSV file
     * @param columnsWithLinkings A map of column names that map to a list of columns that are
     *                            linked together when determining max and min values for
     *                            normalization
     */
    public CSVDataNormalizer(String pathToCSV, Map<String, List<String>> columnsWithLinkings){
        this.csvPath = pathToCSV;
        this.columnsWithLinkings = columnsWithLinkings;
        this.linkingsExist = true;
        readCSV();
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
     * Normalized the columns and saves to a new CSV file
     * @param outputPath The path to the location for the new file
     */
    public void normalizeAndSave(String outputPath){

    }

    private void normalizeWithLinkings(){

    }

    private void normalizeWithoutLinkings(){
        for(String col : this.columns){
            List<Double> minMax = getMaxMinFromCol(col);
            double max = minMax.get(0);
            double min = minMax.get(1);

            // Now normalize each value
            for(CSVRecord record : this.allRecords) {
                double curVal = NumUtils.getDouble(record.get(col));
                double normalizedVal = (curVal - min) / (max - min);
                if(normalizedVal > 1.0 || normalizedVal < 0.0)
                    log_.logAndKill("normalizedVal error in CSVDataNormalizer: " + normalizedVal);
            }
        }
    }

    // Why doesn't java have a Pair class?
    private List<Double> getMaxMinFromCol(String columnName) {
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for(CSVRecord record : this.allRecords){
            double val = NumUtils.getDouble(record.get(columnName));
            // NOTE: Floating point errors aren't really that important here, don't waste time on
            // a proper floating point comparison
            if(val > max) max = val;
            if(val < min) min = val;
        }

        return Arrays.asList(max, min);
    }

    private List<Double> getMaxMinFromLinkedColumns(String columnName) {
        List<String> cols = this.columnsWithLinkings.get(columnName);
        // Also need to look at the primary column name
        cols.add(columnName);
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        for(String col : cols){
            List<Double> maxMin = getMaxMinFromCol(col);
            if(maxMin.get(0) > max) max = maxMin.get(0);
            if(maxMin.get(1) < min) min = maxMin.get(1);
        }

        return Arrays.asList(max, min);
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
            this.allHeaders = new ArrayList(parser.getHeaderMap().keySet());

            // Add them to the records list for later use
            for(CSVRecord record : parser)
                allRecords.add(record);

            parser.close();
        }
        catch(IOException e){
            log_.logAndKill(e);
        }
    }
}
