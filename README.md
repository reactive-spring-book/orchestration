# Some Interesting Use Cases 

## Hedging 

* run `eureka-service`
* run `slow-service` and specify an environment variable `RSB_SLOW_SERVICE_DELAY=0`. Let's call this `fast-slow`.
* run `slow-service` and specify an environment variable `RSB_SLOW_SERVICE_DELAY=10`. Let's call this `slow-slow`.
* run `HedgingApplication` in `client`

## Scatter Gather 

* run `eureka-service`, `profile-service`, `order-service`, and `customer-service`
* run `ScatterGatherApplication` in `client`

## Resilience4J 

* run `eureka-service`
* run `error-service`
* run `ResilientClientApplication` in `client`. There are four different demos in the same package as the main class, so be sure to note the profile of the demo that you want to run. Here are the profile names: `bulkhead`, `cb`, `rl`, and `retry`.

## Gateway 
 * TBD