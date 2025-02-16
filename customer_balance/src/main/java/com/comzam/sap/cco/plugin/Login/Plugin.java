package com.comzam.sap.cco.plugin.Login;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.sap.scco.ap.plugin.BasePlugin;
import com.sap.scco.ap.plugin.annotation.PluginAt;
import com.sap.scco.ap.pos.dao.IReceiptManager;
import com.sap.scco.ap.pos.entity.AdditionalFieldEntity;
import com.sap.scco.ap.pos.entity.BusinessPartnerEntity;
import com.sap.scco.env.UIEventDispatcher;
import com.sap.scco.util.logging.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
//import org.apache.tomcat.util.json.JSONParser;
//import org.json.simple.JSONObject;
public class Plugin extends BasePlugin {
  private static final Logger logger = Logger.getLogger(Plugin.class);

  AdditionalFieldEntity addFldItem1 = new AdditionalFieldEntity();

  long diffInDayss = 0L;

  @Override
public void startup() {
    super.startup();
  }

  @Override
public String getId() {
    return "Customer Balance Plugin";
  }

  @Override
public String getName() {
    return getId();
  }

  @Override
public String getVersion() {
    return getClass().getPackage().getImplementationVersion();
  }

  @Override
public boolean persistPropertiesToDB() {
    return true;
  }

  public Map<String, String> getPluginPropertyConfig() {
    Map<String, String> propertyConfig = new HashMap<>();
    propertyConfig.put("url", "String");
    return propertyConfig;
  }

  @PluginAt(pluginClass = IReceiptManager.class, method = "setBusinessPartner", where = PluginAt.POSITION.AFTER)
  public Object checkForLoyaltyFactor(Object proxy, Object[] args, Object ret, StackTraceElement caller) {
    Logger logger = Logger.getLogger(Plugin.class);

    logger.info("I'm after aftersetBusinessPartnerOtherModes-------------RETAIL-------------------------------");

    BusinessPartnerEntity customer = (BusinessPartnerEntity) args[1];
    if (customer == null) {
      return ret;
    }

    logger.info("------try------------------------------i- : " + customer.toString());
    String customerCode = customer.getExternalId(); // Replace with the desired CustomerCode

    JSONObject payload = new JSONObject();
    payload.put("cardCode",customer.getExternalId() );
  //  customer.getExternalId()
    double customerBalance = 0.0;

    try {
    //http://82.212.91.62:8080/B1iXcellerator/exec/ipo/vP.0010000112.in_HCSX/com.sap.b1i.vplatform.runtime/INB_HT_CALL_SYNC_XPT/INB_HT_CALL_SYNC_XPT.ipo/proc/getBalance
      URL url = new URL(this.getProperty("url", "String"));
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setRequestProperty("Content-Type", "application/json");
      String jsonRequest = "[{\"CustomerCode\":\"" + customerCode + "\"}]";

      try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
          out.writeBytes(jsonRequest);
          out.flush();
      }
      int responseCode = conn.getResponseCode();

      logger.info("  rcode : " + responseCode);

      if (responseCode == 200) {
    	    logger.info("  inside if " + conn.getResponseCode());

    	    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }

                JSONArray jsonArray = JSONArray.fromObject(response.toString());

                for (Object obj : jsonArray) {
                    JSONObject entry = (JSONObject) obj;
                    String code = entry.getString("CustomerCode");
                    if (code.equals(customerCode)) {
                        String balance = entry.getString("CustomerBalance");
                        showMessageToUi("Balance for CustomerCode " + customerCode + ": " + balance, "info");
                        break;  // Stop searching once the code is found
                    }
                }

    	}} else {
            System.out.println("HTTP Error: " + responseCode);
        }
        conn.disconnect();


    } catch (Exception ex) {
      showMessageToUi(ex.getMessage(), "error");
    }

    return ret;
  }


  public static void showMessageToUiWait(String msg, String type) {
    Map<String, String> dialogOptions = new HashMap<>();
    dialogOptions.put("message", msg);
    dialogOptions.put("id", "WAITING");
    dialogOptions.put("type", type);
    dialogOptions.put("maxLifeTime", "240");
    UIEventDispatcher.INSTANCE.dispatchAction("SHOW_MESSAGE_DIALOG", null, dialogOptions);
  }

  private void showMessageToUi(String msg, String type) {
    Map<String, String> dialogOptions = new HashMap<>();
    dialogOptions.put("message", msg);
    dialogOptions.put("id", Plugin.class.getSimpleName());
    dialogOptions.put("type", type);
    dialogOptions.put("maxLifeTime", "30");
    UIEventDispatcher.INSTANCE.dispatchAction("SHOW_MESSAGE_DIALOG", null,
        dialogOptions);
  }

  public static void hideMessage(String type) {
    Map<String, String> dialogOptions = new HashMap<>();
    dialogOptions.put("id", "WAITING");
    dialogOptions.put("type", type);
    dialogOptions.put("maxLifeTime", "120");
    UIEventDispatcher.INSTANCE.dispatchAction("HIDE_MESSAGE_DIALOG", null, dialogOptions);
  }
}

