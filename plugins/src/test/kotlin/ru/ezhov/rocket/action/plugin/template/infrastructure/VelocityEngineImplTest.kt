package ru.ezhov.rocket.action.plugin.template.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VelocityEngineImplTest {
    @Test
    fun `should return worlds`() {
        val velocityEngineImpl = VelocityEngineImpl()
        val text = """
            _>test
            _> test1
            _>test2

            test
        """.trimIndent()

        val words = velocityEngineImpl.words(text)

        assertThat(words)
            .isEqualTo(
                listOf(
                    "test", "test1",
                    "test2"
                )
            )
    }

    @Test
    fun `should apply template`() {
        val velocityEngineImpl = VelocityEngineImpl()
        val text =
            """
            _>test
            _> test1
            _>test2
            ${'$'}test1 ${'$'}test2 ${'$'}test3
            """.trimIndent()

        val result = velocityEngineImpl
            .apply(
                template = text,
                values = mapOf(
                    "test1" to "1",
                    "test2" to "2",
                    "test3" to "3",
                ))

        assertThat(result)
            .isEqualTo("1 2 3")
    }

    @Test
    fun `should apply template2`() {
        val velocityEngineImpl = VelocityEngineImpl()
        val text =
            """
            _>ids
            #foreach( ${'$'}id in ${'$'}ids.split(",") )
            ${'$'}id
            #end
            """.trimIndent()

        val result = velocityEngineImpl
            .apply(
                template = text,
                values = mapOf(
                    "ids" to "1234567",
                ))

        assertThat(result)
            .isEqualTo("1 2 3")
    }


}
