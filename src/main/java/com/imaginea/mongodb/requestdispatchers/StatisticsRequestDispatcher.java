/*
 * Copyright (c) 2011 Imaginea Technologies Private Ltd.
 * Hyderabad, India
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.imaginea.mongodb.requestdispatchers;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.mongodb.common.DateProvider;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.services.CollectionService;
import com.imaginea.mongodb.services.CollectionServiceImpl;
import com.imaginea.mongodb.services.DatabaseService;
import com.imaginea.mongodb.services.DatabaseServiceImpl;
import com.mongodb.CommandResult;
import com.mongodb.Mongo;

/**
 * Defines resources for getting statistics of mongo Server and statistics of a particualr database present in mongo and
 * statistics of a collection present inside a database.
 * <p>
 * The resources also form a error JSON response when any exception occur while performing the operation.
 * 
 * @author Rachit Mittal
 * @since 7 July 2011
 */
@Path("/stats")
public class StatisticsRequestDispatcher extends BaseRequestDispatcher {
    private final static Logger logger = Logger.getLogger(StatisticsRequestDispatcher.class);

    /**
     * Default Constructor
     */
    public StatisticsRequestDispatcher() {
    }

    /**
     * Get Statistics of Mongo Server.
     * 
     * @param tokenId a token Id given to every user at Login.
     * @param request Get the HTTP request context to extract session parameters
     * @return String of JSON Format with server Stats.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getServerStats(@QueryParam("tokenId") final String tokenId, @Context final HttpServletRequest request) throws JSONException {
        if (logger.isInfoEnabled()) {
            logger.info("Recieved GET Stats Request for Server  [" + DateProvider.getDateTime() + "]");
        }
        String response = new ResponseTemplate().execute(logger, new ResponseCallback() {
            public String execute() throws Exception {
                String response = validateTokenId(tokenId, logger, request);
                if (response != null) {
                    return response;
                }
                // Get User for a given Token Id
                String userMappingkey = UserLogin.tokenIDToUserMapping.get(tokenId);
                if (userMappingkey == null) {
                    return formErrorResponse(logger, "User not mapped to token Id", ErrorCodes.INVALID_USER, null, "FATAL");
                }
                JSONObject resp = new JSONObject();
                // Create Instance of Service File
                Mongo mongoInstance = UserLogin.userToMongoInstanceMapping.get(userMappingkey);
                // Get Server Stats
                CommandResult cd = mongoInstance.getDB("admin").command("serverStatus");
                JSONObject stats = new JSONObject();
                stats.put("result", cd);
                resp.put("response", stats);
                response = resp.toString();
                return response;
            }
        });
        if (logger.isInfoEnabled()) {
            logger.info("Request Completed [" + DateProvider.getDateTime() + "]");
        }
        return response;
    }

    /**
     * GET Statistics of a particular database.
     * 
     * @param dbName : Name of Database for which to get DbStats.
     * @param tokenId : a token Id given to every user at Login.
     * @return : String of JSON Format with Db Stats.
     */
    @GET
    @Path("/db/{dbName}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDbStats(@PathParam("dbName") final String dbName, @QueryParam("tokenId") final String tokenId, @Context final HttpServletRequest request) throws JSONException {
        if (logger.isInfoEnabled()) {
            logger.info("Recieved GET Stats Request for a Db  [" + DateProvider.getDateTime() + "]");
        }
        String response = new ResponseTemplate().execute(logger, new ResponseCallback() {
            public String execute() throws Exception {
                String response = validateTokenId(tokenId, logger, request);
                if (response != null) {
                    return response;
                }
                // Get User for a given Token Id
                String userMappingkey = UserLogin.tokenIDToUserMapping.get(tokenId);
                if (userMappingkey == null) {
                    return formErrorResponse(logger, "User not mapped to token Id", ErrorCodes.INVALID_USER, null, "FATAL");
                }
                JSONObject temp = new JSONObject();
                JSONObject resp = new JSONObject();
                // Create Instance of Service File
                DatabaseService databaseService = new DatabaseServiceImpl(userMappingkey);
                // Get the result;
                JSONArray dbStats = databaseService.getDbStats(dbName);
                temp.put("result", dbStats);
                resp.put("response", temp);
                resp.put("totalRecords", dbStats.length());
                response = resp.toString();
                return response;
            }
        });
        if (logger.isInfoEnabled()) {
            logger.info("Request Completed [" + DateProvider.getDateTime() + "]");
        }
        return response;
    }

    /**
     * GET Statistics of Collections in a Database present in mongo.
     * 
     * @param dbName : Name of Database
     * @param collectionName : Name of Collection
     * @param request : Get the HTTP request context to extract session parameters
     * @return : A String of JSON Format with key <result> and value Collection Stats.
     */
    @GET
    @Path("/db/{dbName}/collection/{collectionName}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCollStats(@PathParam("dbName") final String dbName, @PathParam("collectionName") final String collectionName, @QueryParam("tokenId") final String tokenId,
            @Context final HttpServletRequest request) throws JSONException {
        if (logger.isInfoEnabled()) {
            logger.info("Recieved GET Stats Request for Collection  [" + DateProvider.getDateTime() + "]");
        }
        String response = new ResponseTemplate().execute(logger, new ResponseCallback() {
            public String execute() throws Exception {
                String response = validateTokenId(tokenId, logger, request);
                if (response != null) {
                    return response;
                }
                // Get User for a given Token Id
                String userMappingkey = UserLogin.tokenIDToUserMapping.get(tokenId);
                if (userMappingkey == null) {
                    return formErrorResponse(logger, "User not mapped to token Id", ErrorCodes.INVALID_USER, null, "FATAL");
                }
                JSONObject resp = new JSONObject();
                JSONObject temp = new JSONObject();
                // Create Instance of Service File
                CollectionService collectionService = new CollectionServiceImpl(userMappingkey);
                // Get the result;
                JSONArray collectionStats = collectionService.getCollStats(dbName, collectionName);
                temp.put("result", collectionStats);
                resp.put("response", temp);
                resp.put("totalRecords", collectionStats.length());
                response = resp.toString();
                return response;
            }
        });
        if (logger.isInfoEnabled()) {
            logger.info("Request Completed [" + DateProvider.getDateTime() + "]");
        }
        return response;
    }
}