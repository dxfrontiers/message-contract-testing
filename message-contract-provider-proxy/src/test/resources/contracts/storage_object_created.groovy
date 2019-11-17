import org.springframework.cloud.contract.spec.Contract

Contract.make {
    label 'storage_object_created'
    description("""
        Message sent, when a new object has been created (uploaded) to Cloud Storage.
        See: https://cloud.google.com/storage/docs/pubsub-notifications#events
    """)

    input {
        triggeredBy('createStorageObject()')
    }

    outputMessage {
        sentTo('uploads')
        headers {
            header('payloadFormat', 'JSON_API_V1')
            header('eventType', 'OBJECT_FINALIZE')
            header('bucketId', $(stub('cloud_storage_bucket'), test(execute('validateBucketId($it)'))))
            header('objectId', 'users/0tLZoJRcxvh8Q0im8DXSSTAek4Q2/uploads/c78f0017-42c8-4042-a210-abdef557a046')
            header('objectGeneration', $(stub(3298743298472), test(anyPositiveInt())))
        }
        body([
                size       : $(stub(1024234), test(anyPositiveInt())),
                contentType: "image/jpeg",
                metadata   : [
                        originalFilename: "image01.jpg"
                ]
        ])
    }
}
