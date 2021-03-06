package com.example.symptologger;

import android.os.AsyncTask;
import android.util.Log;

import com.searchly.jestdroid.DroidClientConfig;
import com.searchly.jestdroid.JestClientFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.DeleteByQuery;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.mapping.PutMapping;

/*
 *  Copyright 2018 Remi Arshad, Noni Hua, Jason Lee, Patrick Tamm, Kaiwen Zhang
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at

 *     http://www.apache.org/licenses/LICENSE-2.0

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * ElasticSearchClient represents and handles all interactions with the ElasticSearch server. It creates
 * subclasses that represent specific objects for particular interactions (searching, adding, etc.).
 *
 * @author Remi Arshad
 */

public class ElasticSearchClient {

    private static JestClient client = null;
    private static final String server = "http://cmput301.softwareprocess.es:8080";
    private static final String index = "cmput301f18t02";


    /*
     * Initialize connection to server ...
     */
    static { // need static here since initClient is static
        initClient();
    }

    /**
     * initClient() initializes the connection to the ElasticSearch server, constructing a new Jest
     * client.
     */
    public static void initClient() {
        // Construct a new Jest client according to configuration via factory
        if (client == null) {
            JestClientFactory factory = new JestClientFactory();
            DroidClientConfig config = new DroidClientConfig.Builder(server).build();
            factory.setDroidClientConfig(config);
            client = factory.getObject();
        }
    }

    /**
     * Represents the object used to delete indices in the ElasticSearch server. Not used in prototype.
     * Takes as a parameter the string representation of the username to be deleted.
     */
    public static class DeleteIndices extends AsyncTask<String, Void, Void> { //use Void instead of void for AsyncTask as return type
        @Override
        protected Void doInBackground(String... indices) {
            for (String index : indices) {
                try {
                    JestResult result = client.execute(new DeleteIndex.Builder(index).build());
                    if (!result.isSucceeded()) {
                        Log.e("Error", "ElasticSearch was not able to delete some index.");
                    }
                } catch (Exception e) {
                    Log.i("Error", "The application failed - reason: DeleteIndices.");
                }
            }
            return null; //Void requires return, (it's not void)
        }
    }

    /**
     * Represents the object used to add a new table to the server.
     */
    public static class AddConcernsTable extends AsyncTask<String, Void, Void> { //use Void instead of void for AsyncTask as return type
        @Override
        protected Void doInBackground(String... indices) {

            String type = "Concerns";
            String source = "{\"Concerns\" : {\"properties\" : " +
                    "{\"title\": {\"type\" : \"string\", \"index\": \"not_analyzed\"}," +
                    "\"date\": {\"type\" : \"string\"}, " +
                    "\"description\": {\"type\" : \"string\"}, " +
                    "\"userName\" : {\"type\" : \"string\", \"index\": \"not_analyzed\"}" +
                    "}}}";

                try {

                    JestResult result = client.execute(new PutMapping.Builder(index, type, source).build());
                    if (!result.isSucceeded()) {
                        Log.e("Error", "ElasticSearch was not able to add table.");
                    }
                } catch (Exception e) {
                    Log.i("Error", "The application failed - reason: AddConcernsTable.");
                }
            return null; //Void requires return, (it's not void)
        }
    }

    /**
     * Represents the object used to add a new user to the server. The nested doInBackground method
     * returns Boolean.TRUE or Boolean.FALSE depending on whether or not the user was successfully added.
     * Takes as parameter the username to be added.
     *
     * @author Remi Arshad
     */
    public static class AddUser extends AsyncTask<String, Void, Boolean> { //use Void instead of void for AsyncTask as return type
        @Override
        protected Boolean doInBackground(String... record) {

            String type = "usersLogin";
            String source = String.format("{\"userID\": \"%s\"," +
                    " \"creationDate\": \"%s\", " +
                    "\"userRole\": \"%s\", " +
                    "\"email\": \"%s\", " +
                    "\"phone\": \"%s\"}",
                    record[0],
                    record[1],
                    record[2],
                    record[3],
                    record[4]);

            try {
                JestResult result = client.execute( new Index.Builder(source).index(index).type(type).build() );

                if (result.isSucceeded()) {
                    return Boolean.TRUE;
                }
                else{
                    return Boolean.FALSE;
                }
            } catch (Exception e) {
                Log.i("Error", "The application failed - reason: AddUser.");
            }
            return Boolean.FALSE;
        }
    }

