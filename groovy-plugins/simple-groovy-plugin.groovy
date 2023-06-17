import groovy.swing.SwingBuilder
import org.jetbrains.annotations.NotNull
import ru.ezhov.rocket.action.api.*
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.support.AbstractRocketAction

import javax.swing.*
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.List

class SimpleGroovyRocketActionUi extends AbstractRocketAction implements RocketActionPlugin {
    private def context = null

    @Override
    String name() { return "A simple action written in groovy to display" }

    @Override
    String description() {
        return """This action is located in the folder ${new File(".").absolutePath}"""
    }

    @Override
    List<String> asString() {
        return ["KEY"]
    }

    @Override
    List<String> properties() {
        return [
            createRocketActionProperty(
                "KEY",
                "KEY_NAME",
                "TEST_DESCRIPTION",
                true,
                new RocketActionPropertySpec.StringPropertySpec("DEFAULT"),
            )
        ]
    }

    @Override
    Icon icon() {
        return null
    }

    @Override
    RocketAction create(@NotNull RocketActionSettings settings, @NotNull RocketActionContext context) {
        return new RocketAction() {

            @Override
            boolean contains(@NotNull String search) {
                return false
            }

            @Override
            boolean isChanged(@NotNull RocketActionSettings actionSettings) {
                return false
            }

            @Override
            Component component() {
                def text = SimpleGroovyRocketActionUi.this.description()
                def counter = 0

                return new SwingBuilder().label(text: SimpleGroovyRocketActionUi.this.description()).with {
                    it.addMouseListener(new MouseAdapter() {
                        @Override
                        void mouseClicked(MouseEvent e) {
                            counter++
                            it.text = "$counter - $text"
                        }
                    })

                    it
                }
            }
        }
    }

    @Override
    RocketActionType type() {
        return new RocketActionType() {

            @Override
            String value() {
                return "SIMPLE_GROOVY_PLUGIN"
            }
        }
    }

    @Override
    RocketActionFactoryUi factory(RocketActionContext context) {
        this.context = context
        return this
    }

    @Override
    RocketActionConfiguration configuration(RocketActionContext context) {
        this.context = context
        return this
    }
}

return new SimpleGroovyRocketActionUi()
