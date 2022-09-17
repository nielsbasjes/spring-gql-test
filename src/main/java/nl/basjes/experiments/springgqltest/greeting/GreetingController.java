package nl.basjes.experiments.springgqltest.greeting;


import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class GreetingController {

    @QueryMapping
    public String greeting() {
        return "Hello World";
    }

}
