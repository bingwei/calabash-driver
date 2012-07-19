/*
 * Copyright 2012 calabash-driver committers.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package sh.calaba.driver.server;

import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sh.calaba.driver.CalabashCapabilities;
import sh.calaba.driver.net.HttpClientFactory;

public class DriverRegistrationHandler {
  private HttpClientFactory httpClientFactory = new HttpClientFactory();
  private CalabashNodeConfiguration config;

  public DriverRegistrationHandler(CalabashNodeConfiguration config) {
    this.config = config;
  }

  public void handleRegsitration() throws Exception {
    String tmp = "http://" + config.getHubHost() + ":" + config.getHubPort() + "/grid/register";

    HttpClient client = httpClientFactory.getClient();

    URL registration = new URL(tmp);
    System.out.println("Registering the node to hub :" + registration);

    BasicHttpEntityEnclosingRequest r =
        new BasicHttpEntityEnclosingRequest("POST", registration.toExternalForm());
    JSONObject nodeConfig = getNodeConfig();
    r.setEntity(new StringEntity(nodeConfig.toString()));

    HttpHost host = new HttpHost(registration.getHost(), registration.getPort());
    HttpResponse response = client.execute(host, r);
    if (response.getStatusLine().getStatusCode() != 200) {
      throw new RuntimeException("Error sending the registration request.");
    }

  }

  public JSONObject getNodeConfig() {

    JSONObject res = new JSONObject();
    try {
      res.put("class", "org.openqa.grid.common.RegistrationRequest");
      res.put("configuration", getConfiguration());
      JSONArray caps = new JSONArray();
      for (CalabashCapabilities c : config.getCapabilities()) {
        caps.put(c.getRawCapabilities());
      }
      res.put("capabilities", caps);
    } catch (JSONException e) {
      throw new RuntimeException("Error encoding to JSON " + e.getMessage(), e);
    }

    return res;
  }

  public JSONObject getConfiguration() throws JSONException {
    JSONObject configuration = new JSONObject();

    configuration.put("port", config.getDriverPort());
    configuration.put("register", true);
    configuration.put("host", config.getDriverHost());
    configuration.put("proxy", config.getProxy());
    configuration.put("maxSession", config.getDriverMaxSession());
    configuration.put("hubHost", config.getHubHost());
    configuration.put("role", "node");
    configuration.put("registerCycle", 5000);
    configuration.put("hub", "http://" + config.getHubHost() + ":" + config.getHubPort()
        + "/grid/register");
    configuration.put("hubPort", config.getHubPort());
    configuration.put("url", "http://" + config.getDriverHost() + ":" + config.getDriverPort());
    configuration.put("remoteHost",
        "http://" + config.getDriverHost() + ":" + config.getDriverPort());
    return configuration;
  }
}
