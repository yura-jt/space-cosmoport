package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;

import java.util.List;

public interface ShipService {
    List<Ship> getAll(String name, String planet, ShipType shipType, Long after, Long before, Boolean isUsed,
                      Double minSpeed, Double maxSpeed, Integer minCrewSize, Integer maxCrewSize,
                      Double minRating, Double maxRating, ShipOrder shipOrder, Integer pageNumber, Integer pageSize);

    void deleteById(Long id);

    Ship findById(Long id);

    Ship createShip(Ship ship);

    Ship updateShip(Long id, Ship ship);

}
