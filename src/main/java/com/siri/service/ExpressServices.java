/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siri.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.siri.datasource.DBConnection;
import com.siri.datasource.HikariCPDataSource;

/**
 *
 * @author ronald.langat
 *
 * Methods to modify database ... SAVEREQUEST
 */
public class ExpressServices extends AbstractVerticle {

    @Override
    public void start(Future<Void> done) throws Exception {
        System.out.println("deploymentId ExpressServices =" + vertx.getOrCreateContext().deploymentID());
        EventBus eventBus = vertx.eventBus();

        eventBus.consumer("SAVEREQUEST", this::expressServiceRequest);
        eventBus.consumer("UPDATEACKNOWLEDGEMENT", this::expressServiceAckowledgement);
        eventBus.consumer("EXPRESSCALLBACK", this::expressServiceCallBack);
    }

    private void expressServiceRequest(Message<JsonObject> message) {
        JsonObject data = message.body();
        System.out.println("Response: " + data.toString());
        JsonObject ack = data.getJsonObject("acknowledgement");

        String SQL = "INSERT INTO mpesa_in(BusinessShortCode,Timestamp,TransactionType,"
                + "Amount,ConvenienceFee,PartyA,PartyB,PhoneNumber,AccountReference,"
                + "TransactionDesc,ACK_MerchantRequestID,ACK_CheckoutRequestID,"
                + "ACK_ResponseDescription,ACK_ResponseCode,ACK_CustomerMessage) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        long id = 0;

        try (Connection conn = HikariCPDataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL,
                        Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, data.getString("BusinessShortCode"));
            pstmt.setString(2, data.getString("Timestamp"));
            pstmt.setString(3, data.getString("TransactionType"));
            pstmt.setString(4, data.getString("Amount"));
            pstmt.setString(5, data.getString("ConvenienceFee"));
            pstmt.setString(6, data.getString("PartyA"));
            pstmt.setString(7, data.getString("PartyB"));
            pstmt.setString(8, data.getString("PhoneNumber"));
            pstmt.setString(9, data.getString("AccountReference"));
            pstmt.setString(10, data.getString("TransactionDesc"));
            pstmt.setString(11, ack.getString("MerchantRequestID"));
            pstmt.setString(12, ack.getString("CheckoutRequestID"));
            pstmt.setString(13, ack.getString("ResponseDescription"));
            pstmt.setString(14, ack.getString("ResponseCode"));
            pstmt.setString(15, ack.getString("CustomerMessage"));

            int affectedRows = pstmt.executeUpdate();
            // check the affected rows 
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        id = rs.getLong(1);
                    }
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void expressServiceRequestOriginal(Message<JsonObject> message) {
        JsonObject data = message.body();
        System.out.println("Response: " + data.toString());
        JsonObject ack = data.getJsonObject("acknowledgement");

        String finalRes = "{call CreateSTKExpressRequest(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        DBConnection conn = new DBConnection();

        try {
            CallableStatement cstm = conn.getConnection().prepareCall(finalRes);
            cstm.setString("BusinessShortCode", data.getString("BusinessShortCode"));
            cstm.setString("Timestamp", data.getString("Timestamp"));
            cstm.setString("TransactionType", data.getString("TransactionType"));
            cstm.setString("Amount", data.getString("Amount"));
            cstm.setString("ConvenienceFee", data.getString("ConvenienceFee"));
            cstm.setString("PartyA", data.getString("PartyA"));
            cstm.setString("PartyB", data.getString("PartyB"));
            cstm.setString("PhoneNumber", data.getString("PhoneNumber"));
            cstm.setString("AccountReference", data.getString("AccountReference"));
            cstm.setString("TransactionDesc", data.getString("TransactionDesc"));
            cstm.setString("ACK_MerchantRequestID", ack.getString("MerchantRequestID"));
            cstm.setString("ACK_CheckoutRequestID", ack.getString("CheckoutRequestID"));
            cstm.setString("ACK_ResponseDescription", ack.getString("ResponseDescription"));
            cstm.setString("ACK_ResponseCode", ack.getString("ResponseCode"));
            cstm.setString("ACK_CustomerMessage", ack.getString("CustomerMessage"));

            cstm.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conn.closeConn();
        }

    }

    private void expressServiceCallBack(Message<JsonObject> message) {
        JsonObject data = message.body();
        System.out.println("call back: " + data.toString());
        JsonObject body = data.getJsonObject("Body");
        JsonObject stkCallback = body.getJsonObject("stkCallback");
        String MerchantRequestID = stkCallback.getString("MerchantRequestID");
        String CheckoutRequestID = stkCallback.getString("CheckoutRequestID");
        String ResultDesc = stkCallback.getString("ResultDesc");
        Object ResultCode = stkCallback.getValue("ResultCode");

        String MpesaReceiptNumber = null;
        String TransactionDate = null;
        String PhoneNumber = null;

        if (ResultCode instanceof Integer) {
            int ResultCodeInt = (int) ResultCode;
            if (ResultCodeInt == 0) { // means success
                // Get ResultParameters
                JsonObject jResultParameters = stkCallback.getJsonObject("CallbackMetadata");
                JsonArray arrayParams = jResultParameters.getJsonArray("Item");
                JsonObject resultParameters = new JsonObject();
                for (Object jParam : arrayParams) {
                    JsonObject param = (JsonObject) jParam;
                    // System.out.println("key: " + param.getValue("Value"));
                    resultParameters.put(param.getString("Name"), String.valueOf(param.getValue("Value")));
                }
                System.out.println("resultParameters: " + resultParameters);

                MpesaReceiptNumber = resultParameters.getString("MpesaReceiptNumber");
                TransactionDate = resultParameters.getString("TransactionDate");
                PhoneNumber = resultParameters.getString("PhoneNumber");

            }
        }
        // update table with Callback
        String finalRes = "{call UpdateSTKCallBack(?,?,?,?,?,?,?)}";
        DBConnection conn = new DBConnection();
        try {
            CallableStatement cstm = conn.getConnection().prepareCall(finalRes);

            cstm.setString("R_MerchantRequestID", MerchantRequestID);
            cstm.setString("R_CheckoutRequestID", CheckoutRequestID);
            cstm.setString("R_ResultCode", String.valueOf(ResultCode));
            cstm.setString("R_ResultDesc", ResultDesc);
            cstm.setString("R_MpesaReceiptNumber", MpesaReceiptNumber);
            cstm.setString("R_TransactionDate", TransactionDate);
            cstm.setString("R_PhoneNumber", PhoneNumber);

            cstm.execute();

        } catch (SQLException e) {
            System.out.println("Exception: " + e.toString());
        } finally {
            try {
                conn.closeConn();
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.toString());
                Logger.getLogger(ExpressServices.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void expressServiceAckowledgement(Message<JsonObject> message) {
        JsonObject data = message.body();

    }
}
