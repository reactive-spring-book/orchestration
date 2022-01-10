package rsb.orchestration.scattergather;

import rsb.orchestration.Customer;
import rsb.orchestration.Order;
import rsb.orchestration.Profile;

import java.util.Collection;

record CustomerOrders(Customer customer, Collection<Order> orders, Profile profile) {
}