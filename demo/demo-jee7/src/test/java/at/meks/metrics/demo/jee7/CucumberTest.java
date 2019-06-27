package at.meks.metrics.demo.jee7;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = "pretty", monochrome = true, strict = true, features = "src/test/resources")
public class CucumberTest {

}
