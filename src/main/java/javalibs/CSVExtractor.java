package javalibs;
/**
 * Copyright (javalibs.c) 2018 Sean Grimes. All rights reserved.
 * @author Sean Grimes, sean@seanpgrimes.com
 * @since 1/1/19
 * License: MIT License
 */

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings("DeprecatedIsStillUsed")
public class CSVExtractor {
    private final TSL log = TSL.get();
    private final Logic logic = Logic.get();
    private String inCSV;
    private String outCSV;
    private List<String> extractionCols;
    private String[] orderedExtractionCols;
    private Map<String, Integer> rawHeaders;
    private int numInputCols = 0;
    private List<CSVRecord> inRecords = new ArrayList<>();
    private Map<Integer, String> orderedHeadersMap = new HashMap<>();
    private String[] headersInOrder;

    /**
     * C'tor used when extracting specific columns from a csv file and writing them to
     * a new CSV file
     * @param inPath File to read from
     * @param outPath File to write to
     * @param colHeaders Headers to be extracted
     */
    public CSVExtractor(String inPath, String outPath, List<String> colHeaders){
        this.inCSV = inPath;
        this.outCSV = outPath;
        this.extractionCols = colHeaders;
        readCSV();
        orderExtractionHeaders(this.rawHeaders);
    }

    /**
     * C'tor used to read in a CSV, get records, get headers, etc... but no writing
     * @param inCSV The CSV file to read
     */
    public CSVExtractor(String inCSV) {
        this.inCSV = inCSV;
        this.outCSV = null;
        this.extractionCols = null;
        readCSV();
    }

    /**
     * Writes the CSV to the path specified in the c'tor.
     * @return The absolute path to the output CSV file
     */
    public String writeCSV(){
        logic.require(this.outCSV != null, "Cannot write, null output file");
        try(BufferedWriter bw = Files.newBufferedWriter(Paths.get(this.outCSV));
            CSVPrinter printer = new CSVPrinter(bw,
                    CSVFormat.DEFAULT.withHeader(this.orderedExtractionCols))){
            for(CSVRecord rec : this.inRecords){
                List<String> writerCells = new ArrayList<>();
                for(String col : this.headersInOrder){
                    if(!this.extractionCols.contains(col)) continue;
                    try{
                        writerCells.add(rec.get(col));
                    }
                    catch(IllegalArgumentException e){
                        log.die(e, "Could not find column: " + col);
                    }
                }
                printer.printRecord(writerCells.toArray());
            }
            printer.flush();
        }
        catch(IOException e){
            log.die(e);
        }
        return new File(this.outCSV).getAbsolutePath();
    }

    /**
     * Get the ordered headers from whatever CSV file has been read
     * @return List of headers, in order as they appear in the CSV file
     */
    public List<String> getAllInputHeadersInOrder() {
        return orderAllHeaders(this.rawHeaders);
    }

    /**
     * Get the records from whatever CSV file has been read
     * @return List of CSVRecords
     */
    public List<CSVRecord> getRecords() { return this.inRecords; }

    // -------------------------------------------------------------------------
    // Deprecated static methods — use CSVUtils for new code
    // -------------------------------------------------------------------------

    /**
     * @deprecated Use {@link CSVUtils#getCSVRecords}
     */
    @Deprecated
    public static List<CSVRecord> getCSVRecords(String csvPath) {
        return CSVUtils.getCSVRecords(csvPath);
    }

    /**
     * @deprecated Use {@link CSVUtils#writeCSVRecord}
     */
    @Deprecated
    public static void writeCSVRecord(String path, CSVRecord rec, String[] headers) {
        CSVUtils.writeCSVRecord(path, rec, headers);
    }

    /**
     * @deprecated Use {@link CSVUtils#appendRecord}
     */
    @Deprecated
    public static void appendRecord(String path, List<String> row, String[] headers) {
        CSVUtils.appendRecord(path, row, headers);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void readCSV(){
        try(CSVParser parser = new CSVParser(
                Files.newBufferedReader(Paths.get(this.inCSV)),
                CSVFormat.DEFAULT
                        .withHeader()
                        .withIgnoreHeaderCase()
                        .withTrim())){
            this.rawHeaders = parser.getHeaderMap();
            this.inRecords = parser.getRecords();
        }
        catch(IOException e){
            log.die(e);
        }
    }

    private List<String> orderAllHeaders(Map<String, Integer> oooHeaders) {
        Map<Integer, String> headersMap = new HashMap<>();
        for(Map.Entry<String, Integer> entry : oooHeaders.entrySet()){
            headersMap.put(entry.getValue(), entry.getKey());
        }
        String[] orderedHeaders = new String[headersMap.size()];
        for(int i = 0; i < orderedHeaders.length; ++i){
            orderedHeaders[i] = headersMap.get(i);
        }
        return Arrays.asList(orderedHeaders);
    }

    private void orderExtractionHeaders(Map<String, Integer> oooHeaders){
        for(Map.Entry<String, Integer> entry : oooHeaders.entrySet()){
            this.orderedHeadersMap.put(entry.getValue(), entry.getKey());
            ++this.numInputCols;
        }

        this.headersInOrder = new String[this.numInputCols];
        for(int i = 0; i < this.numInputCols; ++i)
            this.headersInOrder[i] = this.orderedHeadersMap.get(i);

        // Put extraction headers in order as they appear in the CSV.
        // One would assume a user passes the columns in order. One would be wrong.
        this.orderedExtractionCols = new String[this.extractionCols.size()];
        int cnt = 0;
        for(String col : this.headersInOrder){
            if(this.extractionCols.contains(col)){
                this.orderedExtractionCols[cnt] = col;
                ++cnt;
            }
        }
    }
}
