package features;

import top.tslj.solon.App;

import org.junit.jupiter.api.Test;

import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonTest;

import java.io.IOException;

@SolonTest(App.class)
public class HelloTest extends HttpTester {
    @Test
    public void hello() throws IOException {
        assert path("/hello?name=world").get().contains("world");
        assert path("/hello?name=solon").get().contains("solon");
        assert path("/hello2?name=solon").get().contains("solon");
    }
}