package com.demoapp.foodorder.buyer.buyerservice.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "FO_BUYER_ADDRESS")
public class Address {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ADDRESS_ID")
	private UUID id;

    @Column(name = "ADDRESS_LINE1")
    @NotBlank(message = "Address line 1 should not be empty.")
    @Size(max=250, message = "Address line 1 too long.")
	private String addressLineNo1;

    @Column(name = "ADDRESS_LINE2")
    @Size(max=250, message = "Address line 2 too long.")
	private String addressLineNo2;

    @Column(name = "ZIP_CODE")
    @Min(value = 50000, message = "Zip code is invalid.")
    @Max(value = 60000, message = "Zip code is invalid.")
    @NotNull(message = "Zip code is invalid.")
	private int zipCode;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getAddressLineNo1() {
		return addressLineNo1;
	}

	public void setAddressLineNo1(String addressLineNo1) {
		this.addressLineNo1 = addressLineNo1;
	}

	public String getAddressLineNo2() {
		return addressLineNo2;
	}

	public void setAddressLineNo2(String addressLineNo2) {
		this.addressLineNo2 = addressLineNo2;
	}

	public int getZipCode() {
		return zipCode;
	}

	public void setZipCode(int zipCode) {
		this.zipCode = zipCode;
	}

	@Override
	public String toString() {
		return "Address [ id=" + id + ", line1=" + addressLineNo1 + ", line2=" + addressLineNo2 + ", zip=" + zipCode + "]";
	}

}
