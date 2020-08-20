# Some Interesting Use Cases 

## Hedging 

* run `eureka-service`
* run `slow-service` and specify an environment variable `RSB_SLOW_SERVICE_DELAY=0`. Let's call this `fast-slow`.
* run `slow-service` and specify an environment variable `RSB_SLOW_SERVICE_DELAY=10`. Let's call this `slow-slow`.
* run `HedgingApplication` in `client`


## Scatter Gather 

* run `eureka-service`, `profile-service`, `order-service`, and `customer-service`
* run `ScatterGatherApplication` in `client`

