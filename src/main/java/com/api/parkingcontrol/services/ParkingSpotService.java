package com.api.parkingcontrol.services;

import com.api.parkingcontrol.dtos.ParkingSpotDto;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.repositories.ParkingSpotRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ParkingSpotService {

    @Autowired
    ParkingSpotRepository parkingSpotRepository;

    public Page<ParkingSpotModel> getAllParkingSpots(Pageable pageable) {
        return parkingSpotRepository.findAll(pageable);
    }

    @Transactional
    public ResponseEntity<Object> createParkingSpot(ParkingSpotDto parkingSpotDto){

        if(findByCarLicensePlate(parkingSpotDto.getCarLicensePlate())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Car license plate is already in use!");
        }
        if(findByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Parking Spot is already in use!");
        }
        if(findByApartmentNumberAndApartmentBlock(parkingSpotDto.getApartmentNumber(), parkingSpotDto.getApartmentBlock())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Apartment and block already been registered!");
        }

        var parkingSpot = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpot);
        parkingSpot.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotRepository.save(parkingSpot));
    }

    public ResponseEntity<Object> getParkingSpotById(UUID parkingSpotId) {
        Optional<ParkingSpotModel> parkingSpot = parkingSpotRepository.findById(parkingSpotId);
        if(parkingSpot == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found!");
        }
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpot);
    }

    @Transactional
    public ResponseEntity<Object> deleteParkingSpot(UUID parkingSpotId) {
        Optional<ParkingSpotModel> parkingSpot = parkingSpotRepository.findById(parkingSpotId);
        if(!parkingSpot.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not found.");
        }
        parkingSpotRepository.delete(parkingSpot.get());
        return ResponseEntity.status(HttpStatus.OK).body("Parking Sport deleted successfully.");
    }

    public ResponseEntity<Object> updateParkingSpot(UUID parkingSpotId, ParkingSpotDto parkingSpotDto) {
        Optional<ParkingSpotModel> parkingSpot = parkingSpotRepository.findById(parkingSpotId);
        if(!parkingSpot.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not found.");
        }
        ParkingSpotModel parkingSpotModel = parkingSpot.get();
        parkingSpotModel.setParkingSpotNumber(parkingSpotDto.getParkingSpotNumber());
        parkingSpotModel.setCarLicensePlate(parkingSpotDto.getCarLicensePlate());
        parkingSpotModel.setCarModel(parkingSpotDto.getCarModel());
        parkingSpotModel.setCarBrand(parkingSpotDto.getCarBrand());
        parkingSpotModel.setCarColor(parkingSpotDto.getCarColor());
        parkingSpotModel.setApartmentNumber(parkingSpotDto.getApartmentNumber());
        parkingSpotModel.setApartmentBlock(parkingSpotDto.getApartmentBlock());
        parkingSpotModel.setResponsibleName(parkingSpotDto.getResponsibleName());
        parkingSpotModel.setRegistrationDate(parkingSpotModel.getRegistrationDate());

        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotRepository.save(parkingSpotModel));
    }

    public boolean findByCarLicensePlate(String carLicensePlate) {
        return parkingSpotRepository.existsByCarLicensePlate(carLicensePlate);
    }

    public boolean findByParkingSpotNumber(String parkingSpotNumber) {
        return parkingSpotRepository.existsByParkingSpotNumber(parkingSpotNumber);
    }

    public boolean findByApartmentNumberAndApartmentBlock(String apartmentNumber, String apartmentBlock) {
        return parkingSpotRepository.existsByApartmentNumberAndApartmentBlock(apartmentNumber, apartmentBlock);
    }
}
