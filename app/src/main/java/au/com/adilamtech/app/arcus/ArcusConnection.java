package au.com.adilamtech.app.arcus;

import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import org.apache.commons.lang.ArrayUtils;
import org.jboss.netty.channel.ChannelException;
import org.projectodd.stilts.stomp.DefaultHeaders;
import org.projectodd.stilts.stomp.DefaultStompMessage;
import org.projectodd.stilts.stomp.StompException;
import org.projectodd.stilts.stomp.StompMessage;
import org.projectodd.stilts.stomp.StompMessages;
import org.projectodd.stilts.stomp.Subscription;
import org.projectodd.stilts.stomp.client.ClientSubscription;
import org.projectodd.stilts.stomp.client.ClientTransaction;
import org.projectodd.stilts.stomp.client.MessageHandler;
import org.projectodd.stilts.stomp.client.StompClient;

import java.net.URISyntaxException;
import java.nio.channels.UnresolvedAddressException;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLException;

/**
 * Created by Gabriel on 2/1/16.
 */
public class ArcusConnection implements Runnable, MessageHandler {

    private String stompMsg;
    private String classname;
    private final String ArcusServerAddress;
    private final String ArcusAccount;
    private final String ArcusLocation;
    private final String ArcusStation;
    private final ArcusConnectionEventListener callback;
    private final String ArcusPublishAddress;
    private final String ArcusSubscribeAddress;
    private final String JMSType = "StocktakeData";

    public ArcusConnection(String server, String publish, String account, String location, String station, String msg, ArcusConnectionEventListener cb) {

        this.stompMsg = msg;
        this.callback = cb;
        this.ArcusServerAddress = server;
        this.ArcusAccount = account;
        this.ArcusLocation = location;
        this.ArcusStation = station;
        this.ArcusPublishAddress = publish;
        this.ArcusSubscribeAddress = "listener." + ArcusAccount + "." + ArcusLocation + "." + ArcusStation;
        this.classname = ArcusConnection.class.getSimpleName();
    }

    @Override
    public void run() {
        uploadInventory(stompMsg);
    }

    private void uploadInventory(String msg) {
        StompClient client;
        ClientSubscription consumer;
        ClientTransaction producer;
        DefaultHeaders arcusHeaders;
        DefaultStompMessage stompmsg;

        try {
            client = new StompClient(ArcusServerAddress);

            client.connect();

            consumer = client.subscribe(ArcusSubscribeAddress)
                    .withHeader("JMSType", JMSType)
                    .withHeader("isAdMessage", "true")
                    .withMessageHandler(this)
                    .withAckMode(Subscription.AckMode.AUTO)
                    .start();

            producer = client.begin();

            arcusHeaders = new DefaultHeaders();
            arcusHeaders.put("JMSType", JMSType);
            arcusHeaders.put("tenant", ArcusAccount);
            arcusHeaders.put("isAdMessage", "true");


            stompmsg = (DefaultStompMessage) StompMessages.createStompMessage(ArcusPublishAddress, arcusHeaders, msg);
            stompmsg.setContentType("application/json");
            producer.send(stompmsg);
            producer.commit();
            client.disconnect();

            callback.onCompletion();
            Log.i(classname, String.format(Locale.US, "EVENT. uploadInventory(message [%s]) over Stomp", msg));

        } catch (Exception e) {
            Log.e("ArcusConnection", "ERROR. Exception", e);
            callback.onException(e);
        } /*catch(ChannelException e) {
            Log.e("ArcusConnection", "ERROR. ChannelException", e);
            callback.onException(e);
        } catch (URISyntaxException e) {
            Log.e("ArcusConnection", "ERROR. URISyntaxException", e);
            callback.onException(e);
        } catch (InterruptedException e) {
            Log.e("ArcusConnection", "ERROR. InterruptedException", e);
            callback.onException(e);
        } catch (TimeoutException e) {
            Log.e("ArcusConnection", "ERROR. TimeoutException", e);
            callback.onException(e);
        } catch (StompException e) {
            Log.e("ArcusConnection", "ERROR. StompException", e);
            callback.onException(e);
        } catch (SSLException e) {
            Log.e("ArcusConnection", "ERROR. SSLException", e);
            callback.onException(e);
        } catch (UnresolvedAddressException e) {
            Log.e("ArcusConnection", "ERROR. UnresolvedAddressException", e);
            callback.onException(e);
        }*/

    }

    @Override
    public void handle(StompMessage message) {
        Log.i(classname, String.format(Locale.US, "EVENT. handle(message [%s]) over Stomp", message.toString()));
    }
}



