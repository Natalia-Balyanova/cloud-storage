package ru.balyanova.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientPropertiesReceiver {
    private static final String pathToProperties = "client/src/main/resources/client.properties";
    private static final Properties properties = new Properties();

    private static String getProperties (String propertyName){
        try (InputStream in = new FileInputStream(pathToProperties)){
            properties.load(in);
            return properties.getProperty(propertyName);
        }catch (IOException e){
            throw new IllegalArgumentException(propertyName + " is none");
        }
    }

    public static String getHost (){
        return ClientPropertiesReceiver.getProperties("host");
    }

    public static int getPort (){
        return Integer.parseInt(ClientPropertiesReceiver.getProperties("port").trim());
    }

    public static String getClientDirectory (){
        return ClientPropertiesReceiver.getProperties("clientDirectory");
    }


}
