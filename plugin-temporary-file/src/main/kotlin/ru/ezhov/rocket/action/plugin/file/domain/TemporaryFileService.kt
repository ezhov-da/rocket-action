package ru.ezhov.rocket.action.plugin.file.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.io.File
import java.nio.charset.Charset

class TemporaryFileService {
    fun createTemporaryFile(
        text: String,
        encoding: String = "UTF-8",
        extension: String = ".txt"
    ): Either<TemporaryFileServiceException, File> =
        File.createTempFile("tmp", extension)
            .let { file ->
                try {
                    file.bufferedWriter(charset = Charset.forName(encoding)).use { bw ->
                        bw.write(text)
                    }
                    file.right()
                } catch (ex: Exception) {
                    TemporaryFileServiceException("Error when write temporary file", ex).left()
                }
            }
}
