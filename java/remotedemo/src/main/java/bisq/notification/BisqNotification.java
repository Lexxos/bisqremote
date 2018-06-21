package bisq.notification;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.sun.xml.internal.rngom.parse.host.Base;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.concurrent.PushNotificationFuture;
import static java.nio.charset.StandardCharsets.*;

import java.io.*;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class BisqNotification extends BisqNotifcationObject {
    private BisqToken bisqToken;
    private BisqKey bisqKey;
    public BisqNotification(BisqToken t, BisqKey k) {
        super();
        bisqToken = t;
        bisqKey = k;
    }


    public void send(Boolean encrypt) {
        try {
            ApnsClient apnsClient;
            apnsClient = new ApnsClientBuilder()
                    .setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                    .setClientCredentials(new File("/Users/joachim/SpiderOak Hive/keys/push_certificate.production.p12"), "")
                    .build();

            PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse = null;
            SimpleApnsPushNotification pushNotification;

            ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
            payloadBuilder.setAlertBody("Bisq notifcation");
            Gson gson = new Gson();
            BisqNotifcationObject mini = new BisqNotifcationObject(this);
            String json = gson.toJson(mini);
            byte[] ptext = json.getBytes(ISO_8859_1);
            json = new String(ptext, UTF_8);

            if (encrypt) {
                payloadBuilder.addCustomProperty("encrypted", bisqKey.encryptBisqMessage(json));
            } else {
                payloadBuilder.addCustomProperty("bisqNotification", json);
            }

            final String payload = payloadBuilder.buildWithDefaultMaximumLength();
            final String token = bisqToken.asHex();

            pushNotification = new SimpleApnsPushNotification(token, bisqToken.bundleidentifier, payload);

            PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>>
                    sendNotificationFuture = apnsClient.sendNotification(pushNotification);

            pushNotificationResponse = sendNotificationFuture.get();
            if (pushNotificationResponse.isAccepted()) {
                System.out.println("Push notification accepted by APNs gateway.");
            } else {
                System.out.println("Notification rejected by the APNs gateway: " +
                        pushNotificationResponse.getRejectionReason());

                if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
                    System.out.println("\t…and the token is invalid as of " +
                            pushNotificationResponse.getTokenInvalidationTimestamp());
                }
            }
        } catch (final ExecutionException e) {
            System.err.println("Failed to send push notification.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
