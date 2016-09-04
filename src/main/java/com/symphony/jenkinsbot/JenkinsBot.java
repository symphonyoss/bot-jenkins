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

package com.symphony.jenkinsbot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.client.services.MessageListener;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.authenticator.invoker.ApiClient;
import org.symphonyoss.symphony.authenticator.invoker.ApiException;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.pod.model.Stream;

import com.symphony.authenticator.api.JenkinsAuthenticationApi;

public class JenkinsBot {

    private final Logger logger = LoggerFactory.getLogger(JenkinsBot.class);
    private SymphonyClient symClient;
    private Map<String,String> initParams = new HashMap<String,String>();
    private static ExecutorService threadpool = Executors.newCachedThreadPool();

    private static Set<String> initParamNames = new HashSet<String>();
    static {
        initParamNames.add("sessionauth.url");
        initParamNames.add("keyauth.url");
        initParamNames.add("pod.url");
        initParamNames.add("agent.url");
        initParamNames.add("rss.url");
        initParamNames.add("rss.limit");
        initParamNames.add("truststore.file");
        initParamNames.add("truststore.password");
        initParamNames.add("keystore.password");
        initParamNames.add("certs.dir");
        initParamNames.add("bot.user.name");
        initParamNames.add("bot.user.email");
        initParamNames.add("receiver.user.email");
    }

    public static void main(String[] args) {
        new JenkinsBot();
    }

    public JenkinsBot() {
        initParams();
        initAuth();
        initDataFeed();
        
        
    }

