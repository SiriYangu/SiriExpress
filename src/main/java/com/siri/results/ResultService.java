/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siri.results;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
/**
 *
 * @author ronald.langat
 */
public class ResultService extends AbstractVerticle{

    @Override
    public void start(Future<Void> done) throws Exception {
        System.out.println("deploymentId GatewayRouter =" + vertx.getOrCreateContext().deploymentID());
        EventBus eventBus = vertx.eventBus();
        System.out.println(eventBus.isMetricsEnabled());
    }    
       
}
