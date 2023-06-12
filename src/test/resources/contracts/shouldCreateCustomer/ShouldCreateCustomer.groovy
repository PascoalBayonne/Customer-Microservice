package contracts.shouldCreateCustomer

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description('should create customer')
    request {
        method POST()
        url('/api/v1/customer')
        headers {
            contentType 'application/json'
        }
        body(file('shouldCreateCustomer_request.json'))
    }

    response {
        status 200
        headers {
            contentType 'application/json'
        }
        body(
                [id          : anyNumber(),
                 firstName   : 'Greg',
                 lastName    : 'Young',
                 birthDate   : '1976-08-11',
                 emailAddress: 'old-Email-greg@gmail.com',
                 ssn         : 888888888]
        )
    }
}