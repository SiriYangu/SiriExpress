/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siri.main;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import com.siri.log.Logging;
import org.apache.commons.lang3.StringUtils;
import com.siri.util.Prop;
import com.siri.service.ExpressServices;
import com.siri.service.FeeService;
import com.siri.service.STKService;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;

/**
 *
 * @author ronald.langat
 */
public class EntryPoint extends AbstractVerticle {

    public static Prop props;
    public static Logging logger;
    public static String LOGS_PATH;
    public static String DATABASE_DRIVER;
    public static String DATABASE_IP;
    public static String DATABASE_PORT;
    public static String DATABASE_NAME;
    public static String DATABASE_USER;
    public static String DATABASE_PASSWORD;
    public static String DATABASE_SERVER_TIME_ZONE;
    public static String SYSTEM_PORT;
    public static String SYSTEM_HOST;
    // ----- B2B ----------------
    public static String SHORTCODE;
    public static String COMMAND_ID;
    public static String RESULT_URL;
    public static String EXPRESS_URL;
    public static String CONSUMER_KEY;
    public static String CONSUMER_SECRET;
    public static String TOKEN_URL;
    public static String PASS_KEY;

    public static String CONVENIENCE_FEE;
    public static String MINIMUM_AMOUNT;

    // Hikari Setup
    static int MAX_POOL_SIZE = 3;
    static int MAX_IDLE_TIME = 4;
    static int WORKER_POOL_SIZE = 6;
    static int TIMEOUT_TIME = 50000;
    static int INITIAL_POOL_SIZE = 2;

    static {
        props = new Prop();
        logger = new Logging();
        LOGS_PATH = "";
        DATABASE_DRIVER = "";
        DATABASE_IP = "";
        DATABASE_PORT = "";
        DATABASE_NAME = "";
        DATABASE_USER = "";
        DATABASE_PASSWORD = "";
        DATABASE_SERVER_TIME_ZONE = "";
        SYSTEM_PORT = "";
        SYSTEM_HOST = "";
        //-----EXPRESS ------
        SHORTCODE = "";
        COMMAND_ID = "";
        RESULT_URL = "";
        EXPRESS_URL = "";
        CONSUMER_KEY = "";
        CONSUMER_SECRET = "";
        TOKEN_URL = "";
        PASS_KEY = "";

        CONVENIENCE_FEE = "";
        MINIMUM_AMOUNT = "";

    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        // instatiate Properties and Logging classes
        props = new Prop();
        logger = new Logging();

        // Get properties from property file
        LOGS_PATH = props.getLogsPath();
        DATABASE_DRIVER = props.getDATABASE_DRIVER();
        DATABASE_IP = props.getDATABASE_IP();
        DATABASE_PORT = props.getDATABASE_PORT();
        DATABASE_NAME = props.getDATABASE_NAME();
        DATABASE_USER = props.getDATABASE_USER();
        DATABASE_PASSWORD = props.getDATABASE_PASSWORD();
        DATABASE_SERVER_TIME_ZONE = props.getDATABASE_SERVER_TIME_ZONE();
        SYSTEM_PORT = props.getSYSTEM_PORT();
        SYSTEM_HOST = props.getSYSTEM_HOST();

        SHORTCODE = props.getSHORTCODE();
        COMMAND_ID = props.getCOMMAND_ID();
        RESULT_URL = props.getRESULT_URL();
        TOKEN_URL = props.getTOKEN_URL();
        EXPRESS_URL = props.getEXPRESS_URL();
        CONSUMER_KEY = props.getCONSUMER_KEY();
        CONSUMER_SECRET = props.getCONSUMER_SECRET();
        PASS_KEY = props.getPASS_KEY();
        MINIMUM_AMOUNT = props.getMINIMUM_AMOUNT();
        // Deployment options
        DeploymentOptions options = new DeploymentOptions()
                .setInstances(1)
                .setWorkerPoolName("b2b - thread")
                .setWorker(true)
                .setWorkerPoolSize(30)
                .setHa(true);

        // deploy Vertices Here 
        vertx.deployVerticle(EntryPoint.class.getName(), options);
        vertx.deployVerticle(STKService.class.getName(), options);
        vertx.deployVerticle(ExpressServices.class.getName(), options);
        vertx.deployVerticle(FeeService.class.getName(), options);
    }

