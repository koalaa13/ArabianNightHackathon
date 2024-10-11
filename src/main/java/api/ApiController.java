package api;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.WorldInfo;
import model.request.Request;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class ApiController implements Controller {
    private static final String TOKEN = "66ffb00e5760466ffb00e57606";
    private static final String API_AUTH_HEADER = "X-Auth-Token";
    private static final String API_URL = "https://games.datsteam.dev";
    private static final String TEST_API_URL = "https://games-test.datsteam.dev";

    private final boolean isTest;
    private final ObjectMapper objectMapper;

    private static ApiController testInstance = null;

    private ApiController(boolean isTest) {
        this.isTest = isTest;
        this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ApiController getTestInstance() {
        if (testInstance == null) {
            testInstance = new ApiController(true);
        }
        return testInstance;
    }

    private static ApiController instance = null;
    public static ApiController getInstance() {
        if (instance == null) {
            instance = new ApiController(false);
        }
        return instance;
    }

    private String getApiUrl() {
        return isTest ? TEST_API_URL : API_URL;
    }

    private <T> T responseHandling(HttpResponse response, Class<? extends T> okResponseClass) throws IOException {
        var content = response.getEntity().getContent();
        if (response.getStatusLine().getStatusCode() != 200) {
            String errorMessage = objectMapper.readTree(content).get("error").asText();
            System.err.println("Code " + response.getStatusLine().getStatusCode() + ": " + errorMessage);
            return null;
        }
        return objectMapper.readValue(content, okResponseClass);
    }

    @Override
    public WorldInfo getInfo(Request requestDao) {
        final String url = getApiUrl();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            final String json = objectMapper.writeValueAsString(requestDao);
            final StringEntity entity = new StringEntity(json);

            HttpPost request = new HttpPost(url + "/play/magcarp/player/move");
            request.setHeader(API_AUTH_HEADER, TOKEN);
            request.setEntity(entity);

            return client.execute(request, response -> responseHandling(response, WorldInfo.class));
        } catch (IOException e) {
            System.err.println(e);
        }
        return null;
    }
}
