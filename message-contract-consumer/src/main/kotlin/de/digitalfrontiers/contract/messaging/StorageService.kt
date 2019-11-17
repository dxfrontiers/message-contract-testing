package de.digitalfrontiers.contract.messaging

import org.springframework.stereotype.Service

@Service
class StorageService {

    fun handle(notification: StorageNotification) {
        println(notification)
    }
}