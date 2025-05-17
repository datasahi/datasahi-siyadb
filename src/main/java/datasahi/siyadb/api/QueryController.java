package datasahi.siyadb.api;


import datasahi.siyadb.query.QueryRequest;
import datasahi.siyadb.query.QueryService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;

@Controller("/siyadb/query")
public class QueryController {

    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @Produces(MediaType.TEXT_JSON)
    @Post("/execute")
    public String executeQuery(HttpRequest<?> request, @Body QueryRequest queryRequest) {
        return queryService.execute(queryRequest).toJsonString();
    }

}
