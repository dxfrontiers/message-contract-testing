import org.springframework.cloud.contract.spec.Contract

Contract.make {
    label 'storage_object_deleted'
    description("""
        Message sent, when an object has been deleted from Cloud Storage.
        See: https://cloud.google.com/storage/docs/pubsub-notifications#events
    """)

    input {
        triggeredBy('deleteStorageObject()')
    }

    outputMessage {
        sentTo('uploads')
        headers {
            header('payloadFormat', 'JSON_API_V1')
            header('eventType', 'OBJECT_DELETE')
            header('bucketId', $(stub('cloud_storage_bucket'), test(execute('validateBucketId($it)'))))
            header('objectId', 'users/0tLZoJRcxvh8Q0im8DXSSTAek4Q2/uploads/607d3b39-47aa-4f3b-bdcd-386de5c8e142')
            header('objectGeneration', $(stub(234234345324), test(anyPositiveInt())))
        }
        body([
                size       : $(stub(91232435), test(anyPositiveInt())),
                contentType: "application/pdf",
                metadata   : [
                        originalFilename: "sample.PDF"
                ]
        ])
    }
}
