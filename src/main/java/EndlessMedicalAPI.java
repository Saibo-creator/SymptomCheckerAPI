import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;


import org.json.*;

public final class EndlessMedicalAPI {
    private static final String API_ENDPOINT ="http://api.endlessmedical.com/v1/dx";
    private static String SessionID="";
    private static HttpClient client=null;
    private static JSONArray diseaseDetails = null;


    public EndlessMedicalAPI() throws URISyntaxException {
        client=HttpClient.newHttpClient();
        diseaseDetails=loadDiseaseDetails();
    }

    private static HttpRequest buildPostRequest(URI url,String str){
        return HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(str)).build();

    }

    private static HttpRequest buildGetRequest(URI url){
        return HttpRequest.newBuilder().uri(url).build();

    }




    public static void initSession() throws IOException, InterruptedException {
        URI url=URI.create(API_ENDPOINT + "/InitSession");
        HttpRequest request =buildGetRequest(url);
        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
        String responseContent=response.body();
        JSONObject obj = new JSONObject(responseContent);
        SessionID = obj.getString("SessionID");
        System.out.println(response.body());
        // {"status":"ok","SessionID":"7Pw8xlJplwzyQc7J"}
    }


    public static void acceptTerms() throws IOException, InterruptedException {
        URI url=URI.create(String.format(API_ENDPOINT + "/AcceptTermsOfUse?SessionID=%s&passphrase=I+have+read,+understood+and+I+accept+and+agree+to+comply+with+the+Terms+of+Use+of+EndlessMedicalAPI+and+Endless+Medical+services.+The+Terms+of+Use+are+available+on+endlessmedical.com", SessionID));
        HttpRequest acceptTermsRequest=buildPostRequest(url,"");
        HttpResponse<String> response = client.send(acceptTermsRequest,HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }

    public static void updateFeature(String key,String value) throws IOException, InterruptedException {
        URI url=URI.create(String.format(API_ENDPOINT + "/UpdateFeature?SessionID=%s&name=%s&value=%s", SessionID, key,value));
        HttpRequest updateFeatureRequest=buildPostRequest(url,"");
        HttpResponse<String> response = client.send(updateFeatureRequest,HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }

    public static JSONArray analyze() throws IOException, InterruptedException {
        URI url=URI.create(String.format(API_ENDPOINT + "/Analyze?SessionID=%s", SessionID));
        HttpRequest request =buildGetRequest(url);
        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
        String responseContent=response.body();
        JSONObject obj = new JSONObject(responseContent);
        JSONArray analysis = obj.getJSONArray("Diseases");
        return analysis;
    }


    public static void parseAnalysis(JSONArray analysis){


        for (int i = 0; i < analysis.length(); i++)
        {
//            HashMap object = analysis.getJSONObject(i).map;
//            System.out.println(object);
        }
    }

    public static JSONArray getSuggestedTests(int  num_disease) throws IOException, InterruptedException {
        URI url=URI.create(String.format(API_ENDPOINT + "/GetSuggestedTests?SessionID=%s&TopDiseasesToTake=%s",SessionID,num_disease));
        HttpRequest request =buildGetRequest(url);
        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
        String responseContent=response.body();
        JSONObject obj = new JSONObject(responseContent);
        JSONArray tests = obj.getJSONArray("Tests");
        return tests;

    }

    public static JSONArray getSuggestedSpecializations(int  num_disease) throws IOException, InterruptedException {
        URI url=URI.create(String.format(API_ENDPOINT + "/GetSuggestedSpecializations?SessionID=%s&TopDiseasesToTake=%s",SessionID,num_disease));
        HttpRequest request =buildGetRequest(url);
        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
        String responseContent=response.body();
        JSONObject obj = new JSONObject(responseContent);
        JSONArray specializations = obj.getJSONArray("SuggestedSpecializations");
        return specializations;

    }

    public static JSONObject getDiseaseDetails(String diseaseFullname){
        for (int i = 0 ; i < diseaseDetails.length(); i++) {
            JSONObject obj = diseaseDetails.getJSONObject(i);
            String A = obj.getString("text");
            if (A.equals(diseaseFullname)) {
                return obj;
            }
        }
        throw new RuntimeException("Disease Detail not Found");
    }

    private static JSONArray loadDiseaseDetails() throws URISyntaxException {

        String fileName = "Diseases.json";

        System.out.println("getResourceAsStream : " + fileName);
        File file = getFileFromResource(fileName);
        String diseases = getJson(file);
        JSONArray diseaseDetails = new JSONArray(diseases);
        return diseaseDetails;
    }

    private static String getJson(File file) {
        String jsonStr = "";
        try {
            FileReader fileReader = new FileReader(file);
            Reader reader = new InputStreamReader(new FileInputStream(file),"Utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (Exception e) {
            return null;
        }
    }
    private static File getFileFromResource(String fileName) throws URISyntaxException {

        ClassLoader classLoader = EndlessMedicalAPI.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {

            // failed if files have whitespaces or special characters
            //return new File(resource.getFile());

            return new File(resource.toURI());
        }

    }
    private static void printFile(File file) {

        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            lines.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {

        EndlessMedicalAPI endlessMedicalAPI= new EndlessMedicalAPI();
        endlessMedicalAPI.initSession();
        endlessMedicalAPI.acceptTerms();



        int iAge = new Random().nextInt(100)+18;

        endlessMedicalAPI.updateFeature("age",Integer.toString(iAge));
        int iGender = new Random().nextFloat() < 0.5f ? 2 : 3; // 2 - Male, 3 - Female
        endlessMedicalAPI.updateFeature("gender",Integer.toString(iGender));
        endlessMedicalAPI.updateFeature("SeverityCough","4");
        float temp = new Random().nextFloat()*4+36;
        endlessMedicalAPI.updateFeature("Temp",Float.toString(temp));

        JSONArray analysis=endlessMedicalAPI.analyze();
        System.out.println("########### Analysis report: ##########\n"+analysis.toString(4));
        String topDiseaseName= (String) analysis.getJSONObject(0).toMap().values().toArray()[0];

//        JSONObject diseaseDetail=getDiseaseDetails()
//        parseAnalysis(analysis);

        JSONArray tests=getSuggestedTests(10);
        System.out.println("########### Suggested Tests : ########### \n"+tests.toString(4));

        JSONArray specializations=getSuggestedSpecializations(10);
        System.out.println("########### Suggested Specializations: ########### \n"+specializations.toString(4));


    }

}
