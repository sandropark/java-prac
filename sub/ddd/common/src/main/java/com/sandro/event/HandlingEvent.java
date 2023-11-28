package com.sandro.event;

import com.sandro.carriermovement.CarrierMovement;
import com.sandro.cargo.Cargo;

import java.time.LocalDateTime;

public class HandlingEvent {
    private Long id;
    private Cargo cargo;
    private CarrierMovement carrierMovement;
    private EventType eventType;
    private LocalDateTime completionTime;

    public HandlingEvent(Cargo cargo, EventType eventType, LocalDateTime completionTime) {
        this.cargo = cargo;
        this.eventType = eventType;
        this.completionTime = completionTime;
    }

    public static HandlingEvent newLoading(Cargo cargo, CarrierMovement loadOnto, LocalDateTime completionTime) {
        HandlingEvent handlingEvent = new HandlingEvent(cargo, EventType.LOAD, completionTime);
        handlingEvent.carrierMovement = loadOnto;
        return handlingEvent;
    }

    public enum EventType {
        LOAD,
        UNLOAD,
        RECEIVE,
        CLAIM,
        CUSTOMS
    }
}
