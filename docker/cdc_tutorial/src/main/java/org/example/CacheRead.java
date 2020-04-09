package org.example;

import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;

public class CacheRead {

    public static void main(String[] args) {
        JetInstance instance = Jet.newJetClient();

        System.out.println("Currently there are following customers in the cache:");
        instance.getMap("customers").values().forEach(c -> System.out.println("\t" + c));

        instance.shutdown();
    }
}
