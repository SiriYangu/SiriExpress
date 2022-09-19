/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siri.service;

import com.siri.datasource.DBConnection;
import com.siri.datasource.HikariCPDataSource;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author Benart
 */
public class FeeService extends AbstractVerticle {

    EventBus eventBus;

    @Override
    public void start(Future<Void> done) throws Exception {

        System.out.println("deploymentId FeeService =" + vertx.getOrCreateContext().deploymentID());
        eventBus = vertx.eventBus();

        eventBus.consumer("Fee", this::getConvenienceFee);
    }

    private void getConvenienceFee(Message<JsonObject> message) {

        JsonObject payLoad = message.body();
        String amount = (!payLoad.getString("amount").trim().isEmpty()) ? payLoad.getString("amount").trim() : "0";

        Double amt = Double.parseDouble(amount);
        String queryBetween = "select * from fees where from_amount = "
                + "(SELECT MAX(from_amount) FROM fees WHERE from_amount <= " + amt
                + ") AND to_amount = (SELECT MIN(to_amount) FROM fees WHERE to_amount >= " + amt + ")";
        try (Connection con = HikariCPDataSource.getConnection();
                PreparedStatement pst = con.prepareStatement(queryBetween);
                ResultSet rs = pst.executeQuery();) {
            if (rs.next()) {
                payLoad.remove("amount");
                payLoad.put("response_code", "00");
                payLoad.put("convenience_fee", rs.getString("amount"));
                message.reply(payLoad);
            } else {
                payLoad.remove("amount");
                payLoad.put("response_code", "999");
                payLoad.put("convenience_fee", "0");
                payLoad.put("response_description", "convenience fee not found");
                message.reply(payLoad);
            }
        } catch (SQLException ex) {
            Logger.getLogger(FeeService.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
}
