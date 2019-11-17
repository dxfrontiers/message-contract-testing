package de.digitalfrontiers.contract.messaging

import com.ninjasquad.springmockk.MockkBean
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.stubrunner.StubTrigger
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner

@SpringBootTest
@AutoConfigureStubRunner
class StorageNotificationSinkTest {

    @MockkBean(relaxed = true)
    lateinit var storageService: StorageService

    @Autowired
    lateinit var stubTrigger: StubTrigger

    @Test
    fun `can handle object creation notification`() {
        // trigger message by label as defined in contract
        stubTrigger.trigger("storage_object_created")

        verify(exactly = 1) {
            storageService.handle(
                    StorageNotification.Created(
                            bucket = "cloud_storage_bucket",
                            objectId = "users/0tLZoJRcxvh8Q0im8DXSSTAek4Q2/uploads/c78f0017-42c8-4042-a210-abdef557a046",
                            objectGeneration = 3298743298472,
                            size = 1024234,
                            contentType = "image/jpeg",
                            originalFilename = "image01.jpg"
                    )
            )
        }
    }

    @Test
    fun `can handle object deletion notification`() {
        // trigger message by label as defined in contract
        stubTrigger.trigger("storage_object_deleted")

        verify(exactly = 1) {
            storageService.handle(
                    StorageNotification.Removed(
                            bucket = "cloud_storage_bucket",
                            objectId = "users/0tLZoJRcxvh8Q0im8DXSSTAek4Q2/uploads/607d3b39-47aa-4f3b-bdcd-386de5c8e142",
                            objectGeneration = 234234345324,
                            size = 91232435,
                            contentType = "application/pdf",
                            originalFilename = "sample.PDF"
                    )
            )
        }
    }
}