    @Override
    public void start(Future<Void> start_application) {

        EventBus eventBus = vertx.eventBus();
        int port = Integer.parseInt(SYSTEM_PORT);
        String host = SYSTEM_HOST;
        HttpServer ovHttpServer = vertx.createHttpServer(new HttpServerOptions().setSsl(true)
                .setKeyStoreOptions(new JksOptions().setPassword("changeit")
                        .setPath("SiriExpress/appconfig/keystore.jks")));
        ovHttpServer.requestHandler(request -> {
            HttpServerResponse response = request.response();
            response.headers()
                    .add("Content-Type", "application/json")
                    .add("Access-Control-Allow-Origin", "*")
                    .add("Access-Control-Allow-Headers", "*")
                    .add("Access-Control-Allow-Methods", "*")
                    .add("Access-Control-Allow-Credentials", "true");
            String method = request.rawMethod();
            String path = request.path();

            request.bodyHandler(bodyHandler -> {
                String body = bodyHandler.toString();
                JsonObject responseOBJ = new JsonObject();
                if ("POST".equalsIgnoreCase(method)) {
                    JsonObject data = new JsonObject(body);
                    if (path.endsWith("/express_channel")) {
                        try {
                            DeliveryOptions deliveryOptions = new DeliveryOptions()
                                    .setSendTimeout(20000);
                            System.out.println("data call : " + data);
                            data.put("processingCode", "STK");
                            String processingCode = data.getString("processingCode");
                            System.out.println("processingCode: " + processingCode);

                            String phonenumber = data.getString("PhoneNumber").trim();
                            String recipientPhonenumber = data.getString("AccountReference").trim();

                            if (phonenumber.length() == 12 && recipientPhonenumber.length() == 12) {
                                if (!StringUtils.containsWhitespace(phonenumber) && !StringUtils.containsWhitespace(recipientPhonenumber)) {
                                    eventBus.send(processingCode, data);
                                    responseOBJ.put("response_code", "00")
                                            .put("response", " received");
                                    response.end(responseOBJ.toString());
                                } else {
                                    responseOBJ.put("response_code", "00")
                                            .put("response", " the phonenumbers contains white spaces");
                                    response.end(responseOBJ.toString());
                                }
                            } else {
                                responseOBJ.put("response_code", "00")
                                        .put("response", " the phonenumbers are not fully formed");
                                response.end(responseOBJ.toString());
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            responseOBJ.put("response_code", "901")
                                    .put("response", "error occured || exception");
                            response.end(responseOBJ.toString());
                        }
                    } else if (path.endsWith("/express_callback")) {
                        System.out.println("data callback : " + data);

                        try {
                            DeliveryOptions deliveryOptions = new DeliveryOptions()
                                    .setSendTimeout(20000);

                            eventBus.send("EXPRESSCALLBACK", data);

                            responseOBJ.put("response_code", "00")
                                    .put("response", "received");
                            response.end(responseOBJ.toString());

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            responseOBJ.put("response_code", "901")
                                    .put("response", "error occured || exception");
                            response.end(responseOBJ.toString());
                        }

                    } else if (path.endsWith("/fetch_fee")) {
                        System.out.println("amount sent : " + data);
                        try {
                            DeliveryOptions deliveryOptions = new DeliveryOptions()
                                    .setSendTimeout(20000);
                            eventBus.send("Fee", data, deliveryOptions, sToBus -> {
                                if (sToBus.succeeded()) {
                                    JsonObject resobject = (JsonObject) sToBus.result().body();
                                    response.end(resobject.toString());
                                } else {
                                    // error
                                    responseOBJ.put("response_code", "999")
                                            .put("responseDescription", "Fee" + " failed")
                                            .put("error_data", sToBus.cause().getLocalizedMessage());
                                    response.end(responseOBJ.toString());

                                }
                            });

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            responseOBJ.put("response_code", "901")
                                    .put("response", "error occured || exception");
                            response.end(responseOBJ.toString());
                        }

                    } else {
                        // Unknown path
                        responseOBJ.put("response_code", "404")
                                .put("response", "Invalid path");
                        response.end(responseOBJ.toString());
                    }

                } else if (path.endsWith("/fetch_min_amount")) {
                    // wrong request method
                    responseOBJ.put("response_code", "00")
                            .put("minimum_amount", MINIMUM_AMOUNT);
                    response.end(responseOBJ.toString());

                } else {
                    // wrong request method
                    responseOBJ.put("response_code", "901")
                            .put("response", "Bad Request");
                    response.end(responseOBJ.toString());
                }
            });
        });
        ovHttpServer.listen(port, resp -> {
            if (resp.succeeded()) {
                System.out.println("System listening at " + host + ":" + port);
            } else {
                System.out.println("System failed to start !!" + resp.failed());
            }
        });

    }
}
