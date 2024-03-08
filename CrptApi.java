package org.rzs.selsup.test.task;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;


public class CrptApi {

    private static final String BASE_URL = "https://ismp.crpt.ru/";
    private static final String BASE_URN = "api/v3/lk/documents/create";
    private static final Logger log = Logger.getLogger(CrptApi.class.getName());
    private int limit;
    private AtomicInteger limitCounter;
    private TimeUnit timeUnit;

    public CrptApi(TimeUnit timeUnit, int limit) {
        this.limitCounter = new AtomicInteger(0);
        this.timeUnit = timeUnit;
        this.limit = limit;

        Executors.newSingleThreadExecutor().execute(this::timeLoop);
    }

    private void timeLoop() {
        while (true){
            try {
                timeUnit.sleep(1);
                limitCounter.set(0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public AtomicInteger getLimitCounter() {
        return limitCounter;
    }

    public synchronized void sendRequest(String requestBody) throws Exception {
        //0 -> 1 -> 2 ... limit
        if (limitCounter.get() >= limit){
            throw new Exception("Connection limit reached");
        }

        //request sending asynchronously
        Executors.newCachedThreadPool().submit(()->{
            RestClient restClient = RestClient.create();
            URI uri = null;
            try {
                uri = new URI(BASE_URL + BASE_URN);
            } catch (URISyntaxException e) {
                log.warning("Wrong syntax of URI: " + uri);
            }
            ResponseEntity<String> response = restClient.post().
                    uri(uri).
                    contentType(MediaType.APPLICATION_JSON).
                    body(requestBody).
                    retrieve().
                    toEntity(String.class);
            });

        log.info("Request sent");
        limitCounter.incrementAndGet();
    }


}





