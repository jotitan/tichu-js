package fr.tichu.service;

import java.io.*;

/**
 * User: Titan
 * Date: 26/04/14
 * Time: 23:25
 */
public class ObjectHelper {

    public static byte[] serialize(Object o){
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try{
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(o);
            return byteOut.toByteArray();
        }   catch(IOException ioex){
            ioex.printStackTrace();
            return null;
        }
    }

    public static <T> T deserialize(Class<T> clazz,byte[] value){

        try{
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(value));
            Object o = in.readObject();
            return clazz.cast(o);
        }   catch(Exception ioex){
            ioex.printStackTrace();
            return null;
        }
    }
}
