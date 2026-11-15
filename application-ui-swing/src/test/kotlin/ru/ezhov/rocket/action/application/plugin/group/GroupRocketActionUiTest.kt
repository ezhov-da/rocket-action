package ru.ezhov.rocket.action.application.plugin.group

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.application.core.application.ActionReloadScope
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionCached
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionCachedState
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.core.infrastructure.RocketActionComponentCacheFactory
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.event.infrastructure.InMemoryDomainEventPublisher
import javax.swing.JMenu
import javax.swing.JMenuItem

internal class GroupRocketActionUiTest {
    private val cache = RocketActionComponentCacheFactory.cache

    @AfterEach
    fun cleanUp() {
        cache.clear()
    }

    @Test
    fun `should consult the reload change set for child changes`() {
        cache.add(CHILD_ID, RocketActionCached.newRocketAction(childAction()))
        val context = mockk<RocketActionContext>(relaxed = true)
        val plugin = GroupRocketActionUi()
        plugin.factory(context)

        val scope = ActionReloadScope()
        val group = plugin.create(groupModel().to(scope), context)!!

        // unchanged reload: no child was rebuilt
        val unchangedScope = ActionReloadScope()
        assertThat(group.isChangedIn(unchangedScope, groupModel())).isFalse()

        // a rebuilt child forces its group to be recreated
        val changedScope = ActionReloadScope()
        changedScope.markChanged(CHILD_ID)
        assertThat(group.isChangedIn(changedScope, groupModel())).isTrue()
    }

    @Test
    fun `should fall back to the cached child state outside a reload`() {
        cache.add(CHILD_ID, RocketActionCached.newRocketAction(childAction()))
        val context = mockk<RocketActionContext>(relaxed = true)
        val plugin = GroupRocketActionUi()
        plugin.factory(context)

        val group = plugin.create(groupModel().to(ActionReloadScope()), context)!!

        assertThat(group.isChanged(groupModel().to(ActionReloadScope()))).isTrue()

        cache.add(
            CHILD_ID,
            RocketActionCached(childAction(), RocketActionCachedState.NOT_CHANGED_SINCE_LAST_LOAD),
        )
        assertThat(group.isChanged(groupModel().to(ActionReloadScope()))).isFalse()
    }

    @Test
    fun `should not register a domain event subscriber per group`() {
        val subscriberCountBefore = subscriberCount()

        val context = mockk<RocketActionContext>(relaxed = true)
        repeat(3) {
            GroupRocketActionUi().factory(context)
        }

        assertThat(subscriberCount()).isEqualTo(subscriberCountBefore)
    }

    @Test
    fun `should tolerate a missing child and count one failure`() {
        val context = mockk<RocketActionContext>(relaxed = true)
        val plugin = GroupRocketActionUi()
        plugin.factory(context)
        val scope = ActionReloadScope()

        val menu = ActionReloadScope.withCurrent(scope) {
            plugin.create(groupModel(childIds = listOf("missing")).to(scope), context)
        }!!.component() as JMenu

        assertThat(menu.itemCount).isZero()
        assertThat(scope.failures).isEqualTo(1)
    }

    @Test
    fun `should keep the available children of a group with a missing child`() {
        cache.add(CHILD_ID, RocketActionCached.newRocketAction(childAction()))
        val context = mockk<RocketActionContext>(relaxed = true)
        val plugin = GroupRocketActionUi()
        plugin.factory(context)
        val scope = ActionReloadScope()

        val menu = ActionReloadScope.withCurrent(scope) {
            plugin.create(groupModel(childIds = listOf(CHILD_ID, "missing")).to(scope), context)
        }!!.component() as JMenu

        assertThat(menu.itemCount).isEqualTo(1)
        assertThat(scope.failures).isEqualTo(1)
    }

    private fun RocketAction.isChangedIn(scope: ActionReloadScope, model: RocketActionSettingsModel): Boolean =
        ActionReloadScope.withCurrent(scope) { isChanged(model.to(scope)) }

    private fun groupModel(childIds: List<String> = listOf(CHILD_ID)) = RocketActionSettingsModel(
        id = GROUP_ID,
        type = GroupRocketActionUi.TYPE,
        settings = listOf(SettingsModel(name = "label", value = "Group")),
        actions = childIds.map { id ->
            RocketActionSettingsModel(
                id = id,
                type = "TEST",
                settings = emptyList(),
                actions = emptyList(),
                tags = emptyList(),
            )
        },
        tags = emptyList(),
    )

    private fun childAction() = object : RocketAction {
        override fun contains(search: String): Boolean = false
        override fun isChanged(actionSettings: ru.ezhov.rocket.action.api.RocketActionSettings): Boolean = false
        override fun component(): java.awt.Component = JMenuItem("child")
    }

    private fun subscriberCount(): Int {
        val publisher = DomainEventFactory.publisher as InMemoryDomainEventPublisher
        val field = InMemoryDomainEventPublisher::class.java
            .getDeclaredField("eventSubscribers")
            .apply { isAccessible = true }
        val subscribers = field.get(publisher) as Map<*, *>
        return subscribers.values.sumOf { (it as Set<*>).size }
    }

    private companion object {
        const val GROUP_ID = "group"
        const val CHILD_ID = "child"
    }
}
