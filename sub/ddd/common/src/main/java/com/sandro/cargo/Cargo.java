package com.sandro.cargo;

import com.sandro.customer.Customer;
import com.sandro.customer.CustomerRole;
import com.sandro.event.HandlingEvent;
import com.sandro.location.Location;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cargo {
    private Long id;
    private final Map<Customer, CustomerRole> customerRole;
    @Setter
    private DeliverySpecification deliverySpecification;
    private final DeliveryHistory deliveryHistory;

    public Cargo(Long id) {
        this.id = id;
        this.deliveryHistory = new DeliveryHistory(this);
        this.customerRole = new HashMap<>();
    }

    static class DeliveryHistory {
        private Long id;
        private final Cargo cargo;

        public DeliveryHistory(Cargo cargo) {
            this.cargo = cargo;
        }
    }

    static class DeliverySpecification {
        private Location location;
    }

}
