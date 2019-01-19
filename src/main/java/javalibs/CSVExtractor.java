package javalibs;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVExtractor {
    private TSL log_ = TSL.get();
    private String inCSV;
    private String outCSV;
    private List<String> extractionCols;
    private List<CSVRecord> inRecords = new ArrayList<>();
    private Map<Integer, String> orderedHeadersMap = new HashMap<>();
    private String[] headersInOrder;

    public CSVExtractor(String inPath, String outPath, List<String> colHeaders){
        this.inCSV = inPath;
        this.outCSV = outPath;
        this.extractionCols = colHeaders;
        readCSV();
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
            log_.logAndKill(e);
        }
    }

    private void orderHeaders(Map<String, Integer> oooHeaders){
        int numCols = 0;
        for(String col: oooHeaders.keySet()){
            this.orderedHeadersMap.put(oooHeaders.get(col), col);
            ++numCols;
        }

        this.headersInOrder = new String[numCols];
        for(int i = 0; i < numCols; ++i)
            this.headersInOrder[i] = this.orderedHeadersMap.get(i);
    }
}
