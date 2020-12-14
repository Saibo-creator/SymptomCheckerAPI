import okhttp3.*;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


import org.json.*;

public final class EndlessMedicalAPI {
    private static final String API_ENDPOINT ="http://api.endlessmedical.com/v1/dx";
    private static String SessionID="";
    private static final OkHttpClient client = new OkHttpClient();
    private static JSONArray diseaseDetails = null;
    private static JSONObject initFeatures = null;
    private static JSONArray symptomsQA = null;
    private static JSONArray currentSuggestedFeatures = null;
//    public static int diagn_round=0;


    public EndlessMedicalAPI() throws URISyntaxException {
        diseaseDetails=loadDiseaseDetails();
        initFeatures = loadInitFeatures();
        symptomsQA = loadSymptomsQA();
    }

    private static boolean netIsAvailable() {
        try {
            final URL url = new URL("http://www.google.com");
            final URLConnection conn = url.openConnection();
            conn.connect();
            conn.getInputStream().close();
            return true;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return false;
        }
    }

    private static Request buildPostRequest(URI url, String str){
        RequestBody body = RequestBody.create(null,str);
        return new Request.Builder()
                .url(HttpUrl.get(url))
                .post(body)
                .build();
    }

    private static Request buildGetRequest(URI url){
        return new Request.Builder().url(HttpUrl.get(url)).build();
    }



    public static void initSession() throws IOException, InterruptedException {
        URI url=URI.create(API_ENDPOINT + "/InitSession");
        Request request =buildGetRequest(url);
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            String responseContent=response.body().string();
            JSONObject obj = new JSONObject(responseContent);
            SessionID = obj.getString("SessionID");
            System.out.println(responseContent);
            // {"status":"ok","SessionID":"7Pw8xlJplwzyQc7J"}
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }


    public static void acceptTerms() throws IOException, InterruptedException {
        URI url=URI.create(String.format(API_ENDPOINT + "/AcceptTermsOfUse?SessionID=%s&passphrase=I+have+read,+understood+and+I+accept+and+agree+to+comply+with+the+Terms+of+Use+of+EndlessMedicalAPI+and+Endless+Medical+services.+The+Terms+of+Use+are+available+on+endlessmedical.com", SessionID));
        RequestBody body = RequestBody.create(null,"");
        Request acceptTermsRequest = new Request.Builder()
                .url(HttpUrl.get(url))
                .post(body)
                .build();

        Response response = client.newCall(acceptTermsRequest).execute();
        System.out.println(response.body().string());
    }

    public static JSONObject loadInitFeatures() throws URISyntaxException {

        String fileName = "InitFeatures.json";

        System.out.println("getResourceAsStream : " + fileName);
        File file = getFileFromResource(fileName);
        String raw_text = getJson(file);
        initFeatures = new JSONObject(raw_text);
        return initFeatures;

    }

    public static JSONArray loadSymptomsQA() throws URISyntaxException {

        String fileName = "SymptomsQA.json";
        System.out.println("getResourceAsStream : " + fileName);
        File file = getFileFromResource(fileName);
        String raw_text = getJson(file);
        symptomsQA = new JSONArray(raw_text);
        return symptomsQA;

    }
    private static JSONArray loadDiseaseDetails() throws URISyntaxException {

        String fileName = "Diseases.json";

        System.out.println("getResourceAsStream : " + fileName);
        File file = getFileFromResource(fileName);
        String diseases = getJson(file);
        JSONArray diseaseDetails = new JSONArray(diseases);
        return diseaseDetails;
    }

    public static JSONObject retrieveQuestion(String feature){
        for (int i = 0 ; i < symptomsQA.length(); i++) {
            JSONObject obj = symptomsQA.getJSONObject(i);
            String A = obj.getString("name");
            if (A.equals(feature)) {
                return obj;
            }
        }
        throw new RuntimeException("No Question about the given feature is found");
    }

    public static void updateFeature(String key,String value) throws IOException, InterruptedException {
        URI url=URI.create(String.format(API_ENDPOINT + "/UpdateFeature?SessionID=%s&name=%s&value=%s", SessionID, key,value));
        RequestBody body = RequestBody.create(null,"");
        Request updateFeatureRequest = new Request.Builder()
                .url(HttpUrl.get(url))
                .post(body)
                .build();
        Response response = client.newCall(updateFeatureRequest).execute();
        System.out.println(response.body().string());
    }

    public static JSONArray getSuggestedFeatures_PatientProvided(int  num_disease) throws IOException, InterruptedException {
        URI url=URI.create(String.format(API_ENDPOINT + "/GetSuggestedFeatures_PatientProvided?SessionID=%s&TopDiseasesToTake=%s",SessionID,num_disease));
        Request request = new Request.Builder().url(HttpUrl.get(url)).build();
        Response response = client.newCall(request).execute();
        String responseContent=response.body().string();
        JSONObject obj = new JSONObject(responseContent);
        JSONArray SuggestedFeatures = obj.getJSONArray("SuggestedFeatures");
        return SuggestedFeatures;

    }

    public static JSONArray analyze() throws IOException, InterruptedException {
        URI url=URI.create(String.format(API_ENDPOINT + "/Analyze?SessionID=%s", SessionID));
        Request request =buildGetRequest(url);
        Response response = client.newCall(request).execute();
        String responseContent=response.body().string();
        JSONObject obj = new JSONObject(responseContent);
        JSONArray analysis = obj.getJSONArray("Diseases");
        return analysis;
    }




