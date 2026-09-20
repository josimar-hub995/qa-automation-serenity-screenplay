package pe.com.challenge.automation.interactions;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;
import pe.com.challenge.automation.exceptions.FrameworkException;
import pe.com.challenge.automation.utilities.WaitUtility;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isClickable;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

/** Esperas explícitas con mensajes funcionales listos para los reportes de fallo. */
public class WaitFor implements Interaction {
    private enum Condition { VISIBLE, CLICKABLE }

    private final Target target;
    private final Condition condition;
    private final String context;

    public WaitFor(Target target, Condition condition, String context) {
        this.target = target;
        this.condition = condition;
        this.context = context;
    }

    public static WaitFor visible(Target target, String context) {
        return Tasks.instrumented(WaitFor.class, target, Condition.VISIBLE, context);
    }

    public static WaitFor clickable(Target target, String context) {
        return Tasks.instrumented(WaitFor.class, target, Condition.CLICKABLE, context);
    }

    @Override
    @Step("{0} espera que #target esté disponible: #context")
    public <T extends Actor> void performAs(T actor) {
        int timeout = (int) WaitUtility.explicitTimeout().toSeconds();
        try {
            if (condition == Condition.CLICKABLE) {
                actor.attemptsTo(WaitUntil.the(target, isClickable())
                        .forNoMoreThan(timeout).seconds());
            } else {
                actor.attemptsTo(WaitUntil.the(target, isVisible())
                        .forNoMoreThan(timeout).seconds());
            }
        } catch (RuntimeException waitError) {
            throw new FrameworkException(
                    "Espera fallida: %s | Elemento: %s | Condición: %s | Timeout: %d s"
                            .formatted(context, target.getName(), condition.name(), timeout),
                    waitError);
        }
    }
}
