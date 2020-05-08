import testsmell.AbstractSmell;
import testsmell.ResultsWriter;
import testsmell.TestFile;
import testsmell.TestSmellDetector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args == null) {
            System.out.println("Please provide the file containing the paths to the collection of test files");
            return;
        }
        if(!args[0].isEmpty()){
            File inputFile = new File(args[0]);
            if(!inputFile.exists() || inputFile.isDirectory()) {
                System.out.println("Please provide a valid file containing the paths to the collection of test files");
                return;
            }
        }


        TestSmellDetector testSmellDetector = TestSmellDetector.createTestSmellDetector();

        /*
          Read the input file and build the TestFile objects
         */
        BufferedReader in = new BufferedReader(new FileReader(args[0]));
        String str;

        String[] lineItem;
        TestFile testFile;
        List<TestFile> testFiles = new ArrayList<>();
        while ((str = in.readLine()) != null) {
            // use comma as separator
            lineItem = str.split(",");

            //check if the test file has an associated production file
            if(lineItem.length ==2){
                testFile = new TestFile(lineItem[0], lineItem[1], "");
            }
            else{
                testFile = new TestFile(lineItem[0], lineItem[1], lineItem[2]);
            }

            testFiles.add(testFile);
        }

        /*
          Initialize the output file - Create the output file and add the column names
         */
        ResultsWriter resultsWriter = ResultsWriter.createResultsWriter();
        List<String> columnNames;
        List<String> columnValues;

        columnNames = testSmellDetector.getTestSmellNames();
        columnNames.add(0, "App");
        columnNames.add(1, "Version");
        columnNames.add(2, "TestFilePath");
        columnNames.add(3, "ProductionFilePath");
        columnNames.add(4, "RelativeTestFilePath");
        columnNames.add(5, "RelativeProductionFilePath");

        resultsWriter.writeColumnName(columnNames);

        /*
          Added by Niamh Duffy - Student 40178269 at Queen's University Belfast
          Initialize second output file for test smell detection at granular level
         */
        ResultsWriter resultsWriterGranular = ResultsWriter.createResultsWriter();
        List<String> columnNamesGranular;
        List<String> columnValuesGranular;

        columnNamesGranular = testSmellDetector.getTestSmellNamesMethodLevel();
        columnNamesGranular.add(0, "App");
        columnNamesGranular.add(1, "Version");
        columnNamesGranular.add(2, "TestFilePath");
        columnNamesGranular.add(3, "ProductionFilePath");
        columnNamesGranular.add(4, "TestMethod");

        resultsWriterGranular.writeColumnName(columnNamesGranular);

        /*
          Iterate through all test files to detect smells and then write the output
        */
        TestFile tempFile;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date;
        for (TestFile file : testFiles) {
            date = new Date();
            System.out.println(dateFormat.format(date) + " Processing: "+file.getTestFilePath());
            System.out.println("Processing: "+file.getTestFilePath());

            //detect smells
            tempFile = testSmellDetector.detectSmells(file);

            //write output
            columnValues = new ArrayList<>();
            columnValues.add(file.getApp());
            columnValues.add(file.getTagName());
            columnValues.add(file.getTestFilePath());
            columnValues.add(file.getProductionFilePath());
            columnValues.add(file.getRelativeTestFilePath());
            columnValues.add(file.getRelativeProductionFilePath());
            for (AbstractSmell smell : tempFile.getTestSmells()) {
                try {
                    columnValues.add(String.valueOf(smell.getHasSmell()));
                }
                catch (NullPointerException e){
                    columnValues.add("");
                }
            }
            resultsWriter.writeLine(columnValues);
        }

        /*
          Added by Niamh Duffy - Student 40178269 at Queen's University Belfast
          Iterate through all test files to detect smells at granular level and then write the output to second output file
        */
        TestFile tempFileGranular;
        DateFormat dateFormatGranular = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date dateGranular;
        for (int i = 0; i < testFiles.size(); i++) {
            TestFile file = testFiles.get(i);
            dateGranular = new Date();
            System.out.println(dateFormatGranular.format(dateGranular) + " Processing: "+file.getTestFilePath());
            System.out.println("Processing: "+file.getTestFilePath());
            //detect smells
            tempFileGranular = testSmellDetector.detectSmellsAtMethodLevel(file);

            for (int j = 0; j < tempFileGranular.getTestSmells().get(0).getSmellyElements().size(); j++){
                columnValuesGranular = new ArrayList<>();
                try {
                    columnValuesGranular.add(file.getApp());
                    columnValuesGranular.add(file.getTagName());
                    columnValuesGranular.add(file.getTestFilePath());
                    columnValuesGranular.add(file.getProductionFilePath());
                    columnValuesGranular.add(String.valueOf(tempFileGranular.getTestSmells().get(0).getSmellyElements().get(j).getElementName()));
                }
                catch (NullPointerException e) {
                    columnValuesGranular.add("");
                }
                catch (IndexOutOfBoundsException e){
                    columnValuesGranular.add("");
                }

                for (int k = 0; k < testSmellDetector.getTestSmellNames().size(); k++) {
                    try {
                        columnValuesGranular.add(String.valueOf(tempFileGranular.getTestSmells().get(k).getSmellyElements().get(j).getHasSmell()));
                    }
                    catch (NullPointerException e) {
                        columnValuesGranular.add("");
                    }
                    catch (IndexOutOfBoundsException e) {
                        columnValuesGranular.add("");
                    }
                }
                // Remove Constructor Initialization and Default Test from method-level/granular-level detection
                columnValuesGranular.remove(8);
                columnValuesGranular.remove(8);
                resultsWriterGranular.writeLine(columnValuesGranular);
            }
        }
        System.out.println("end");
    }

}
