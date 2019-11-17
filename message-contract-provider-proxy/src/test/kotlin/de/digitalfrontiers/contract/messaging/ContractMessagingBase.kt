package de.digitalfrontiers.contract.messaging

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.google.api.gax.paging.Page
import com.google.cloud.storage.*
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.integration.channel.QueueChannel
import org.springframework.messaging.Message
import org.springframework.messaging.PollableChannel
import org.springframework.messaging.SubscribableChannel
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.event.EventPublishingTestExecutionListener
import org.springframework.test.context.event.annotation.AfterTestClass

@SpringBootTest
@Import(ContractMessagingBase.GcpMessagingConfiguration::class)
@AutoConfigureMessageVerifier
@TestExecutionListeners(
        EventPublishingTestExecutionListener::class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
abstract class ContractMessagingBase {

    interface MessageSelector {

        fun fetchNextMessageMatching(
                maxTotalMilliseconds: Long = 10000,
                receiveWaitMilliseconds: Long = 500,
                matcher: (headers: Map<String, Any?>) -> Boolean)
    }

    @TestConfiguration
    class GcpMessagingConfiguration(
            val storage: Storage,
            @Value("\${storage.bucket}") val bucket: String
    ) {

        @Bean("uploads")
        fun contractsChannel() = QueueChannel()

        @Bean
        fun messageSelector(input: SubscribableChannel) =
                QueueChannel()
                        .also { channel -> input.subscribe { msg -> channel.send(msg) } }
                        .run {
                            object : MessageSelector {
                                override fun fetchNextMessageMatching(maxTotalMilliseconds: Long, receiveWaitMilliseconds: Long, matcher: (Map<String, Any?>) -> Boolean) {
                                    val started = System.currentTimeMillis()

                                    tailrec fun PollableChannel.receiveMatchingMessage(): Message<*> =
                                            receive(receiveWaitMilliseconds)
                                                    ?.takeIf { matcher(it.headers) }
                                                    ?: when {
                                                        (System.currentTimeMillis() - started) > maxTotalMilliseconds ->
                                                            fail { "Matching message not received within max $maxTotalMilliseconds milliseconds." }
                                                        else -> receiveMatchingMessage()
                                                    }

                                    receiveMatchingMessage().run(contractsChannel()::send)
                                }
                            }
                        }

        @AfterTestClass
        fun cleanUpBucket() {
            tailrec fun Page<Blob>.doWithRecursively(block: (BlobId) -> Unit) {
                iterateAll()
                        .map(Blob::getBlobId)
                        .forEach(block)

                if (hasNextPage())
                    nextPage.doWithRecursively(block)
            }

            storage
                    .batch()
                    .also { batch ->
                        storage
                                .list(bucket)
                                .doWithRecursively { batch.delete(it) }
                    }
                    .run(StorageBatch::submit)
        }
    }

    @Autowired
    lateinit var messageSelector: MessageSelector

    @Autowired
    lateinit var storage: Storage

    @Value("\${storage.bucket}")
    lateinit var bucket: String

    fun validateBucketId(receivedBucket: String) {
        assertThat(receivedBucket).isEqualTo(bucket)
    }

    fun createStorageObject() {
        val objectId = "users/0tLZoJRcxvh8Q0im8DXSSTAek4Q2/uploads/c78f0017-42c8-4042-a210-abdef557a046"

        storage
                .create(
                        BlobInfo
                                .newBuilder(bucket, objectId)
                                .setContentType("image/jpeg")
                                .setMetadata(mapOf("originalFilename" to "image01.jpg"))
                                .build(),
                        "DUMMY CONTENT".toByteArray(),
                        Storage.BlobTargetOption.doesNotExist())

        messageSelector.fetchNextMessageMatching {
            it["eventType"] == "OBJECT_FINALIZE" && it["objectId"] == objectId
        }
    }

    fun deleteStorageObject() {
        val objectId = "users/0tLZoJRcxvh8Q0im8DXSSTAek4Q2/uploads/607d3b39-47aa-4f3b-bdcd-386de5c8e142"

        storage
                .create(
                        BlobInfo
                                .newBuilder(bucket, objectId)
                                .setContentType("application/pdf")
                                .setMetadata(mapOf("originalFilename" to "sample.PDF"))
                                .build(),
                        "DUMMY CONTENT".toByteArray(),
                        Storage.BlobTargetOption.doesNotExist())
                .delete()

        messageSelector.fetchNextMessageMatching {
            it["eventType"] == "OBJECT_DELETE" && it["objectId"] == objectId
        }
    }
}
