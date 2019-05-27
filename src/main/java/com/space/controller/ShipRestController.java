package com.space.controller;

import com.space.exception.BadRequestException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/ships")
public class ShipRestController {
    private final ShipService shipService;

    @Autowired
    public ShipRestController(final ShipService shipService) {
        this.shipService = shipService;
    }

    @GetMapping
    public List<Ship> getAllShips(@RequestParam(value = "name", required = false) String name,
                                  @RequestParam(value = "planet", required = false) String planet,
                                  @RequestParam(value = "shipType", required = false) ShipType shipType,
                                  @RequestParam(value = "after", required = false) Long after,
                                  @RequestParam(value = "before", required = false) Long before,
                                  @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                  @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                  @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                  @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                  @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                  @RequestParam(value = "minRating", required = false) Double minRating,
                                  @RequestParam(value = "maxRating", required = false) Double maxRating,
                                  @RequestParam(value = "order", defaultValue = "ID") ShipOrder order,
                                  @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
                                  @RequestParam(value = "pageSize", defaultValue = "3") Integer pageSize) {
        return shipService.getAll(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating, order, pageNumber, pageSize);
    }

    @GetMapping(value = "/count")
    public Integer getShipsCount(@RequestParam(value = "name", required = false) String name,
                                 @RequestParam(value = "planet", required = false) String planet,
                                 @RequestParam(value = "shipType", required = false) ShipType shipType,
                                 @RequestParam(value = "after", required = false) Long after,
                                 @RequestParam(value = "before", required = false) Long before,
                                 @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                 @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                 @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                 @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                 @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                 @RequestParam(value = "minRating", required = false) Double minRating,
                                 @RequestParam(value = "maxRating", required = false) Double maxRating) {

        ShipOrder order = ShipOrder.ID;
        Integer pageNumber = 0;
        Integer pageSize = Integer.MIN_VALUE;
        return shipService.getAll(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating, order, pageNumber, pageSize).size();
    }

    @PostMapping
    public Ship createShip(@RequestBody Ship ship) {
        return shipService.createShip(ship);
    }

    @PostMapping(value = "/{id}")
    public Ship updateShip(@PathVariable(value = "id") String idParam, @RequestBody Ship ship) {
        Long id = validateAndGetLongId(idParam);
        ship.setId(id);
        return shipService.updateShip(id, ship);
    }

    @DeleteMapping(value = "/{id}")
    public void deleteShip(@PathVariable(value = "id") String idParam) {
        Long id = validateAndGetLongId(idParam);
        shipService.deleteById(id);
    }

    @GetMapping(value = "/{id}")
    public Ship findShip(@PathVariable(value = "id") String idParam) {
        Long id = validateAndGetLongId(idParam);
        return shipService.findById(id);
    }


    private Long validateAndGetLongId(String idParam) {
        Long result = null;
        try {
            result = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            throw new BadRequestException(String.format("%s - is not valid id!", idParam));
        }
        if (result <= 0) {
            throw new BadRequestException(String.format("Id couldn't be less or equal to zero! Error while processing id: %s", idParam));
        }
        return result;
    }
}
