package net.bridgesapis.bungeebridge.services;

import java.util.HashMap;

/**
 * @author zyuiop
 */
public class ServiceManager {
    protected static HashMap<Class<? extends Service>, Service> serviceHashMap = new HashMap<>();

    public static <T extends Service> void registerService(Class<T> serviceInterface, T serviceInstance) {
        serviceHashMap.put(serviceInterface, serviceInstance);
    }

    public static <T extends Service> T getService(Class<T> serviceInterface) {
        return (T) serviceHashMap.get(serviceInterface);
    }

    public static boolean hasService(Class<? extends Service> serviceInterface) {
        return serviceHashMap.containsKey(serviceInterface);
    }
}