	private void initDataFeed() {
	    try {
			
			symClient.getMessageService().registerMessageListener(listener);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void initParams() {
        for(String initParam : initParamNames) {
            String systemProperty = System.getProperty(initParam);
            if (systemProperty == null) {
                throw new IllegalArgumentException("Cannot find system property; make sure you're using -D" + systemProperty + " to run RssBot");
            } else {
                initParams.put(initParam,systemProperty);
            }
        }
    }

    private void initAuth() {
        try {
            symClient = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.BASIC);

            logger.debug("{} {}", System.getProperty("sessionauth.url"),
                    System.getProperty("keyauth.url"));


            AuthorizationClient authClient = new AuthorizationClient(
                    initParams.get("sessionauth.url"),
                    initParams.get("keyauth.url"));


            authClient.setKeystores(
                    initParams.get("truststore.file"),
                    initParams.get("truststore.password"),
                    initParams.get("certs.dir") + initParams.get("bot.user.name") + ".p12",
                    initParams.get("keystore.password"));

            SymAuth symAuth = authClient.authenticate();


            symClient.init(
                    symAuth,
                    initParams.get("bot.user.email"),
                    initParams.get("agent.url"),
                    initParams.get("pod.url")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MessageSubmission createAMessage(String message, MessageSubmission.FormatEnum type) {
        MessageSubmission aMessage = new MessageSubmission();
        aMessage.setFormat(type);
        aMessage.setMessage(message);
        return aMessage;
    }

    MessageListener listener = new MessageListener() {
		
		@Override
		public void onMessage(Message message) {
			// TODO Auto-generated method stub
			
			System.out.println("message------- " + message.getMessage());
			String messageContent = message.getMessage().replace("<messageML>", "").replace("</messageML>", "");
			if(messageContent.toLowerCase().contains("/start:")){
				String [] str = messageContent.split("/start:");
				String jenkinsJobToStart = "/job/" + str[1].trim();
				Callable<Boolean> task = null;
				 
				 task = new Callable<Boolean>() {

					@Override
					public Boolean call() throws Exception {
						// TODO Auto-generated method stub
						return startBuildAndReportStatus(jenkinsJobToStart, message);
					}
					 
				};
				threadpool.submit(task);
				
			}else if(messageContent.toLowerCase().contains("/watch:")){
				String [] str = messageContent.split("/watch:");
				String jenkinsJobToStart = "/job/" +str[1].trim();
				Callable<Boolean> task = null;
				 
				 task = new Callable<Boolean>() {

					@Override
					public Boolean call() throws Exception {
						// TODO Auto-generated method stub
						return watchBuildAndReportStatus(jenkinsJobToStart, message);
					}
					 
				};
				threadpool.submit(task);
				
			}else if(messageContent.toLowerCase().startsWith("/help:")){
				
				Callable<Boolean> task = null;
				 
				 task = new Callable<Boolean>() {

					@Override
					public Boolean call() throws Exception {
						// TODO Auto-generated method stub
						return showHelp(message);
					}
					 
				};
				threadpool.submit(task);
				
			}
			else if(messageContent.toLowerCase().contains("/report:")){
				Callable<Boolean> task = null;
				String [] str = messageContent.split("/report:");
				if (str[1].trim().toLowerCase().startsWith("regression")){
					task = new Callable<Boolean>() {

						@Override
						public Boolean call() throws Exception {
							// TODO Auto-generated method stub
							 reportStatus("/job/CIP-QA-Robot-WebClient-Test-Suite", message.getStreamId());
							return reportStatus("/job/CIP-QA-Robot-AC-Test-Suite", message.getStreamId());
						}
						 
					};
					threadpool.submit(task);

					
				}else{
					
				
				String jenkinsJobToStart = "/job/" + str[1].trim();
				
				 
				
				 task = new Callable<Boolean>() {

					@Override
					public Boolean call() throws Exception {
						// TODO Auto-generated method stub
						return reportStatus(jenkinsJobToStart, message.getStreamId());
					}
					 
				};
				threadpool.submit(task);
				}
			}
			else if (messageContent.toLowerCase().contains("/deploy:")) {
				String[] str = messageContent.split("/deploy:");
				String pod = str[1].trim();
				String jobUrl = "job/CIP-GEN-SBE-Deploy-Update-Check-Validate-Pod";
				String buildParams = "?PARM_BUILD_TOOLS_FORK=SymphonyOSF&PARM_BUILD_TOOLS_REPO=ESAutoDeploy&PARM_BUILD_TOOLS_BRANCH=origin/master"
						+ "&PARM_JENKINS_NODE=infra-dev-ause1-slave-jenkins2&PARM_DEPLOY_SALT_REPO_SBE=cip-local-dev&PARM_DEPLOY_SALT_REPO_SFE=cip-local-dev"
						+ "&PARM_DEPLOY_ONLY_SFE=False&PARM_DEPLOY_SALT_ENV=dev&PARM_CHECK_PIPELINE_NAME=IGNORE";
				Callable<Boolean> task = null;
				if ("NEXUS".equals(pod)) {
					final String nexusBuildParams = buildParams + "&PARM_DEPLOY_POD_NAME=nexus-dev-ause1-all.symphony.com";
					task = new Callable<Boolean>() {

						@Override
						public Boolean call() throws Exception {
							// TODO Auto-generated method stub
							return startDeploymentAndReportStatus(jobUrl,
									nexusBuildParams, message);
						}

					};
					threadpool.submit(task);

				}else if("SEC".equals(pod)){
					
					final String nexusBuildParams = buildParams + "&PARM_DEPLOY_POD_NAME=sec-dev-ause1-all.symphony.com";
					task = new Callable<Boolean>() {

						@Override
						public Boolean call() throws Exception {
							// TODO Auto-generated method stub
							return startDeploymentAndReportStatus(jobUrl,
									nexusBuildParams, message);
						}

					};
					threadpool.submit(task);
					
				}else {
					String message2 = "NEXUS and SEC pods are supported at this point";
					Chat chat2 = new Chat();
					 Stream stream = new Stream();
					 stream.setId(message.getStreamId());
					 chat2.setStream(stream);
					
					 try {
						symClient.getMessageService().sendMessage(chat2, createAMessage(message2, MessageSubmission.FormatEnum.MESSAGEML));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        
					
				}

			}

		}
	};
	
	 private JSONObject getBuildInfo(JenkinsAuthenticationApi jenkinsApi, String jobUrl) {

		    String buildInfo = null;
		    JSONObject buildInfoJson = null;
		    try {
		    	//JenkinsAuthenticationApi jenkinsApi = new JenkinsAuthenticationApi(new ApiClient());
				buildInfo = jenkinsApi.jenkinsAuthenticationPost(jobUrl + "/api/json");
				buildInfoJson = new JSONObject(buildInfo);

		    } catch (ApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		    return buildInfoJson;

		  }
	 
	 private boolean startDeploymentAndReportStatus(String buildURl,String params,  Message msg) throws Exception{
		 //send a message
		 Chat chat2 = new Chat();
		 Stream stream = new Stream();
		 stream.setId(msg.getStreamId());
		 chat2.setStream(stream);
		 
		 JenkinsAuthenticationApi jenkinsApi = new JenkinsAuthenticationApi(new ApiClient());
		 JSONObject buildInfoJson = getBuildInfo(jenkinsApi, buildURl);
         Object n = buildInfoJson.get("nextBuildNumber");

         int nextBuildNumber = Integer.parseInt(n.toString());
         
         try {
        	 
        	 System.out.println(buildURl + "/buildWithParameters" + params);
			jenkinsApi.jenkinsAuthenticationPost(buildURl + "/buildWithParameters" + params);
            
		} catch (ApiException e) {
				Thread.sleep(20000);
		}
         
         JSONObject buildStatusJson = getBuildInfo(jenkinsApi, buildURl + "/" + nextBuildNumber);
         String buildURL = buildStatusJson.get("url").toString();
         String messageURl = "<messageML>Build Url is <a href=\"" + buildURL + "\"/></messageML>";
         symClient.getMessageService().sendMessage(chat2, createAMessage(messageURl, MessageSubmission.FormatEnum.MESSAGEML));
         boolean isCompleted = false;
         Object buildResult = "";
         while (!isCompleted) {

           JSONObject buildStatusJson2 = getBuildInfo(jenkinsApi, buildURl + "/" + nextBuildNumber);
           buildResult = buildStatusJson2.get("result");
           System.out.println("---------------------" + buildResult.getClass().toString());
           if (JSONObject.NULL == buildResult) {
             //System.out.println("build is still running.. waiting.. ");
            // Thread.sleep(30000);
           } else {
             System.out.println("build result " + buildResult);
             isCompleted = true;
             break;
           }

         }
         
         if(isCompleted){
        	 	
				try {
					String messageURl2 = "<messageML>Build result : " +  buildResult  + "<br/> Build Url :  <a href=\"" + buildURL + "\"/></messageML>";
			         symClient.getMessageService().sendMessage(chat2, createAMessage(messageURl2, MessageSubmission.FormatEnum.MESSAGEML));
			         
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
         }
        
         return isCompleted;

		 
	 }
	 
	 private boolean startBuildAndReportStatus(String buildURl, Message msg) throws Exception{
		 //send a message
		 Chat chat2 = new Chat();
		 Stream stream = new Stream();
		 stream.setId(msg.getStreamId());
		 chat2.setStream(stream);
		 
		 JenkinsAuthenticationApi jenkinsApi = new JenkinsAuthenticationApi(new ApiClient());
		 JSONObject buildInfoJson = getBuildInfo(jenkinsApi, buildURl);
         Object n = buildInfoJson.get("nextBuildNumber");

         int nextBuildNumber = Integer.parseInt(n.toString());
         
         try {
			jenkinsApi.jenkinsAuthenticationPost(buildURl + "/build");
            
		} catch (ApiException e) {
				Thread.sleep(15000);
		}
         
         JSONObject buildStatusJson = getBuildInfo(jenkinsApi, buildURl + "/" + nextBuildNumber);
         String buildURL = buildStatusJson.get("url").toString();
         String messageURl = "<messageML>Started a Build. Build  url : <a href=\"" + buildURL + "\"/></messageML>";
         symClient.getMessageService().sendMessage(chat2, createAMessage(messageURl, MessageSubmission.FormatEnum.MESSAGEML));
         boolean isCompleted = false;
         Object buildResult = "";
         while (!isCompleted) {

           JSONObject buildStatusJson2 = getBuildInfo(jenkinsApi, buildURl + "/" + nextBuildNumber);
           buildResult = buildStatusJson2.get("result");
           //System.out.println("---------------------" + buildResult.getClass().toString());
           if (JSONObject.NULL == buildResult) {
             System.out.println("build is still running.. waiting.. ");
              //Thread.sleep(30000);
           } else {
             System.out.println("build result " + buildResult);
             isCompleted = true;
             break;
           }

         }
         
         if(isCompleted){
        	 	
				try {
					String messageURl2 = "<messageML>Build result : " +  buildResult  + "<br/> Build completed :  <a href=\"" + buildURL + "\"/></messageML>";
			         symClient.getMessageService().sendMessage(chat2, createAMessage(messageURl2, MessageSubmission.FormatEnum.MESSAGEML));
			         
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
         }
        
         return isCompleted;

		 
	 }
	 
	 private boolean watchBuildAndReportStatus(String buildURl, Message msg) throws Exception{
		 //send a message
		 Chat chat2 = new Chat();
		 Stream stream = new Stream();
		 stream.setId(msg.getStreamId());
		 chat2.setStream(stream);
		 
		 JenkinsAuthenticationApi jenkinsApi = new JenkinsAuthenticationApi(new ApiClient());
		 JSONObject buildInfoJson = getBuildInfo(jenkinsApi, buildURl);
         Object n = buildInfoJson.getJSONObject("lastBuild").getInt("number");
         	
         int currentBuildNumber = Integer.parseInt(n.toString());
         
         JSONObject buildStatusJson = getBuildInfo(jenkinsApi, buildURl + "/" + currentBuildNumber);
         String buildURL = buildStatusJson.get("url").toString();
         String messageURl = "<messageML>Build Url is <a href=\"" + buildURL + "\"/></messageML>";
         symClient.getMessageService().sendMessage(chat2, createAMessage(messageURl, MessageSubmission.FormatEnum.MESSAGEML));
         boolean isCompleted = false;
         Object buildResult = "";
         while (!isCompleted) {

           JSONObject buildStatusJson2 = getBuildInfo(jenkinsApi, buildURl + "/" + currentBuildNumber);
           buildResult = buildStatusJson2.get("result");
           System.out.println("---------------------" + buildResult.getClass().toString());
           if (JSONObject.NULL == buildResult) {
             //System.out.println("build is still running.. waiting.. ");
        	   Thread.sleep(10000);
           } else {
             System.out.println("build result " + buildResult);
             isCompleted = true;
             break;
           }

         }
         
         if(isCompleted){
        	 	
				try {
					String messageURl2 = "<messageML>Build result : " +  buildResult  + "<br/> Build Url :  <a href=\"" + buildURL + "\"/></messageML>";
			         symClient.getMessageService().sendMessage(chat2, createAMessage(messageURl2, MessageSubmission.FormatEnum.MESSAGEML));
			         
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
         }
        
         return isCompleted;

		 
	 }
	 
	 private boolean reportStatus(String buildURl, String streamId) throws Exception{
		 
		 int failCount=0, totalCount=0, passCount=0;
		 Chat chat2 = new Chat();
		 Stream stream = new Stream();
		 stream.setId(streamId);
		 chat2.setStream(stream);
		 
		 JenkinsAuthenticationApi jenkinsApi = new JenkinsAuthenticationApi(new ApiClient());
		 JSONObject buildInfoJson = getBuildInfo(jenkinsApi, buildURl);
		 int lastBuildNumber = buildInfoJson.getJSONObject("lastBuild").getInt("number");
         	
         
         String lastBuildUrl = buildURl + "/" + lastBuildNumber;
         JSONObject lastBuildRunInfo = getBuildInfo(jenkinsApi, lastBuildUrl);
         if(lastBuildRunInfo.getBoolean("building")){
           lastBuildNumber = lastBuildNumber - 1;
         }

       String lastBuildUrl2 = buildURl + "/" + lastBuildNumber;
       JSONObject lastBuildRunInfo2 = getBuildInfo(jenkinsApi, lastBuildUrl2);
       String jobName = lastBuildRunInfo2.getString("fullDisplayName");
         JSONArray actions = lastBuildRunInfo2.getJSONArray("actions");
       for (int j=0; j< actions.length(); j++){
         if(actions.getJSONObject(j).has("failCount")){
             failCount = actions.getJSONObject(j).getInt("failCount");
             totalCount = actions.getJSONObject(j).getInt("totalCount");
           break;

         }else{
           continue;
         }

       }
       passCount = totalCount - failCount;
       String buildURL = lastBuildRunInfo2.get("url").toString();
       String messageURl = "<messageML>Total tests for: " +  jobName + " - "   + totalCount +  " Pass: " + passCount + " Fail: " + failCount   +" Jenkins report url :  <a href=\"" + buildURL + "\"/></messageML>";
       symClient.getMessageService().sendMessage(chat2, createAMessage(messageURl, MessageSubmission.FormatEnum.MESSAGEML));
       return true;

		 
	 }
	 
	 private boolean showHelp(Message msg) throws Exception{
		
		 String helpStr = "<messageML> Help for jenkins bot <br/>" +
				 "/start:jenkins-job-Name -- Starts a jenkins job <br/>" +
				 "/watch :jenkins-job-Name -- Sends a message when a jenkins job is completed <br/>" +
				 "/report:jenkins-job-Name - Sends a message with test results <br/>" +
				 "/report:regression - Sends a message with regression test results <br/>" +
				 "/deploy :pod-name - deploys latest dev builds to the box <br/>" 		
		 		+ "</messageML>";
	
		 Chat chat2 = new Chat();
		 Stream stream = new Stream();
		 stream.setId(msg.getStreamId());
		 chat2.setStream(stream);
		
		 symClient.getMessageService().sendMessage(chat2, createAMessage(helpStr, MessageSubmission.FormatEnum.MESSAGEML));
         return true;
	 }
}