    /**
     * Represents the object used to search for a user in the server. It takes as parameter the
     * username to be found.
     *
     * @author Remi Arshad
     */
    public static class SearchUser extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... search_parameters){

            String val = null;

            String type = "usersLogin";
            String query =  String.format("{\"query\": {\"match\": {\"userID\": \"%s\"}}}", search_parameters[0]);
            try {
                JestResult result = client.execute(  new Search.Builder(query).addIndex(index).addType(type).build() );

                if (result.isSucceeded()){
                    List<SourceAsObjectListMap> res = result.getSourceAsObjectList(SourceAsObjectListMap.class);
                    if (res.size() != 0){
                        val = "";
                    }
                    else{
                        //Log.e("Error","nothing found.");
                        val = "The user name was not found.";
                    }


                } else {
                    Log.e("Error","Some issues with query.");
                    val = "The was an issue with the ElasticSearch query; please try again.";
                }
            } catch (Exception e){
                Log.i("Error","Something went wrong when we tried to communicate with the elasticsearch server.");
                val = "There was an issue with communicating with the server; it may be offline. Try again.";
            }
            return val;
        }
    }

    /**
     * FetchChatLogs represents the object used to fetch the chat logs.
     *
     * @author Remi Arshad
     */
    public static class FetchChatLogs extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... search_parameters){

            String type = "chatLogs";
            String query =  String.format("{\"query\":{\"bool\":{\"must\":[{\"match\":{\"recordID\":\"%s\"}}, {\"match\":{\"participantsID\":\"%s\"}},{\"match\":{\"participantsID\":\"%s\"}}]}},\"sort\":{\"timestamp\":\"asc\"},\"size\":1000}", search_parameters[0], search_parameters[1], search_parameters[2]);
            try {
                List<SearchResult.Hit<ChatLogs,Void>> hits = client.execute(  new Search.Builder(query).addIndex(index).addType(type).build() ).getHits(ChatLogs.class);

                if (hits.size() != 0){
                    Log.d("DEBUG", "Hit size is: " + hits.size());
                    RecordCommentFragment.chatLogs.add(hits.stream()
                            .map(result -> new ChatLogs(result.source.getRecordID(), result.source.getParticipantsID(), result.source.getMessage(), result.source.getTimestamp()))
                            .collect(Collectors.toList()));
                    System.out.println("FETCHING DONE!");
                    RecordCommentFragment.updateLogsReady = true;
                    System.out.println("UPDATING VIEW DONE!");

                    try {
                        ChatManager.callViewUpdate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                }
                else{
                    return null;
                }
            } catch (Exception e){
                Log.i("Error","Something went wrong when we tried to communicate with the elasticsearch server.");
            }
            return null;
        }
    }


    /**
     * SaveChatLog represents the object used to save the chat log to the server
     *
     * @author Remi Arshad
     */
    public static class SaveChatLog extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... save_parameters){


            String type = "chatLogs";
            String source = String.format("{\"recordID\":\"%s\",\"participantsID\":[\"%s\",\"%s\"],\"message\":\"%s\",\"timestamp\":\"%s\"}", save_parameters[0], save_parameters[1], save_parameters[2], save_parameters[3], save_parameters[4]);
            System.out.println(source);

            try {
                JestResult result = client.execute( new Index.Builder(source).index(index).type(type).build() );

                if (result.isSucceeded()) {
                    System.out.println("SAVING DONE!");
                    return null;
                }
                else{
                    return null;
                }
            } catch (Exception e) {
                Log.i("Error", "The application failed - reason: AddChatLog.");
            }
            return null;
        }
    }
    
    /**
     * Represents the object used to find the largest member id.
     *
     * @author Remi Arshad
     */
    public static class SearchLargestMemberID extends AsyncTask<String, Void, Integer>{

        @Override
        protected Integer doInBackground(String... search_parameters){

            String type = "usersLogin";
            String query =  "{\"query\": {\"match_all\": {}},\"sort\": {\"memberID\": \"desc\"},\"size\": 1}";
            try {
                JestResult result = client.execute(  new Search.Builder(query).addIndex(index).addType(type).build() );

                if (result.isSucceeded()){
                    List<SourceAsObjectListMap> res = result.getSourceAsObjectList(SourceAsObjectListMap.class);
                    if (res.size() != 0){
                        return res.get(0).getMemberID();
                    }
                    else{
                        return -1;
                    }


                } else {
                    Log.e("Error","Some issues with query.");
                }
            } catch (Exception e){
                Log.i("Error","Something went wrong when we tried to communicate with the elasticsearch server.");
            }
            return -1;
        }
    }

    /**
     * GetUserRole represents the object used to find the role of a particular user on sign in.
     *
     * @author Patrick Tamm
     */
    public static class GetUserRole extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... search_parameters) {

            String type = "usersLogin";
            String query = String.format("{\"query\": {\"match\": {\"userID\": \"%s\"}}}", search_parameters[0]);
            try {
                JestResult result = client.execute(new Search.Builder(query).addIndex(index).addType(type).build());
                if (result.isSucceeded()) {
                    List<SourceAsObjectListMap> res = result.getSourceAsObjectList(SourceAsObjectListMap.class);
                    if (res.size() != 0) {
                        return res.get(0).getRole();
                    } else {
                        //Log.e("Error","nothing found.");
                        return "";
                    }
                } else {
                    Log.e("Error", "Some issues with query.");
                }
            } catch (Exception e) {
                Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server.");
            }
            return "";
        }
    }

        /**
        * Represents the object used to add a concern to ElasticSearch
         *
         * @author Patrick Tamm
        */
        public static class AddConcern extends AsyncTask<String, Void, Boolean> { //use Void instead of void for AsyncTask as return type
            @Override
            protected Boolean doInBackground(String... record) {

                String type = "Concerns";
                String source = String.format("{\"title\": \"%s\", \"date\": \"%s\", \"description\": \"%s\", \"userName\": \"%s\"}", record[0], record[1], record[2], record[3]);

                try {
                    JestResult result = client.execute(new Index.Builder(source).index(index).type(type).build());

                    if (result.isSucceeded()) {
                        //ListConcernActivity.fetchConcerns();
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
                } catch (Exception e) {
                    Log.i("Error", "The application failed - reason: AddConcern.");
                }
                return Boolean.FALSE;
            }
        }

    /**
     * Represents the object used to get concerns from the server.
     *
     * @author Patrick Tamm
     */
    public static class GetConcerns extends AsyncTask<String, Void, ArrayList<Concern>> {

            @Override
            protected ArrayList<Concern> doInBackground(String... search_parameters) {

                ArrayList<Concern> foundConcerns = new ArrayList<Concern>();
                String type = "Concerns";
                String query = String.format("{\"query\": {\"match\": {\"userName\": \"%s\"}}}", search_parameters[0]);
                try {
                    JestResult result = client.execute(new Search.Builder(query).addIndex(index).addType(type).build());
                    if (result.isSucceeded()) {
                        List<Concern> res = result.getSourceAsObjectList(Concern.class);
                        if (res.size() != 0) {
                            foundConcerns.addAll(res);
                        } else {
                            Log.e("Error", "nothing found.");
                        }
                    } else {
                        Log.e("Error", "Some issues with the GetConcerns query");
                    }
                } catch (Exception e) {
                    Log.e("Error", "Something went wrong when trying to communicate with the elasticsearch server");
                }
                return foundConcerns;
            }
        }

    /**
     * Represents the object used to add records to the server.
     *
     * @author Patrick Tamm
     */
    public static class AddRecord extends AsyncTask<String, Void, Boolean> { //use Void instead of void for AsyncTask as return type
            @Override
            protected Boolean doInBackground(String... record) {

                String type = "Records";
                String source = String.format("{\"title\": \"%s\", \"date\": \"%s\", \"concernTitle\": \"%s\", \"userName\": \"%s\"}", record[0], record[1], record[2], record[3]);

                try {
                    JestResult result = client.execute(new Index.Builder(source).index(index).type(type).build());
                    if (result.isSucceeded()) {
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
                } catch (Exception e) {
                    Log.i("Error", "The application failed - reason: AddRecord.");
                }
                return Boolean.FALSE;
            }
        }

    /**
     * GetRecords represents the object used to get records from the server.
     *
     * @author Patrick Tamm
     */
    public static class GetRecords extends AsyncTask<String, Void, ArrayList<Record>> {

            @Override
            protected ArrayList<Record> doInBackground(String... search_parameters) {

                ArrayList<Record> foundRecords = new ArrayList<Record>();
                String type = "Records";
                String query = String.format(
                        "{\"query\": {\"bool\": " +
                                "{\"must\": [" +
                                "{\"match\": {\"concernTitle\": \"%s\"}}, " +
                                "{\"match\": {\"userName\": \"%s\"}}]}}}", search_parameters[0], search_parameters[1]);
                try {
                    JestResult result = client.execute(new Search.Builder(query).addIndex(index).addType(type).build());
                    if (result.isSucceeded()) {
                        List<Record> res = result.getSourceAsObjectList(Record.class);
                        if (res.size() != 0) {
                            foundRecords.addAll(res);
                        } else {
                            Log.e("Error", "nothing found.");
                        }
                    } else {
                        Log.e("Error", "Some issues with query.");
                    }
                } catch (Exception e) {
                    Log.i("Error", "000000000Something went wrong when we tried to communicate with the elasticsearch server.");
                    return foundRecords;
                }
                return foundRecords;
            }
        }

    /**
     * DeleteRecord represents the object used to remove a record from the server.
     *
     * @author Patrick Tamm
     */
    public static class DeleteRecord extends AsyncTask<String, Void, Boolean> {
            @Override
            protected Boolean doInBackground(String... search_parameters) {

                String type = "Records";
                String query = String.format(
                        "{\"query\": {\"bool\": " +
                                "{\"must\": [" +
                                "{\"match\": {\"title\": \"%s\"}}, " +
                                "{\"match\": {\"concernTitle\": \"%s\"}}]}}}", search_parameters[0], search_parameters[1]);
                try {
                    JestResult result = client.execute(new DeleteByQuery.Builder(query).addIndex(index).addType(type).build());
                    if (result.isSucceeded()) {
                        return Boolean.TRUE;
                    } else {
                        Log.e("Error", "Some issues with DeleteRecord query.");
                    }
                } catch (Exception e) {
                    Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server.");
                }
                return Boolean.FALSE;
            }
        }

    /**
     * DeleteConcern represents the object used to remove a concern from the server.
     *
     * @author Patrick Tamm
     */
    public static class DeleteConcern extends AsyncTask<String, Void, Boolean> {
            @Override
            protected Boolean doInBackground(String... search_parameters) {
                String type = "Concerns";
                String query = String.format(
                        "{\"query\": {\"bool\": " +
                                "{\"must\": [" +
                                "{\"match\": {\"title\": \"%s\"}}, " +
                                "{\"match\": {\"userName\": \"%s\"}}]}}}", search_parameters[0], search_parameters[1]);
                try {
                    JestResult result = client.execute(new DeleteByQuery.Builder(query).addIndex(index).addType(type).build());
                    if (result.isSucceeded()) {
                        return Boolean.TRUE;
                    } else {
                        Log.e("Error", "Some issues with DeleteRecord query.");
                    }
                } catch (Exception e) {
                    Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server.");
                }
                return Boolean.FALSE;
            }
        }


    public static class AddPatientsTable extends AsyncTask<String, Void, Void> { //use Void instead of void for AsyncTask as return type
        @Override
        protected Void doInBackground(String... indices) {

            String type = "Patients";
            String source = "{\"Patients\" : {\"properties\" : " +
                    "{\"userID\": {\"type\" : \"string\", \"index\": \"not_analyzed\"}," +
                    "\"email\": {\"type\" : \"string\"}, " +
                    "\"cell\": {\"type\" : \"string\"}, " +
                    "\"cpUserName\" : {\"type\" : \"string\", \"index\": \"not_analyzed\"}," +
                    "\"created\": {\"type\" : \"date\"}," +
                    "}}}";

            try {
                JestResult result = client.execute(new PutMapping.Builder(index, type, source).build());
                if (!result.isSucceeded()) {
                    Log.e("Error", "ElasticSearch was not able to add table.");
                }
            } catch (Exception e) {
                Log.i("Error", "The application failed - reason: AddPatientsTable.");
            }
            return null; //Void requires return, (it's not void)
        }
    }

    /**
     * AddPatient represents the object used to add a patient to the server.
     *
     * @author Patrick Tamm
     */
    public static class AddPatient extends AsyncTask<String, Void, Boolean> { //use Void instead of void for AsyncTask as return type
        @Override
        protected Boolean doInBackground(String... record) {

            String type = "Patients";
            String source = String.format("{\"userID\": \"%s\", \"email\": \"%s\", \"cell\": \"%s\", \"cpUserName\": \"%s\", \"created\": \"%s\"}", record[0], record[1], record[2], record[3], record[4]);

            try {
                JestResult result = client.execute(new Index.Builder(source).index(index).type(type).build());

                if (result.isSucceeded()) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            } catch (Exception e) {
                Log.i("Error", "The application failed - reason: AddPatient.");
            }
            return Boolean.FALSE;
        }
    }

    /**
     * GetSinglePatient represents the object that gets a SINGLE patient from the server.
     *
     * @author Patrick Tamm
     */
    public static class GetSinglePatient extends AsyncTask<String, Void, Patient> {

        @Override
        protected Patient doInBackground(String... search_parameters) {

            Patient p = null;

            String type = "Patients";
            String query = String.format("{\"query\": {\"match\": {\"userID\": \"%s\"}}}", search_parameters[0]);
            try {
                JestResult result = client.execute(new Search.Builder(query).addIndex(index).addType(type).build());
                if (result.isSucceeded()) {
                    List<Patient> res = result.getSourceAsObjectList(Patient.class);
                    if (res.size() != 0) {
                        p = res.get(0);
                    } else {
                        Log.e("Error","nothing found.");
                    }
                } else {
                    Log.e("Error", "Some issues with GetShareCode query.");
                }
            } catch (Exception e) {
                Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server.");
            }
            return p;
        }
    }


    /**
     * GetPatients represents the object used to get a list of patients from the server.
     *
     * @author Patrick Tamm
     */
    public static class GetPatients extends AsyncTask<String, Void, ArrayList<Patient>> {

        @Override
        protected ArrayList<Patient> doInBackground(String... search_parameters) {

            ArrayList<Patient> foundPatients = new ArrayList<Patient>();
            String type = "Patients";
            String query = String.format("{\"query\": {\"match\": {\"cpUserName\": \"%s\"}}}", search_parameters[0]);
            try {
                JestResult result = client.execute(new Search.Builder(query).addIndex(index).addType(type).build());
                if (result.isSucceeded()) {
                    List<Patient> res = result.getSourceAsObjectList(Patient.class);
                    if (res.size() != 0) {
                        foundPatients.addAll(res);
                    } else {
                        Log.e("Error", "nothing found.");
                    }
                } else {
                    Log.e("Error", "Some issues with GetPatients query.");
                }
            } catch (Exception e) {
                Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server.");
            }
            return foundPatients;
        }
    }

    /**
     * DeletePatient represents the object used to delete a patient from the server.
     *
     * @author Patrick Tamm
     */
    public static class DeletePatient extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... search_parameters) {
            String type = "Patients";
            String query = String.format(
                    "{\"query\": {\"match\": {\"userID\": \"%s\"}}}", search_parameters[0]);
            try {
                JestResult result = client.execute(new DeleteByQuery.Builder(query).addIndex(index).addType(type).build());
                if (result.isSucceeded()) {
                    return Boolean.TRUE;
                } else {
                    Log.e("Error", "Some issues with DeletePatient query.");
                }
            } catch (Exception e) {
                Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server.");
            }
            return Boolean.FALSE;
        }
    }


    public static class AddShareCodeTable extends AsyncTask<String, Void, Void> { //use Void instead of void for AsyncTask as return type
        @Override
        protected Void doInBackground(String... indices) {

            String type = "shareCode";
            String source = "{\"shareCode\" : {\"properties\" : " +
                    "{\"userID\": {\"type\" : \"string\", \"index\": \"not_analyzed\"}," +
                    "\"code\": {\"type\" : \"string\"}, " +
                    "\"created\": {\"type\" : \"date\"}," +
                    "}}}";

            try {
                JestResult result = client.execute(new PutMapping.Builder(index, type, source).build());
                if (!result.isSucceeded()) {
                    Log.e("Error", "ElasticSearch was not able to add table.");
                }
            } catch (Exception e) {
                Log.i("Error", "The application failed - reason: AddShareCodeTable.");
            }
            return null; //Void requires return, (it's not void)
        }
    }

    /**
     * AddShareCode represents the object used to add a share code to the server. This will be used
     * when the patient is sharing their profile.
     *
     * @author Patrick Tamm
     */
    public static class AddShareCode extends AsyncTask<String, Void, Boolean> { //use Void instead of void for AsyncTask as return type
        @Override
        protected Boolean doInBackground(String... record) {

            String type = "shareCode";
            String source = String.format("{\"userID\": \"%s\", \"code\": \"%s\", \"created\": \"%s\"}", record[0], record[1], record[2]);

            try {
                JestResult result = client.execute(new Index.Builder(source).index(index).type(type).build());

                if (result.isSucceeded()) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            } catch (Exception e) {
                Log.i("Error", "The application failed - reason: AddShareCode.");
            }
            return Boolean.FALSE;
        }
    }

    /**
     * DeleteShareCode represents the object used to delete a share code from the share code table.
     * This will be done after successful login on the new device, as well as when the patient clicks
     * done in the generate code activity.
     *
     * @author Patrick Tamm
     */

    public static class DeleteShareCode extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... search_parameters) {
            String type = "shareCode";
            String query = String.format(
                    "{\"query\": {\"bool\": " +
                            "{\"must\": [" +
                            "{\"match\": {\"userID\": \"%s\"}}, " +
                            "{\"match\": {\"code\": \"%s\"}}]}}}", search_parameters[0], search_parameters[1]);
            try {
                JestResult result = client.execute(new DeleteByQuery.Builder(query).addIndex(index).addType(type).build());
                if (result.isSucceeded()) {
                    return Boolean.TRUE;
                } else {
                    Log.e("Error", "Some issues with DeleteShareCode query.");
                }
            } catch (Exception e) {
                Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server.");
            }
            return Boolean.FALSE;
        }
    }

    /**
     * GetShareCode represents the object used to get a share code from the server. This is done when
     * sharing patient profiles.
     *
     * @author Patrick Tamm
     */
    public static class GetShareCode extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... search_parameters) {

            String type = "shareCode";
            String query = String.format("{\"query\": {\"match\": {\"userID\": \"%s\"}}}", search_parameters[0]);
            try {
                JestResult result = client.execute(new Search.Builder(query).addIndex(index).addType(type).build());
                if (result.isSucceeded()) {
                    List<SourceAsObjectListMap> res = result.getSourceAsObjectList(SourceAsObjectListMap.class);
                    if (res.size() != 0) {
                        return res.get(0).getCode();
                    } else {
                        //Log.e("Error","nothing found.");
                        return "";
                    }
                } else {
                    Log.e("Error", "Some issues with GetShareCode query.");
                }
            } catch (Exception e) {
                Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server.");
            }
            return "";
        }
    }


    public static class AddRecordTable extends AsyncTask<String, Void, Void> { //use Void instead of void for AsyncTask as return type
        @Override
        protected Void doInBackground(String... indices) {

            String type = "Records";
//            String source = "{\"Records\" : {\"properties\" : " +
//                    "{\"title\": {\"type\" : \"string\", \"index\": \"not_analyzed\"}," +
//                    "\"date\": {\"type\" : \"date\",\"format\":\"HH:mm:ss.SSS dd/MM/yyyy\"}, " +
//                    "\"concernTitle\": {\"type\" : \"string\", \"index\": \"not_analyzed\"}," +
//                    "\"userName\": {\"type\" : \"string\", \"index\": \"not_analyzed\"}" +
//                    "}}}";

            String source = "{\"Records\" : {\"properties\" : " +
                    "{\"title\": {\"type\" : \"string\", \"index\": \"not_analyzed\"}," +
                    "\"date\": {\"type\" : \"string\", \"index\": \"not_analyzed\"}," +
                    "\"concernTitle\": {\"type\" : \"string\", \"index\": \"not_analyzed\"}," +
                    "\"userName\": {\"type\" : \"string\", \"index\": \"not_analyzed\"}" +
                    "}}}";

            try {
                JestResult result = client.execute(new PutMapping.Builder(index, type, source).build());
                if (!result.isSucceeded()) {
                    Log.e("Error", "ElasticSearch was not able to add table.");
                }
            } catch (Exception e) {
                Log.i("Error", "The application failed - reason: AddRecordTable.");
            }
            return null; //Void requires return, (it's not void)
        }
    }

    public static class AddPhoto extends AsyncTask<String, Void, Boolean> { //use Void instead of void for AsyncTask as return type
        @Override
        protected Boolean doInBackground(String... record) {

            String type = "Photos";
            String source = String.format("{\"bodyParts\": %s, \"encrypted\": \"%s\", \"photoID\": \"%s\", \"recordID\": \"%s\", \"userID\": \"%s\"}", record[0], record[1], record[2], record[3], record[4]);
            System.out.println(source);
            try {
                JestResult result = client.execute(new Index.Builder(source).index(index).type(type).build());
                if (result.isSucceeded()) {
                    Log.d("Success","data sent");
                    return Boolean.TRUE;
                } else {
                    Log.d("Failed","data not sent");
                    return Boolean.FALSE;
                }
            } catch (Exception e) {
                Log.i("Error", "The application failed - reason: AddPhotos.");
            }
            return Boolean.FALSE;
        }
    }

    public static class GetPhotos extends AsyncTask<String, Void, Void> { //use Void instead of void for AsyncTask as return type
        @Override
        protected Void doInBackground(String... search_parameters){

            ArrayList<Photograph> foundPhotos = new ArrayList<>();
            String type = "Photos";
            String query =  String.format("{\"query\":{\"bool\":{\"must\":[{\"match\":{\"recordID\":\"%s \"}},{\"match\":{\"userID\":\"%s\"}}]}}}", search_parameters[0], search_parameters[1]);
            try {
                JestResult result = client.execute(new Search.Builder(query).addIndex(index).addType(type).build());
                if (result.isSucceeded()) {
                    List<Photograph> res = result.getSourceAsObjectList(Photograph.class);
                    if (res.size() != 0) {
                        foundPhotos.addAll(res);
                    } else {
                        Log.e("Error", "nothing found.");
                    }
                } else {
                    Log.e("Error", "Some issues with the photos query");
                }
            } catch (Exception e){
                Log.i("Error","Something went wrong when we tried to communicate with the elasticsearch server.");
            }
            return null;
        }
    }
}
