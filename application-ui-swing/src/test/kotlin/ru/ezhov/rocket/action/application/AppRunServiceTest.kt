package ru.ezhov.rocket.action.application

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.startup.domain.StartupActionsRunStarter
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunTrigger
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource

internal class AppRunServiceTest {
    private var fontDefaults: Map<Any, FontUIResource> = emptyMap()

    /**
     * The service rewrites every FontUIResource default, so the UI defaults are restored
     * after the test; otherwise other tests in the same JVM see a broken font.
     */
    @BeforeEach
    fun captureUiFonts() {
        fontDefaults = UIManager.getDefaults()
            .filterValues { it is FontUIResource }
            .mapValues { it.value as FontUIResource }
    }

    @AfterEach
    fun restoreUiFonts() {
        fontDefaults.forEach { (key, font) -> UIManager.put(key, font) }
    }

    @Test
    fun `should start startup actions after the main dialog becomes visible`() {
        var visible = false
        var startedAfterVisible = false
        val triggers = mutableListOf<StartupRunTrigger>()

        val dialogMock = mockk<BaseDialog>(relaxed = true)
        every { dialogMock.isVisible = true } answers { visible = true }

        val baseDialogFactory = mockk<BaseDialogFactory> {
            every { dialog } returns dialogMock
        }
        val chainActionDialogFactory = mockk<ChainActionDialogFactory>(relaxed = true)
        // No font is configured, so the service must not touch the UI defaults.
        val properties = mockk<GeneralPropertiesRepository>(relaxed = true) {
            every { asStringOrNull(UsedPropertiesName.FONT_NAME) } returns null
            every { asIntegerOrNull(UsedPropertiesName.FONT_STYLE) } returns null
            every { asIntegerOrNull(UsedPropertiesName.FONT_SIZE) } returns null
        }

        val starter = object : StartupActionsRunStarter {
            override fun startAsync(trigger: StartupRunTrigger) {
                triggers += trigger
                startedAfterVisible = visible
            }
        }

        AppRunService(
            generalPropertiesRepository = properties,
            baseDialogFactory = baseDialogFactory,
            chainActionDialogFactory = chainActionDialogFactory,
            startupActionsRunStarter = starter,
        ).run(emptyArray())

        assertThat(triggers).containsExactly(StartupRunTrigger.STARTUP)
        assertThat(startedAfterVisible).isTrue
    }
}
