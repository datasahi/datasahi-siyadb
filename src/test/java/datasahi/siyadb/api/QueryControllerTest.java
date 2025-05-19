package datasahi.siyadb.api;

import datasahi.siyadb.query.QueryRequest;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
public class QueryControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testExecuteQuery() throws Exception {
        // Prepare test data
        QueryRequest queryRequest = new QueryRequest()
                .setDatasource("s3csp")
                .setBucket("cspdata")
                .setFilepath("holdings.csv")
                .setFiletype("csv")
                .setQuery("SELECT * FROM s3csp_cspdata_holdings_csv LIMIT 5");

        // Send the request to the real endpoint with real QueryService
        HttpRequest<QueryRequest> request = HttpRequest.POST("/siyadb/query/execute", queryRequest);
        HttpResponse<String> response = client.toBlocking().exchange(request, String.class);

        // Verify the response status
        Assertions.assertEquals(200, response.status().getCode());

        // Verify the response is not null
        String responseBody = response.body();
        Assertions.assertNotNull(responseBody);

        System.out.println("responseBody :: " + responseBody);
    }
}