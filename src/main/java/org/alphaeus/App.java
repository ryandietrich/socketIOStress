package org.alphaeus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

public class App {
    public static AtomicLong successCount = new AtomicLong(0);
    public static AtomicLong counter = new AtomicLong(5);
    public static AtomicLong connectionCount = new AtomicLong(5);
    public static Random r = new Random();

    public static void createSocket( final int id ) {
        IO.Options opts = new IO.Options();
        opts.forceNew = true;

        try {
            final Socket sck = IO.socket("http://localhost:9092", opts);

            sck.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    successCount.incrementAndGet();

                    sck.close();
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if ( successCount.get() <= counter.get() ) {
                        // Adding a little rate limiting helps, but we're not hitting this *that* hard
                        /*
                        try {
                            Thread.sleep(r.nextInt(300 + 50));
                        } catch ( java.lang.InterruptedException iex ) {
                        }
                        */
                        sck.connect();
                    }
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("Error! " + new ArrayList(Arrays.asList(args)));
                }
            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("Connect error! " + new ArrayList(Arrays.asList(args)));
                }
            }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("Connect timeout! " + new ArrayList(Arrays.asList(args)));
                }
            }).on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("Reconnect " + new ArrayList(Arrays.asList(args)));
                }
            }).on(Socket.EVENT_RECONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("Reconnect ERROR! " + new ArrayList(Arrays.asList(args)));
                }
            }).on(Socket.EVENT_RECONNECT_FAILED, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("Reconnect FAILED! " + new ArrayList(Arrays.asList(args)));
                }
            }).on(Socket.EVENT_RECONNECT_ATTEMPT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("Reconnect Attempt! " + new ArrayList(Arrays.asList(args)));
                }
            }).on(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("Reconnecting! " + new ArrayList(Arrays.asList(args)));
                }
            });
            sck.connect();

        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    public static void main(String... args ) throws Exception {
        if ( args.length > 0 )
            connectionCount.set(Long.parseLong(args[0]));

        if ( args.length > 1 )
            counter.set(Long.parseLong(args[1]));

        System.out.println("Going to create " + counter + " connections to the SocketIO server");

        long start = System.currentTimeMillis();

        for ( int ii = 0; ii < connectionCount.get(); ii++ )
            App.createSocket(ii);

        System.out.println("Done creating connections");

        while ( successCount.get() < counter.get() ) {
            System.out.println(new Date() + " " + successCount.get() + " of " + counter + " completed");
            Thread.sleep(100);
        }
        System.out.println(new Date() + " " + successCount.get() + " of " + counter + " completed");
        System.out.println("Done: elapsed=" + (System.currentTimeMillis() - start) + "ms");
        System.exit(0);
    }
}
