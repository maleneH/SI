package com.example.mailservices;

/*
 * Topic Producer 4
 *
 * Sends a message and key words as a routingKey
 * Both the message load and the topic key words are entered as arguments of the application
 */

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.json.JsonParser;
import org.w3c.dom.ls.LSOutput;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

@SpringBootApplication
public class MailservicesApplication {
    //private final static String QUEUE_NAME = "helloqueue";
    // non-durable, exclusive, auto-delete queue with an automatically generated name
    private static String queueName = "message";
    private final static String EXCHANGE_NAME = "message-exchange";
    // routingKey is a composite topic of three words, concatenated with '.' given as args[1]
    private static String routingKey = "message-key";
    private static String message;
    private static ArrayList<String> messages = new ArrayList<>();


    public MailservicesApplication() throws Exception {
    }


    public static void main(String[] args) throws Exception {
        SpringApplication.run(MailservicesApplication.class, args);
        // args[0]-message; args[1]-topics
        // if (args.length > 0) message = args[0];
        //if (args.length > 1) routingKey = args[1];
        createMessages();
        createQueue(messages, routingKey);
        System.out.println(" [4] Sent routing key '" + routingKey + "' and message '" + message + "'");
    }

    public static void createQueue(ArrayList<String> messages, String rKey) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // queueName = channel.queueDeclare().getQueue();

            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            //  channel.queueDeclare(queueName, false, false, false, null);
            //   bind Exchange to queue
            //  channel.queueBind(queueName, EXCHANGE_NAME, "");

            for (String newMessage : messages) {
                channel.basicPublish(EXCHANGE_NAME, rKey, null, newMessage.getBytes("UTF-8"));

            }
        }
    }

    public static void createMessages() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(
                    new FileReader("./src/main/resources/GroupMembers.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder jsonString = new StringBuilder();
        String line = null;

        while (true) {
            try {
                if (!((line = br.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            jsonString.append(line);
        }

        Gson gson = new Gson();

        Type collectionType = new TypeToken<Collection<GroupMember>>() {
        }.getType();
        ArrayList<GroupMember> groupMembers = gson.fromJson(jsonString.toString(), collectionType);


        for (GroupMember m : groupMembers) {
            message = "Dear";
            if (Gender.getGender(m.name).contains("<male>true</male>")) {
                message = message + " Mr " + m.name;
                messages.add(message);
            } else {
                message = message + " Ms " + m.name;
                messages.add(message);
            }
        }
    }
}
