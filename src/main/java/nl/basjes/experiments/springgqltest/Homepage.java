package nl.basjes.experiments.springgqltest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Homepage {
    @GetMapping(
        path = "/",
        produces = MediaType.TEXT_HTML_VALUE
    )
    public String homepage() {
        return "Test thing: <br/>" +
                "<a href=\"/graphiql\">Graphiql</a><br/>" +
                "<a href=\"/graphql/schema\">Schema</a><br/>" +
                "<a href=\"/graphql\">Endpoint</a><br/>";
    }

}
