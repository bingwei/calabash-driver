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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidParameterException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sh.calaba.driver.CalabashCapabilities;
import sh.calaba.driver.server.exceptions.CalabashConfigurationException;

/**
 * The object to hold the Calabash Driver configuration.
 * 
 * @author ddary
 * 
 */
public class CalabashNodeConfiguration {
  final static Logger logger = LoggerFactory.getLogger(CalabashNodeConfiguration.class);
  public static final String CAPABILITIES = "capabilities";
  public static final String CONFIGURATION = "configuration";
  protected List<CalabashCapabilities> capabilities = new ArrayList<CalabashCapabilities>();
  protected String driverHost = null;
  protected String mobileAppPath = null;
  protected String mobileTestAppPath = null;
  protected int driverMaxSession;
  protected int driverPort;
  protected boolean driverRegistrationEnabled;
  protected boolean installApksEnabled;
  protected boolean cleanSavedUserDataEnabled;
  protected String hubHost = null;
  protected String proxy = null;
  protected int hubPort;

  /**
   * Reads the the driver configuration form the specified file. The file is expected to be in JSON
   * format.
   * 
   * @param driverConfigurationFile The file name of the driver configuration file.
   * @return The Calabash node configuration.
   * @throws CalabashConfigurationException On IO and Parsing errors.
   * @throws InvalidParameterException if parameter is null or empty
   */
  public static CalabashNodeConfiguration readFromFile(File driverConfigurationFile)
      throws CalabashConfigurationException {
    if (driverConfigurationFile == null) {
      throw new InvalidParameterException(
          "Calabash-Driver Configuration-File is missing. Pls specifiy name like: calabashNode.json");
    }
    String driverConfiguration;
    try {
      driverConfiguration = FileUtils.readFileToString(driverConfigurationFile);
    } catch (IOException e1) {
      throw new CalabashConfigurationException(
          "Error reading file content. Did you have specified the right file name and path?", e1);
    }

    try {
      return new CalabashNodeConfiguration(new JSONObject(driverConfiguration));
    } catch (JSONException e) {
      throw new CalabashConfigurationException("Error occured during parsing json file: '"
          + driverConfigurationFile + "'. Pls make sure you are using a valid JSON file!", e);
    }
  }

  /**
   * Reads the the driver configuration from the specified URI. The file is expected to be in JSON
   * format.
   * 
   * @param driverConfigFileURI The file name of the driver configuration file URI.
   * @return The Calabash node configuration.
   * @throws CalabashConfigurationException On IO and Parsing errors.
   * @throws InvalidParameterException if parameter is null or empty
   */
  public static CalabashNodeConfiguration readFromURI(URI driverConfigFileURI)
      throws CalabashConfigurationException {
    if (driverConfigFileURI == null) {
      throw new InvalidParameterException("Calabash-Driver Configuration-URI is missing.");
    }
    if (driverConfigFileURI.getHost() == null || driverConfigFileURI.getPath() == null) {
      throw new InvalidParameterException("Calabash-Driver Configuration-URI is invalid.");
    }
    String driverConfiguration;
    try {
      HttpClient client = getDefaultHttpClient();
      HttpGet request = new HttpGet(driverConfigFileURI);
      HttpResponse response = client.execute(request);

      driverConfiguration = IOUtils.toString(response.getEntity().getContent());
    } catch (IOException e1) {
      logger.error("Error occured while reading config from URI:", e1);
      throw new CalabashConfigurationException(
          "Error reading file content. Did you have specified the right URI?", e1);
    } catch (KeyManagementException e) {
      logger.error("Error occured while creating httpclient:", e);
      throw new CalabashConfigurationException("Error occured while creating httpclient", e);
    } catch (NoSuchAlgorithmException e) {
      logger.error("Error occured while creating httpclient:", e);
      throw new CalabashConfigurationException("Error occured while creating httpclient", e);
    }

    try {
      return new CalabashNodeConfiguration(new JSONObject(driverConfiguration));
    } catch (JSONException e) {
      logger.error("Error occured while parsing config file: ", e);
      throw new CalabashConfigurationException("Error occured during parsing json file from URI: '"
          + driverConfigFileURI + "'. Pls make sure you are using a valid JSON file!", e);
    }
  }

