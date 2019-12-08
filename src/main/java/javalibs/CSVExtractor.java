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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVExtractor {
    private TSL log_ = TSL.get();
    private Logic logic = Logic.get();
    private String inCSV;
    private String outCSV;
    private List<String> extractionCols;
    private String[] orderedExtractionCols;
    private int numInputCols = 0;
    private List<CSVRecord> inRecords = new ArrayList<>();
    private Map<Integer, String> orderedHeadersMap = new HashMap<>();
    private String[] headersInOrder;

    public CSVExtractor(String inPath, String outPath, List<String> colHeaders){
        this.inCSV = inPath;
        this.outCSV = outPath;
        this.extractionCols = colHeaders;
        readCSV();
    }

    /**
     * Writes the CSV to the path specified in the c'tor. Returns the absolute path to
     * the output CSV file
     * @return The absolute path to the output CSV file
     */
    public String writeCSV(){
        BufferedWriter bw = null;
        CSVPrinter printer = null;

        try{
            bw = Files.newBufferedWriter(Paths.get(this.outCSV));
            printer = new CSVPrinter(bw, CSVFormat.DEFAULT
                                    .withHeader(this.orderedExtractionCols));
        }
        catch(IOException e){
            log_.die(e);
        }

        logic.require(bw != null, "BufferedWriter cannot be null");
        logic.require(printer != null, "CSVPrinter cannot be null");

        for(CSVRecord rec: this.inRecords){
            List<String> writerCells = new ArrayList<>();
            for(String col: this.headersInOrder){
                if(!this.extractionCols.contains(col))
                    continue;

                String colVal = null;
                try{
                    colVal = rec.get(col);
                }
                catch(IllegalArgumentException e){
                    log_.err("Could not find column: " + col);
                    log_.die(e);
                }
                writerCells.add(colVal);
            }
            try {
                printer.printRecord(writerCells.toArray());
            }
            catch(IOException e){
                log_.die(e);
            }
        }
        try{
            printer.flush();
        }
        catch(IOException e){
            log_.die(e);
        }


        return new File(this.outCSV).getAbsolutePath();
    }

    /**
     * Read a CSV file and return a list of records representing each row in the CSV
     * NOTE: This does not handle anything but plain CSV files with default formatting
     * @param csvPath The path to the CSV file
     * @return The list of CSVRecord objects
     */
    public static List<CSVRecord> getCSVRecords(String csvPath) {
        CSVParser parser = null;
        List<CSVRecord> records = null;
        try{
            parser = new CSVParser(
                    Files.newBufferedReader(Paths.get(csvPath)),
                    CSVFormat.DEFAULT
                    .withHeader()
                    .withIgnoreHeaderCase()
                    .withTrim()
            );
            records = parser.getRecords();
        }
        catch(IOException e){
            TSL.get().exception(e);
        }
        return records;
    }

    /**
     * Write a single CSV record to a CSV file
     * @param path The path to save the CSV file
     * @param rec The record to be written
     * @param headers Headers for the CSV. If this value is null there will be no
     *                headers added to the CSV
     */
    public static void writeCSVRecord(String path, CSVRecord rec, String[] headers) {
        BufferedWriter bw = null;
        CSVPrinter printer = null;
        try{
            bw = Files.newBufferedWriter(Paths.get(path));
            if(headers != null)
                printer = new CSVPrinter(bw, CSVFormat.DEFAULT.withHeader(headers));
            else
                printer = new CSVPrinter(bw, CSVFormat.DEFAULT);
        }
        catch(IOException e){
            TSL.get().exception(e);
        }

        Logic.get().require(bw != null, "BufferedWriter cannot be null");
        Logic.get().require(printer != null, "CSVPrinter cannot be null");

        try {
            printer.printRecord(rec);
            printer.flush();
        }
        catch(IOException e){
            TSL.get().exception(e);
        }
    }

    private void readCSV(){
        try{
            CSVParser parser = new CSVParser(
                    Files.newBufferedReader(Paths.get(this.inCSV)),
                    CSVFormat.DEFAULT
                    .withHeader()
                    .withIgnoreHeaderCase()
                    .withTrim()
            );

            // Get all headers
            Map<String, Integer> rawHeaders = parser.getHeaderMap();

            // Store the inRecords
            this.inRecords = parser.getRecords();
            parser.close();

            orderHeaders(rawHeaders);
        }
        catch(IOException e){
            log_.die(e);
        }
    }

    private void orderHeaders(Map<String, Integer> oooHeaders){
        for(String col: oooHeaders.keySet()){
            this.orderedHeadersMap.put(oooHeaders.get(col), col);
            ++this.numInputCols;
        }

        // Put *all* headers in order
        this.headersInOrder = new String[this.numInputCols];
        for(int i = 0; i < this.numInputCols; ++i)
            this.headersInOrder[i] = this.orderedHeadersMap.get(i);

        // Put all of the extraction headers in order. One would assume a user passes
        // the columns in order as they appear in the CSV. One would be wrong.
        this.orderedExtractionCols = new String[this.extractionCols.size()];

        int cnt = 0;
        for(String col: this.headersInOrder){
            if(this.extractionCols.contains(col)) {
                this.orderedExtractionCols[cnt] = col;
                ++cnt;
            }
        }
    }
}
