/*
 *
 *
 * Copyright 2016 Symphony Communication Services, LLC
 *
 * Licensed to Symphony Communication Services, LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package com.symphony.authenticator.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.GenericType;

import org.symphonyoss.symphony.authenticator.invoker.ApiClient;
import org.symphonyoss.symphony.authenticator.invoker.ApiException;
import org.symphonyoss.symphony.authenticator.invoker.Configuration;
import org.symphonyoss.symphony.authenticator.invoker.Pair;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-07-29T22:47:27.367-07:00")
public class JenkinsAuthenticationApi {
  private ApiClient apiClient;

  public JenkinsAuthenticationApi() {
    this(Configuration.getDefaultApiClient());
  }

  public JenkinsAuthenticationApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }
  
  /**
   * Authenticate.
   * Based on the SSL client certificate presented by the TLS layer, authenticate\nthe API caller and return a session token.\n
   * @return Token
   * @throws ApiException if fails to make API call
   */
  public String jenkinsAuthenticationPost(String jenkinsJobPath) throws ApiException {
    Object localVarPostBody = "";
    this.apiClient.setBasePath("https://jenkins.symphony.com/");
    // create path and map variables
    String localVarPath = jenkinsJobPath;;

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    String userPassword = "jignesh:69cf6dbf29e42a022d86d120e9cc795a";
    String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
    localVarHeaderParams.put("Authorization", "Basic " + encoding);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };
    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
}
