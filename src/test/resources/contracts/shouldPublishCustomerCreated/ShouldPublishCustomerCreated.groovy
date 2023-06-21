package contracts.shoulPublishCustomerCreated

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("should publish customer created event")
    label("shouldPublishCustomerCreated")

    input {
        triggeredBy("shouldPublishCustomerCreated()")
    }

    outputMessage {
        sentTo("customer-topic")
        body(file("shouldPublishCustomerCreated.json"))
        headers {
            header("X-EVENT-TYPE","CustomerCreated")
        }
    }
}