  protected CalabashNodeConfiguration() {}

  protected CalabashNodeConfiguration(JSONObject config) throws JSONException,
      CalabashConfigurationException {
    readDriverConfiguration(config.getJSONObject(CONFIGURATION));
    readCapabilities(config.getJSONArray(CAPABILITIES));
  }

  /**
   * @return the capabilities
   */
  public List<CalabashCapabilities> getCapabilities() {
    return capabilities;
  }

  /**
   * @return the proxy
   */
  public String getProxy() {
    return proxy;
  }

  /**
   * @return the driverHost
   */
  public String getDriverHost() {
    return driverHost;
  }

  /**
   * @return the driverMaxSession
   */
  public int getDriverMaxSession() {
    return driverMaxSession;
  }

  /**
   * @return the driverPort
   */
  public int getDriverPort() {
    return driverPort;
  }

  /**
   * @return the mobileAppPath
   */
  public String getMobileAppPath() {
    return mobileAppPath;
  }

  /**
   * @return the mobileTestAppPath
   */
  public String getMobileTestAppPath() {
    return mobileTestAppPath;
  }

  /**
   * @return the hubHost
   */
  public String getHubHost() {
    return hubHost;
  }

  /**
   * @return the hubPort
   */
  public int getHubPort() {
    return hubPort;
  }

  /**
   * @return the installApksEnabled
   */
  public boolean isInstallApksEnabled() {
    return installApksEnabled;
  }

  /**
   * @return the cleanSavedUserData
   */
  public boolean isCleanSavedUserDataEnabled() {
    return cleanSavedUserDataEnabled;
  }

  /**
   * @return the driverRegistrationEnabled
   */
  public boolean isDriverRegistrationEnabled() {
    return driverRegistrationEnabled;
  }

  private void readCapabilities(JSONArray jsonArray) throws JSONException,
      CalabashConfigurationException {
    if (jsonArray == null || jsonArray.length() == 0) {
      throw new CalabashConfigurationException("No capabilities are specified.");
    }
    for (int i = 0; i < jsonArray.length(); i++) {
      JSONObject capa = jsonArray.getJSONObject(i);

      capabilities.add(CalabashCapabilities.fromJSON(capa));
    }

  }

  /**
   * Reads the driver configuration from given config.
   * 
   * @param configuration The driver config.
   * @throws JSONException On JSON errors.
   */
  private void readDriverConfiguration(JSONObject configuration) throws JSONException {
    hubHost = configuration.getString("hubHost");
    hubPort = configuration.getInt("hubPort");
    driverHost = configuration.getString("host");
    driverPort = configuration.getInt("port");
    driverRegistrationEnabled = configuration.getBoolean("register");
    driverMaxSession = configuration.getInt("maxSession");
    mobileAppPath = configuration.getString("autApk");
    mobileTestAppPath = configuration.getString("autTestApk");
    installApksEnabled = configuration.getBoolean("installApks");
    cleanSavedUserDataEnabled = configuration.getBoolean("cleanSavedUserData");
    proxy =
        configuration.isNull("proxy")
            ? "org.openqa.grid.selenium.proxy.DefaultRemoteProxy"
            : configuration.getString("proxy");
  }

  protected static HttpClient getDefaultHttpClient() throws KeyManagementException,
      NoSuchAlgorithmException {
    HttpClient base = new DefaultHttpClient();

    SSLContext ctx = SSLContext.getInstance("TLS");
    X509TrustManager tm = new X509TrustManager() {

      public void checkClientTrusted(X509Certificate[] xcs, String string)
          throws CertificateException {}

      public void checkServerTrusted(X509Certificate[] xcs, String string)
          throws CertificateException {}

      public X509Certificate[] getAcceptedIssuers() {
        return null;
      }
    };
    ctx.init(null, new TrustManager[] {tm}, null);
    SSLSocketFactory ssf = new SSLSocketFactory(ctx);
    ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    ClientConnectionManager ccm = base.getConnectionManager();
    SchemeRegistry sr = ccm.getSchemeRegistry();
    sr.register(new Scheme("https", ssf, 443));
    return new DefaultHttpClient(ccm, base.getParams());
  }
}
