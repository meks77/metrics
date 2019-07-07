package at.meks.metrics.demo.jee8;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:build/cucumber"}, monochrome = true, strict = true,
        features = "src/test/resources", glue = {"at.meks.metrics.bdd.tests.common", "at.meks.metrics.demo.jee8"})
public class CucumberTest {

}
