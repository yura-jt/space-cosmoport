package com.space.service.impl;

import com.space.controller.ShipOrder;
import com.space.exception.BadRequestException;
import com.space.exception.EntityNotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ShipServiceImpl implements ShipService {
    private final ShipRepository shipRepository;

    @Autowired
    public ShipServiceImpl(final ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ship> getAll(String name, String planet, ShipType shipType, Long after, Long before, Boolean isUsed,
                             Double minSpeed, Double maxSpeed, Integer minCrewSize, Integer maxCrewSize, Double minRating,
                             Double maxRating, ShipOrder shipOrder, Integer pageNumber, Integer pageSize) {
        List<Ship> allShips = shipRepository.findAll();
        List<Ship> filteredShips = new ArrayList<>();

        if (pageSize.equals(Integer.MIN_VALUE)) {
            pageSize = allShips.size();
        }

        for (Ship ship : allShips) {
            boolean shipIsMatchedByAllCriteria = true;
            if (!Objects.isNull(name) && !ship.getName().contains(name)) {
                shipIsMatchedByAllCriteria = false;
            }
            if (!Objects.isNull(planet) && !ship.getPlanet().contains(planet)) {
                shipIsMatchedByAllCriteria = false;
            }
            if (!Objects.isNull(shipType) && ship.getShipType() != shipType) {
                shipIsMatchedByAllCriteria = false;
            }
            if (!Objects.isNull(after) && ship.getProdDate().before(new Date(after))) {
                shipIsMatchedByAllCriteria = false;
            }
            if (!Objects.isNull(before) && ship.getProdDate().after(new Date(before))) {
                shipIsMatchedByAllCriteria = false;
            }
            if (!Objects.isNull(isUsed) && (!ship.getUsed().equals(isUsed))) {
                shipIsMatchedByAllCriteria = false;
            }
            if (!Objects.isNull(minSpeed) && ship.getSpeed() < minSpeed) {
                shipIsMatchedByAllCriteria = false;
            }
            if (!Objects.isNull(maxSpeed) && ship.getSpeed() > maxSpeed) {
                shipIsMatchedByAllCriteria = false;
            }
            if (!Objects.isNull(minRating) && ship.getRating() < minRating) {
                shipIsMatchedByAllCriteria = false;
            }
            if (!Objects.isNull(maxRating) && ship.getRating() > maxRating) {
                shipIsMatchedByAllCriteria = false;
            }
            if (!Objects.isNull(minCrewSize) && ship.getCrewSize() < minCrewSize) {
                shipIsMatchedByAllCriteria = false;
            }
            if (!Objects.isNull(maxCrewSize) && ship.getCrewSize() > maxCrewSize) {
                shipIsMatchedByAllCriteria = false;
            }

            if (shipIsMatchedByAllCriteria) {
                filteredShips.add(ship);
            }
        }

        filteredShips.sort(getComparator(shipOrder));

        int startIndex = pageNumber * pageSize;
        int endIndex = (startIndex + pageSize) > filteredShips.size() ? filteredShips.size() : startIndex + pageSize;

        return filteredShips.subList(startIndex, endIndex);
    }


    private Comparator<Ship> getComparator(ShipOrder shipOrder) {
        Comparator<Ship> comparator;
        if (shipOrder.equals(ShipOrder.SPEED)) {
            comparator = Comparator.comparing(Ship::getSpeed);
        } else if (shipOrder.equals(ShipOrder.DATE)) {
            comparator = Comparator.comparing(Ship::getProdDate);
        } else if (shipOrder.equals(ShipOrder.RATING)) {
            comparator = Comparator.comparing(Ship::getRating);
        } else {
            comparator = Comparator.comparing(Ship::getId);
        }
        return comparator;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        checkAndGetIfShip(id);
        shipRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Ship findById(Long id) {
        return checkAndGetIfShip(id);

    }

    private Ship checkAndGetIfShip(Long id) {
        Optional<Ship> ship = shipRepository.findById(id);
        if (!ship.isPresent()) {
            throw new EntityNotFoundException(String.format("Ship with id: %d, is not exist in database!", id));
        }
        return ship.get();
    }

    @Override
    @Transactional
    public Ship createShip(Ship newShip) {
        validateProvidedParams(newShip);
        if (newShip.getUsed() == null) {
            newShip.setUsed(false);
        }

        newShip.setSpeed(Math.round(newShip.getSpeed() * 100) / 100d);
        Double rating = calculateShipRating(newShip.getSpeed(), newShip.getProdDate(), newShip.getUsed());
        newShip.setRating(rating);
        return shipRepository.save(newShip);
    }

    @Override
    @Transactional
    public Ship updateShip(Long id, Ship updatedShip) {
        Ship existingShip = checkAndGetIfShip(id);
        String name = updatedShip.getName();
        String planet = updatedShip.getPlanet();
        Date prodDate = updatedShip.getProdDate();
        Boolean isUsed = updatedShip.getUsed();
        Double speed = updatedShip.getSpeed();
        Integer crewSize = updatedShip.getCrewSize();
        ShipType shipType = updatedShip.getShipType();

        if (name != null && isNameValid(name)) {
            existingShip.setName(name);
        }
        if (planet != null && isPlanetValid(planet)) {
            existingShip.setPlanet(planet);
        }
        if (prodDate != null && isProdDateValid(prodDate)) {
            existingShip.setProdDate(prodDate);
        }
        if (isUsed != null) {
            existingShip.setUsed(isUsed);
        }
        if (speed != null && isSpeedValid(speed)) {
            existingShip.setSpeed(speed);
        }
        if (crewSize != null && isCrewSize(crewSize)) {
            existingShip.setCrewSize(crewSize);
        }

        if (shipType != null) {
            existingShip.setShipType(shipType);
        }

        Double rating = calculateShipRating(existingShip.getSpeed(), existingShip.getProdDate(), existingShip.getUsed());
        existingShip.setRating(rating);

        return shipRepository.save(existingShip);
    }

    private void validateProvidedParams(Ship providedParams) {
        checkParamsForNull(providedParams);
        checkProvidedParamsAreInBound(providedParams);
    }

    private void checkParamsForNull(Ship ship) {
        if (Objects.isNull(ship.getName()) ||
                Objects.isNull(ship.getPlanet()) ||
                Objects.isNull(ship.getShipType()) ||
                Objects.isNull(ship.getProdDate()) ||
                Objects.isNull(ship.getSpeed()) ||
                Objects.isNull(ship.getCrewSize())) {
            throw new BadRequestException();
        }
    }

    private void checkProvidedParamsAreInBound(Ship ship) {
        isNameValid(ship.getName());
        isPlanetValid(ship.getPlanet());
        isProdDateValid(ship.getProdDate());
        isSpeedValid(ship.getSpeed());
        isCrewSize(ship.getCrewSize());
    }

    private boolean isNameValid(String name) {
        if (name.length() > 50 || name.equals("")) {
            throw new BadRequestException();
        }
        return true;
    }

    private boolean isPlanetValid(String planet) {
        if (planet.length() > 50 || planet.equals("")) {
            throw new BadRequestException();
        }
        return true;
    }

    private boolean isProdDateValid(Date prodDate) {
        int year = prodDate.getYear() + 1900;
        if (year < 2800 || year > 3019) {
            throw new BadRequestException();
        }
        return true;
    }

    private boolean isSpeedValid(Double speed) {
        if (speed < 0.01 || speed > 0.99) {
            throw new BadRequestException();
        }
        return true;
    }

    private boolean isCrewSize(Integer crewSize) {
        if (crewSize < 1 || crewSize > 9999) {
            throw new BadRequestException();
        }
        return true;
    }


    private Double calculateShipRating(Double shipSpeed, Date prodDate, Boolean isUsed) {
        double k = isUsed ? 0.5 : 1;
        int shipAge = 3019 - (prodDate.getYear() + 1900);

        Double rating = (80 * shipSpeed * k) / (shipAge + 1);

        return Math.round(rating * 100) / 100d;
    }

}
