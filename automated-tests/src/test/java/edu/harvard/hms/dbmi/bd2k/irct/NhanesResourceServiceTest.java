
package edu.harvard.hms.dbmi.bd2k.irct;

import com.opencsv.*;


import io.restassured.response.Response;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.restassured.RestAssured.given;

@SuppressWarnings("unused")

/**
 * CheckStatusCodeNhanes.java class which check the response of End Points(PUIs),validate
 * it and counts the number of Puis under base project.  
 * @author Atul
 * @Version 1.0    BufferedWriter bw = null;
FileWriter fw = null;
 */


public class NhanesResourceServiceTest {


    private static final Logger LOGGER = Logger.getLogger(NhanesResourceServiceTest.class.getName());
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    private static List<String[]> csvData = null;
    private static int countPui;


    @SuppressWarnings("resource")


    /**
     * Retrieve the value of endpoint (baseURI) from pom.xml
     */

String baseUri = System.getProperty("path");    //Getting  the value from pom.xml
//String endpoint=System.getProperty("path");
//String baseUri="http://nhanes.hms.harvard.edu/rest/v1/resourceService/path/nhanes/Demo/questionnaire/questionnaire/alcohol use/";


    /**
     * Retrieve the value of accessToken from pom.xml and set timeout of  30000000 milliseonds for getting
     * the response.
     *
     * @throws IOException i
     */


    @Test(timeOut = 30000000)
    public void ResourceServiceCheckStatusCode() throws IOException {
        countPui = 0;
        csvData = new ArrayList<>();
        String fileName = "Nhanes_Pui_Paths_Check_Code_" + df.format(new Date()) + ".csv";
        String accessToken = System.getProperty("accessToken");
        resourceServiceStatusCodePuis(baseUri, accessToken);
        System.out.println("Test CSV");
        writeToCSV(csvData, fileName);
    }


    /**
     * Check the status code of all the puis and gets the count of number of puis
     *
     * @throws IOException
     */


    @SuppressWarnings({})

    public void resourceServiceStatusCodePuis(String puiPath, String puiAccessToken) throws IOException {
        {
            int statusCode = 0;
            String[] data = new String[4];
            data[0] = puiPath;

            try {
                statusCode = given()
                        .header("Authorization", puiAccessToken)
                        .when()
                        .get(puiPath)
                        .statusCode();

            } catch (AssertionError e) {
                LOGGER.error("Rest URI has Exception/Error", e);
            }

            data[1] = statusCode == 200 ? "PASS" : "FAIL";
            data[2] = "" + statusCode;

            if (statusCode == 200) {
                Response response = (Response) given()
                        .header("Authorization", puiAccessToken)
                        .when()
                        .get(puiPath)
                        .then()
                        .extract()
                        .response();

                List<String> pui = response
                        .getBody()
                        .jsonPath()
                        .getList("pui");

                LOGGER.info("***************PUIs in response************************      : " + pui.toString());
                LOGGER.info("***************Count of child puis*********************      : " + pui.size());
                int puiCount = pui.size();
                data[3] = "" + puiCount;
                csvData.add(data);

                countPui++;

                for (int i = 0; i < pui.size(); i++) {
                String childPuiPath = baseUri + pui.get(i);
      //    	String childPuiPath = "http://nhanes.hms.harvard.edu/rest/v1/resourceService/path" + pui.get(i);
                    LOGGER.info("-----------------------------------------------------------------------------------------------");
                    LOGGER.info("Path Unique Identifier with baseURI             :" + countPui + "  : " + childPuiPath);
                    LOGGER.info("-----------------------------------------------------------------------------------------------");
                    resourceServiceStatusCodePuis(childPuiPath, puiAccessToken);
                }
            } else {
                data[3] = "0";
                csvData.add(data);
                LOGGER.info("=========================== No Puis====================      : ");
            }
        }
    }


    public static void writeToCSV(List<String[]> data, String fileName) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(fileName));) {
            String[] header = new String[]{"PUI", "Pass_or_Fail", "Http_Status_Code", "Count"};
            writer.writeNext(header);
            writer.writeAll(data);
          //  writer.close();
        } catch (IOException e) {
            LOGGER.error("Exception Occurred : ", e);
        }
    }

}

			