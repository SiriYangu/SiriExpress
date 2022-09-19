/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siri.service;

import com.siri.main.EntryPoint;
//import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 * @author ronald.langat
 */
public class STKService extends AbstractVerticle {

    String SHORTCODE = EntryPoint.SHORTCODE;
    String COMMAND_ID = EntryPoint.COMMAND_ID;
    String RESULT_URL = EntryPoint.RESULT_URL;
    String CONSUMER_KEY = EntryPoint.CONSUMER_KEY;
    String CONSUMER_SECRET = EntryPoint.CONSUMER_SECRET;
    String EXPRESS_URL = EntryPoint.EXPRESS_URL;
    String PASS_KEY = EntryPoint.PASS_KEY;
    String TOKEN = "";

    EventBus eventBus;

    @Override
    public void start(Future<Void> done) throws Exception {

        System.out.println("deploymentId STKService =" + vertx.getOrCreateContext().deploymentID());
        eventBus = vertx.eventBus();

        eventBus.consumer("STK", this::expressService);
    }

    private void expressService(Message<JsonObject> message) {

        JsonObject b2c_Data = message.body();
        String amount = b2c_Data.getString("Amount");
        String phoneNumber = b2c_Data.getString("PhoneNumber").trim();
        String accountReference = b2c_Data.getString("AccountReference").trim();
        String transactionDesc = b2c_Data.getString("TransactionDesc");
        String convenienceFee = b2c_Data.getString("ConvenienceFee");
        
        // Generate token
        TOKEN = Utilities.getAccessToken(CONSUMER_KEY, CONSUMER_SECRET);
        System.out.println("Token: "+TOKEN);
        //Get and format Date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String Timestamp = sdf.format(new Date());

        // pass Key
        //String Passkey = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";
        String credential = SHORTCODE + PASS_KEY + Timestamp;
        System.out.println("credential: " + credential);
        String auth = "";
        byte[] bytes = null;
        try {
            bytes = credential.getBytes("ISO-8859-1");
            auth = DatatypeConverter.printBase64Binary(bytes);
        } catch (UnsupportedEncodingException ex) {
            System.out.println("Credential Error: " + ex);
            Logger.getLogger(STKService.class.getName()).log(Level.SEVERE, null, ex);
        }

        String jsonRequest = "{\n"
                + "    \"BusinessShortCode\": \"" + SHORTCODE + "\",\n"
                + "    \"Password\": \"" + auth + "\",\n"
                + "    \"Timestamp\": \"" + Timestamp + "\",\n"
                + "    \"TransactionType\": \"" + COMMAND_ID + "\",\n"
                + "    \"Amount\": \"" + amount + "\",\n"
                + "    \"PartyA\": \"" + phoneNumber + "\",\n"
                + "    \"PartyB\": \"" + SHORTCODE + "\",\n"
                + "    \"PhoneNumber\": \"" + phoneNumber + "\",\n"
                + "    \"CallBackURL\": \"" + RESULT_URL + "\",\n"
                + "    \"AccountReference\": \"" + accountReference + "\",\n"
                + "    \"TransactionDesc\": \"" + transactionDesc + "\"\n"
                + "}";
        System.out.println(jsonRequest);

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonRequest);
        Request request = new Request.Builder()
                .url(EXPRESS_URL)
                .post(body)
                .addHeader("authorization", "Bearer " + TOKEN + "")
                .addHeader("content-type", "application/json")
                .build();

        try {
            Response response = client.newCall(request).execute();
            JsonObject accessT = new JsonObject(response.body().string());
            // combine req and aknowledgement
            JsonObject requestAck = new JsonObject(jsonRequest);
            requestAck.put("acknowledgement", accessT);
            requestAck.put("ConvenienceFee", convenienceFee);
            // save to db  ||  update acknowledgement 
            eventBus.send("SAVEREQUEST", requestAck);
        
        } catch (IOException ex) {
            System.out.println("Ex: "+ex);
            Logger.getLogger(STKService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
