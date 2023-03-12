package com.api.parkingcontrol.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.parkingcontrol.dtos.ParkingSpotDto;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

	@Autowired
	private ParkingSpotService parkingSpotService;
	
	@PostMapping
	public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto){
		// VALIDACOES
		if(parkingSpotService.existsByLicensePlateCar(parkingSpotDto.getLicensePlateCar())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflit: A placa do carro ja esta em uso!");
		}
		if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflit: A vaga ja esta em uso!");
		}
		if(parkingSpotService.existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflit: A placa do carro ja esta registrada para esse apartamento/bloco");
		}
		
		var parkingSpotModel = new ParkingSpotModel(); // APENAS PARA INSTANCIAR O OBJETO PARKINGSPOTMODEL
		BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);// PARA CONVERTER O DTO EM MODEL
		parkingSpotModel.setRegistrationData(LocalDateTime.now(ZoneId.of("UTC")));// PARA SETAR O HORARIO DE REGISTRO DO POST
		return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
	}
	
	@GetMapping
	public ResponseEntity<List<ParkingSpotModel>>  getAllParkingSpots(){
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value="id") UUID id){
		Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
		
		if(!parkingSpotModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vaga não encontrada!");
		}
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value="id") UUID id){
		Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
		
		if(!parkingSpotModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vaga não encontrada!");
		}
		parkingSpotService.delete(parkingSpotModelOptional.get());
		
		return ResponseEntity.status(HttpStatus.OK).body("Vaga deletada com sucesso!");
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") UUID id, @RequestBody @Valid ParkingSpotDto parkingSpotDto){
		
		Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
		
		if(!parkingSpotModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vaga não encontrada!");
		}
		
		var parkingSpotModel = new ParkingSpotModel();
		BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);//PARA CONVERTER DE SPOTMODEL PARA MODEL
		parkingSpotModel.setId(parkingSpotModelOptional.get().getId());
		parkingSpotModel.setRegistrationData(parkingSpotModelOptional.get().getRegistrationData());
		
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
	}
}
