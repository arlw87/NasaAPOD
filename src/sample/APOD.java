package sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Class that represents a Nasa Astronomy of the Day (APOD) Object.
 * The object has a URL link to the APOD image, an
 * explanation of the APOD image, the APOD title and the APOD author. The APOD object is initialised with the
 * date of the request APOD and retrieve the APOD information using a HTTP request. The response can then be parsed and
 * queried on demand.
 * @author Andrew White
 * @version 1
 */

public class APOD {

    //API key required
    final static String keyGen = "j2WGbfjKwpbluNejgvW4eczfPbGrU7ue9zxiMypd";
    private LocalDate pictureDate;
    private boolean isHD;
    private URL apiCall;
    final static String baseApiCallString = "https://api.nasa.gov/planetary/apod?";
    private String response;
    private String apodAuthor;
    private String apodExplanation;
    private String apodURLImage;
    private String apodTitle;

    /**
     * Constructor for the APOD object.
     * @param date The date of the Astronomy picture of the day
     */
    public APOD(LocalDate date){
        pictureDate = date;
        this.isHD = true;
        response = "";
        apodAuthor = "";
        apodExplanation = "";
        apodURLImage = "";
        apodTitle = "";
    }


    /**
     * Creates the URL to request the APOD information. First builds a string using the base {@code baseApiCallString},
     * {@code pictureDate}, {@code isHD} and {@code keyGen}. Then creates a URL with the string URL and references
     * it to {@code apiCall}.
     */
    public void createURL(){

        //sort out the date format
        String apiDate = pictureDate.format(DateTimeFormatter.ISO_LOCAL_DATE); //format YYY-MM-DD

        //sort out HD or not
        String HD = "";
        if (isHD){
            HD = "True";
        } else {
            HD = "False";
        }

        //Build the string query
        StringBuilder qry = new StringBuilder(baseApiCallString);
        qry.append("date=");
        qry.append(apiDate);
        qry.append("&hd=");
        qry.append(HD);
        qry.append("&api_key=");
        qry.append(keyGen);
        String finalQry = qry.toString(); // convert to string for URL creation

        try{
            apiCall = new URL(finalQry);
        } catch (MalformedURLException e){
            System.out.println("Failed to create an api URL call: " + e.getMessage());
        }
    }

    /**
     * Performs an HTTP request to the {@code apiCall} address and saves the response
     */

    public void makeAPIcall(){
        BufferedReader input = null;

        try{
            //create and set up the HTTP connection
            HttpURLConnection connection = (HttpURLConnection) apiCall.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Chrome");
            connection.setConnectTimeout(5000); //timeout 5 seconds

            int responseCode = connection.getResponseCode();
            response = ""; //clear response for new Http Request
            //get the input stream and wrap it to make it easier to read
            input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            //200 is a good response
            if (responseCode == 200){
                System.out.println("APOD has been received correctly");
                //read inputStream from connection into string
                while (input.ready()){
                    response = input.readLine();
                }
            } else {
                System.out.println("There has been an error retrieiving the API call");
            }

        } catch (IOException e){
            System.out.println("IOException when creating connection to the NASA APOD API: " + e.getMessage());
        } finally {
            try{
                input.close();
            } catch (IOException e){
                System.out.println("There has been a problem closing the Input Stream Reader: " + e.getMessage());
            }
        }
    }

    /**
     * @throws VideoException
     * Take the http response and extract the key information
     */

    public void parseResponse() throws VideoException{
        if (response.equals("")){
            System.out.println("No response to parse");
        } else {

            //for debugging
            System.out.println(response);

            //use regex expression to extract information from the http response
            //some explanation have " in text so have modified regex to match
            String regExpHDurl = "(\"copyright\")?:\"(.+?)\".*\"explanation\":(.+?)\"hdurl\":\"(.+?)\".*\"title\":\"(.+?)\"";
            //String regExpHDurl = "(\"copyright\")?:\"(.+?)\".*\"explanation\":\"(.+?)\".*\"hdurl\":\"(.+?)\".*\"title\":\"(.+?)\"";
            Pattern p1 = Pattern.compile(regExpHDurl);
            Matcher m1 = p1.matcher(response);
            while(m1.find()){
                apodAuthor = m1.group(2);
                apodExplanation = m1.group(3);
                apodURLImage = m1.group(4);
                apodTitle = m1.group(5);
            }

            //if the APOD is a video throw an error
            //if it is a video there will be no apodURLImage
            if (apodURLImage.equals(""))
            {
                throw new VideoException();
            }

            //with new modifiied regex to account for " in explanation text, now have to remove the " from the
            //start and end of the explanation from response
            String temp = apodExplanation;
            apodExplanation = temp.substring(1, temp.length()-2);

            //helps to remove some addtional text that appears at the end of the explanations
            String explanation = "";
            String regex = "    ";

            if (apodExplanation.indexOf(regex) > 0){
                explanation = apodExplanation.substring(0, apodExplanation.indexOf(regex));
                apodExplanation = explanation;
            }


        }
    }

    /**
     * @return The author of the APOD image, if there is one
     */
    public String getApodAuthor() {
        return apodAuthor;
    }

    /**
     * @return A string with the APOD image explanation
     */

    public String getApodExplanation() {
        return apodExplanation;
    }

    /**
     * @return The string of the address of the APOD image
     */

    public String getApodURLImage() {
        return apodURLImage;
    }

    /**
     * @return The title of the APOD image
     */

    public String getApodTitle() {
        return apodTitle;
    }

    /**
     * Creates a new URL object that uses the address of the APOD image
     * @return a new URL object
     */
    public URL getApodURL(){
        try{
            return new URL(getApodURLImage());
        } catch (MalformedURLException e){
            System.out.println("IN APOD OBJECT: " + e.getMessage());
        }
        return null;
    }
}
