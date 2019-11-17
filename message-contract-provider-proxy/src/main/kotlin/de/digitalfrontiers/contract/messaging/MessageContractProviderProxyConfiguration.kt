package de.digitalfrontiers.contract.messaging

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Sink

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableBinding(Sink::class)
class MessageContractProviderProxyConfiguration
