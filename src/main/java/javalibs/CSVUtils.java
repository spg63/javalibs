package javalibs;
/**
 * Copyright (javalibs.c) 2018 Sean Grimes. All rights reserved.
 * @author Sean Grimes, sean@seanpgrimes.com
 * License: MIT License
 */

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Stateless CSV utility methods. Prefer these over the static methods on CSVExtractor
 * for new code.
 */
public class CSVUtils {

    private CSVUtils() {}

    /**
     * Read a CSV file and return all records.
     * NOTE: Does not handle anything but plain CSV files with default formatting.
     * @param csvPath The path to the CSV file
     * @return The list of CSVRecord objects
     */
    public static List<CSVRecord> getCSVRecords(String csvPath) {
        try(CSVParser parser = new CSVParser(
                Files.newBufferedReader(Paths.get(csvPath)),
                CSVFormat.DEFAULT
                        .withHeader()
                        .withIgnoreHeaderCase()
                        .withTrim())){
            return parser.getRecords();
        }
        catch(IOException e){
            TSL.get().exception(e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Write a single CSV record to a file.
     * @param path The path to save the CSV file
     * @param rec The record to write
     * @param headers Headers for the CSV, or null for no headers
     */
    public static void writeCSVRecord(String path, CSVRecord rec, String[] headers) {
        CSVFormat format = headers != null
                ? CSVFormat.DEFAULT.withHeader(headers)
                : CSVFormat.DEFAULT;
        try(BufferedWriter bw = Files.newBufferedWriter(Paths.get(path));
            CSVPrinter printer = new CSVPrinter(bw, format)){
            printer.printRecord(rec);
            printer.flush();
        }
        catch(IOException e){
            TSL.get().exception(e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Append a row to a CSV file, creating it if it doesn't exist.
     * Headers are only written if the file does not already exist.
     * @param path The path to the CSV file
     * @param row The row data to append
     * @param headers Headers for the CSV, or null for no headers
     */
    public static void appendRecord(String path, List<String> row, String[] headers) {
        CSVFormat csvFormat = headers != null
                ? CSVFormat.DEFAULT
                        .withHeader(headers)
                        .withSkipHeaderRecord(FileUtils.get().fexists(path))
                : CSVFormat.DEFAULT;
        try(BufferedWriter bw = Files.newBufferedWriter(
                Paths.get(path),
                StandardOpenOption.APPEND,
                StandardOpenOption.CREATE);
            CSVPrinter printer = new CSVPrinter(bw, csvFormat)){
            printer.printRecord(row);
            printer.flush();
        }
        catch(IOException e){
            TSL.get().exception(e);
            throw new UncheckedIOException(e);
        }
    }
}
