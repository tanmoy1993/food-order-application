package com.demoapp.foodorder.buyer.buyerservice.controller;

import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.demoapp.foodorder.buyer.buyerservice.misc.BuyerServiceException;
import com.demoapp.foodorder.buyer.buyerservice.model.Buyer;
import com.demoapp.foodorder.buyer.buyerservice.model.ExceptionResponse;
import com.demoapp.foodorder.buyer.buyerservice.service.BuyerService;

@RestController
@Validated
public class BuyerController {

	private static final String MEDIA_TYPE = "application/json";
	private static final Logger LOGGER = LoggerFactory.getLogger(BuyerController.class);

	@Autowired
	BuyerService buyerService;

	@GetMapping(value = "/buyer", produces = MEDIA_TYPE)
	public ResponseEntity<Iterable<Buyer>> getAllBuyer() {
		if (buyerService.getAllBuyer().toString().equals("[]"))
			return new ResponseEntity<Iterable<Buyer>>(buyerService.getAllBuyer(), HttpStatus.NO_CONTENT);
		return new ResponseEntity<Iterable<Buyer>>(buyerService.getAllBuyer(), HttpStatus.OK);
	}

	@PostMapping(value = "/buyer", produces = MEDIA_TYPE, consumes = MEDIA_TYPE)
	public ResponseEntity<Buyer> createBuyer(@Valid @RequestBody Buyer buyer) {
		return new ResponseEntity<Buyer>( buyerService.save(buyer), HttpStatus.CREATED);
	}

	@PutMapping(value = "/buyer/{id}", produces = MEDIA_TYPE, consumes = MEDIA_TYPE)
	public ResponseEntity<Buyer> updateBuyer(@Valid @RequestBody Buyer buyer) {
		return new ResponseEntity<Buyer>(buyerService.updateBuyer(buyer), HttpStatus.OK);
	}
	
	@PutMapping(value = "/buyer/{id}/{type}/{t_id}")
	public ResponseEntity<?> updateBuyerPreferrence(@Valid 
			@NotNull(message = "Invalid user id.") @PathVariable UUID buyerId,
			@Valid @NotEmpty(message = "Invalid type.") @PathVariable String type, 
			@Valid @NotNull(message = "Invalid type id.") @PathVariable UUID tId) {
		if(type.equalsIgnoreCase("address"))
			buyerService.updateBuyerPrefAddress(buyerId, tId);
		else if(type.equalsIgnoreCase("phone"))
			buyerService.updateBuyerPrefPhone(buyerId, tId);
		else
			throw new BuyerServiceException("Invalid resource access.");
		return new ResponseEntity<String>("updated", HttpStatus.OK);
	}

	@GetMapping(value = "/buyer/{id}", produces = MEDIA_TYPE)
	public ResponseEntity<Buyer> getUserById(@Valid 
			@NotNull(message = "Invalid user id.") @PathVariable UUID id) {
		return new ResponseEntity<Buyer>(buyerService.getBuyerbyId(id), HttpStatus.OK);
	}

	@RequestMapping(value = "/buyer/{id}", method = RequestMethod.DELETE, produces = MEDIA_TYPE)
	public ResponseEntity<?> deleteUserById(@Valid 
			@NotNull(message = "Invalid user id.") @PathVariable UUID id) {
		buyerService.deleteBuyer(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ExceptionHandler(value = BuyerServiceException.class)
	public ResponseEntity<ExceptionResponse> exceptionProcessor(BuyerServiceException ex){
		return new ResponseEntity<ExceptionResponse>(
				new ExceptionResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public ResponseEntity<ExceptionResponse> exceptionProcessorValidationBody(MethodArgumentNotValidException ex){
		LOGGER.error("Exception : {}", ex);
		StringBuilder message = new StringBuilder();
		for(FieldError fe : ex.getBindingResult().getFieldErrors()) {
			message.append(fe.getDefaultMessage());
			message.append(",");
		}
		return new ResponseEntity<ExceptionResponse>(new ExceptionResponse(message.toString()), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(value = { ConstraintViolationException.class })
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionResponse> exceptionProcessorValidationMethod(ConstraintViolationException e) {
         Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
         StringBuilder message = new StringBuilder();
         for (ConstraintViolation<?> violation : violations ) {
        	 message.append(violation.getMessage() + ",");
         }
 		return new ResponseEntity<ExceptionResponse>(new ExceptionResponse(message.toString()), HttpStatus.BAD_REQUEST);
    }

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<ExceptionResponse> exceptionProcessorFallback(Exception ex){
		LOGGER.error("Exception : {}", ex);
		return new ResponseEntity<ExceptionResponse>(new ExceptionResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