    public static JSONArray getSuggestedTests(int  num_disease) throws IOException, InterruptedException {
        URI url=URI.create(String.format(API_ENDPOINT + "/GetSuggestedTests?SessionID=%s&TopDiseasesToTake=%s",SessionID,num_disease));
        Request request =buildGetRequest(url);
        Response response = client.newCall(request).execute();
        String responseContent=response.body().string();
        JSONObject obj = new JSONObject(responseContent);
        JSONArray tests = obj.getJSONArray("Tests");
        return tests;
    }

    public static JSONArray getSuggestedSpecializations(int  num_disease) throws IOException, InterruptedException {
        URI url=URI.create(String.format(API_ENDPOINT + "/GetSuggestedSpecializations?SessionID=%s&TopDiseasesToTake=%s",SessionID,num_disease));
        Request request =buildGetRequest(url);
        Response response = client.newCall(request).execute();
        String responseContent=response.body().string();
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



    private static void printQuestion(String featureName){
        JSONObject question = retrieveQuestion(featureName);
        System.out.println("########### Please answer this question : ##########\n"+question.get("laytext").toString());
        JSONArray choices= (JSONArray) question.get("choices");
        printJSONArray(choices,"laytext");
        System.out.println("########### choices : ##########\n"+question.get("choices").toString());
    }

                                     // JSON IO Methods
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

    private static void printJSONArray(JSONArray analysis, String key){


        for (int i = 0; i < analysis.length(); i++)
        {
            JSONObject object = analysis.getJSONObject(i);
            System.out.println(object.get(key));
        }
    }

    // User Simulating methods

    private static String getRandomInitFeature(){

        List<String> keysAsArray = new ArrayList<>(initFeatures.keySet());
        Random r = new Random();
        String randomKey=keysAsArray.get(r.nextInt(keysAsArray.size()));
        String featureName= (String) initFeatures.get(randomKey);

        return featureName;
    }

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        if (netIsAvailable()){
            EndlessMedicalAPI endlessMedicalAPI= new EndlessMedicalAPI();
            endlessMedicalAPI.initSession();
            endlessMedicalAPI.acceptTerms();

            System.out.println("\n\n\n\n Submit inital features:age,hr");

            int iAge = new Random().nextInt(100)+18;

            endlessMedicalAPI.updateFeature("age",Integer.toString(iAge));
            int iGender = new Random().nextFloat() < 0.5f ? 2 : 3; // 2 - Male, 3 - Female
            endlessMedicalAPI.updateFeature("gender",Integer.toString(iGender));
    //        endlessMedicalAPI.updateFeature("SeverityCough","4");
            float temp = new Random().nextFloat()*4+36;
            endlessMedicalAPI.updateFeature("Temp",Float.toString(temp));

            int pulseRate = new Random().nextInt(50)+50;
            endlessMedicalAPI.updateFeature("HeartRate",Integer.toString(pulseRate));

            System.out.println("\n\n\n\n,preliminary diagnosis");

            System.out.println("########### Please pick up some symptoms : ##########\n"+endlessMedicalAPI.initFeatures.toString(4));


            for (int i=0;i<3;i++){
                System.out.println(String.format("\n\nFeature No%s",i ));
                String randomFeature = (String) endlessMedicalAPI.getRandomInitFeature();
                System.out.println(randomFeature);
                printQuestion(randomFeature);

                endlessMedicalAPI.updateFeature(randomFeature,Integer.toString(2));
            }

            System.out.println("\n\n\n\n Further Diagnosis");

            // return to init features interface
            for (int i=1;i<=3;i++) {
                System.out.println(String.format("\n\n\n\nRound%s",i ));
                endlessMedicalAPI.analyze();
                JSONArray SuggestedFeatures_PatientProvided = getSuggestedFeatures_PatientProvided(10);
                System.out.println(SuggestedFeatures_PatientProvided);
                String next_feature=SuggestedFeatures_PatientProvided.getJSONArray(0).getString(0);
                printQuestion(next_feature);
                endlessMedicalAPI.updateFeature(next_feature,Integer.toString(2));
            }

            System.out.println("\n\n\n\n Analysis Step \n\n ");

            JSONArray analysis=endlessMedicalAPI.analyze();
            //Analysis is a JSONArray composed of 10 object,e.g. {"Sepsis": "0.08117125597385819"},
            System.out.println("########### Analysis report: ##########\n"+analysis.toString(4));
            String topDiseaseName= (String) analysis.getJSONObject(0).toMap().keySet().toArray()[0];
            System.out.println("########### Top Desease : ##########\n"+topDiseaseName);

            JSONObject diseaseDetail=getDiseaseDetails(topDiseaseName);
            System.out.println("########### Desease Detail: ##########\n"+diseaseDetail.toString(4));

            JSONArray tests=getSuggestedTests(10);
            System.out.println("########### Suggested Tests : ########### \n"+tests.toString(4));

            JSONArray specializations=getSuggestedSpecializations(10);
            System.out.println("########### Suggested Specializations: ########### \n"+specializations.toString(4));

        }else{
            System.out.println("Internet is not available");
        }

    }

}
