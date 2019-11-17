package de.digitalfrontiers.contract.messaging

import de.digitalfrontiers.contract.messaging.StorageNotification.*
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class StorageNotificationSink(private val storageService: StorageService) {

    @StreamListener(Sink.INPUT, condition = "headers['payloadFormat']=='JSON_API_V1'")
    fun handleNotification(
            @Header("eventType") eventType: String,
            @Header("bucketId") bucketId: String,
            @Header("objectId") objectId: String,
            @Header("objectGeneration") objectGeneration: Long,
            @Header("overwroteGeneration") overwroteGeneration: Long?,
            @Header("overwrittenByGeneration") overwrittenByGeneration: Long?,
            @Payload payload: NotificationPayload) {

        when (eventType) {
            "OBJECT_FINALIZE" ->
                overwroteGeneration
                        ?.run {
                            CreatedOverwrote(
                                    bucketId,
                                    objectId,
                                    objectGeneration,
                                    overwroteGeneration,
                                    payload.size,
                                    payload.contentType,
                                    payload.metadata["originalFilename"]!!)
                        }
                        ?: Created(
                                bucketId,
                                objectId,
                                objectGeneration,
                                payload.size,
                                payload.contentType,
                                payload.metadata["originalFilename"]!!)
            "OBJECT_DELETE", "OBJECT_ARCHIVE" ->
                overwrittenByGeneration
                        ?.run {
                            RemovedOverwritten(
                                    bucketId,
                                    objectId,
                                    objectGeneration,
                                    overwrittenByGeneration,
                                    payload.size,
                                    payload.contentType,
                                    payload.metadata["originalFilename"]!!)
                        }
                        ?: Removed(
                                bucketId,
                                objectId,
                                objectGeneration,
                                payload.size,
                                payload.contentType,
                                payload.metadata["originalFilename"]!!)
            else ->
                Unknown
        }.run(storageService::handle)
    }

    data class NotificationPayload(
            val size: Long,
            val contentType: String,
            val metadata: Map<String, String>
    )
}

sealed class StorageNotification {
    object Unknown : StorageNotification()

    data class Created(
            val bucket: String,
            val objectId: String,
            val objectGeneration: Long,
            val size: Long,
            val contentType: String,
            val originalFilename: String
    ) : StorageNotification()

    data class CreatedOverwrote(
            val bucket: String,
            val objectId: String,
            val objectGeneration: Long,
            val overwroteGeneration: Long,
            val size: Long,
            val contentType: String,
            val originalFilename: String
    ) : StorageNotification()

    data class Removed(
            val bucket: String,
            val objectId: String,
            val objectGeneration: Long,
            val size: Long,
            val contentType: String,
            val originalFilename: String
    ) : StorageNotification()

    data class RemovedOverwritten(
            val bucket: String,
            val objectId: String,
            val objectGeneration: Long,
            val overwrittenByGeneration: Long,
            val size: Long,
            val contentType: String,
            val originalFilename: String
    ) : StorageNotification()
}
