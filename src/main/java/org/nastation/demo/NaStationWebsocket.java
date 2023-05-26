package org.nastation.demo;

import org.apache.log4j.Logger;
import org.nachain.core.chain.structure.instance.CoreInstanceEnum;
import org.nachain.core.token.CoreTokenEnum;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @Deprecated
 * This example mainly demonstrates how to subscribe to new transactions of a specific instance through websocket
 * @author John | Nirvana Core
 * @since 12/06/2021
 */

@Deprecated
public class NaStationWebsocket {

    public static final String SERVER = "localhost";

    /* NaStation default port */
    public static final int PORT = 20902;

    private static Logger logger = Logger.getLogger(NaStationWebsocket.class);

    private final static WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

    public ListenableFuture<StompSession> connect() {

        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
        List<Transport> transports = Collections.singletonList(webSocketTransport);

        SockJsClient sockJsClient = new SockJsClient(transports);
        sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        String url = "ws://{host}:{port}/join";
        return stompClient.connect(url, headers, new DefaultHandler(), SERVER, PORT);
    }

    /**
     * You can do your business processing when the transaction of the nac instance is received
     * @param stompSession
     * @param instanceId
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void subscribeTx(StompSession stompSession, long instanceId) throws ExecutionException, InterruptedException {
        stompSession.subscribe("/topic/tx/" + instanceId, new StompFrameHandler() {

            public Type getPayloadType(StompHeaders stompHeaders) {
                return byte[].class;
            }

            public void handleFrame(StompHeaders stompHeaders, Object o) {

                String txJson = new String((byte[]) o);

                logger.info("instanceId: " + instanceId + " -> Received tx json: " + txJson);
            }
        });
    }

    public void subscribeWelcome(StompSession stompSession) throws ExecutionException, InterruptedException {
        stompSession.subscribe("/topic/welcome", new StompFrameHandler() {

            public Type getPayloadType(StompHeaders stompHeaders) {
                return byte[].class;
            }

            public void handleFrame(StompHeaders stompHeaders, Object o) {
                logger.info("Received welcome response: " + new String((byte[]) o));
            }
        });
    }

    private class DefaultHandler extends StompSessionHandlerAdapter {
        public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
            logger.info("Now connected");
        }
    }

    public void sendHello(StompSession stompSession) {
        String name = "john";
        stompSession.send("/app/join", name.getBytes());
    }

    static long nacTokenId = CoreTokenEnum.NAC.id;
    static long instanceId = CoreInstanceEnum.NAC.id;

    public static void main(String[] args) throws Exception {
        NaStationWebsocket client = new NaStationWebsocket();

        ListenableFuture<StompSession> f = client.connect();
        StompSession stompSession = f.get();

        logger.info("Subscribing to tx topic using session " + stompSession);

        CoreInstanceEnum[] values = CoreInstanceEnum.values();
        for (CoreInstanceEnum inst : values) {
            client.subscribeTx(stompSession, inst.id);
        }

        logger.info("Subscribing to welcome topic using session " + stompSession);
        client.subscribeWelcome(stompSession);

        logger.info("Sending hello message" + stompSession);
        client.sendHello(stompSession);

        Thread.sleep(30*1000);
    }

}
