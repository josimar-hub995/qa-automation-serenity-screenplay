package pe.com.challenge.automation.interactions;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pe.com.challenge.automation.managers.ScenarioManager;
import pe.com.challenge.automation.exceptions.FrameworkException;
import pe.com.challenge.automation.utilities.WaitUtility;

public class EnterSearchQuery implements Interaction {
    private static final By SEARCH_INPUT = By.id("docsearch-input");
    private final String query;

    public EnterSearchQuery(String query) {
        this.query = query;
    }

    public static EnterSearchQuery value(String query) {
        return Tasks.instrumented(EnterSearchQuery.class, query);
    }

    @Override
    @Step("{0} escribe '#query' en el buscador de Selenium")
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        WebDriverWait wait = new WebDriverWait(driver, WaitUtility.explicitTimeout());
        try {
            WebElement input = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
            input.click();
            input.clear();
            input.sendKeys(query);

            if (!query.equals(input.getAttribute("value"))) {
                setValueAndDispatchInput(driver, input, query);
            }

            wait.until(ExpectedConditions.attributeToBe(SEARCH_INPUT, "value", query));
            ScenarioManager.log("Texto ingresado en DocSearch: " + query);
        } catch (RuntimeException inputError) {
            throw new FrameworkException(
                    "No se pudo ingresar la consulta '%s' en #docsearch-input dentro de %d s"
                            .formatted(query, WaitUtility.explicitTimeout().toSeconds()),
                    inputError);
        }
    }

    private static void setValueAndDispatchInput(WebDriver driver, WebElement input, String value) {
        ((JavascriptExecutor) driver).executeScript("""
                const element = arguments[0];
                const value = arguments[1];
                const setter = Object.getOwnPropertyDescriptor(
                    window.HTMLInputElement.prototype, 'value').set;
                setter.call(element, value);
                element.dispatchEvent(new InputEvent('input', {
                    bubbles: true,
                    inputType: 'insertText',
                    data: value
                }));
                element.dispatchEvent(new Event('change', { bubbles: true }));
                """, input, value);
    }
}
