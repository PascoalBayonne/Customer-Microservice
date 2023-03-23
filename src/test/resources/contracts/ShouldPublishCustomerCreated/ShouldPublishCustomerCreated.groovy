package contracts.ShouldPublishCustomerCreated

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("should produce customerCreated 'event' when customer is created")
    label("shouldPublishCustomerCreated_label")
    input {
        triggeredBy("shouldPublishCustomerCreated()")
    }
    outputMessage {
        sentTo("customer-topic")
        body(file("shouldPublishCustomerCreated.json"))
    }
}