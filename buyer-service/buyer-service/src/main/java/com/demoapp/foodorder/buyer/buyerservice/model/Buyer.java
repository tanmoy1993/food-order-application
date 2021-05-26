package com.demoapp.foodorder.buyer.buyerservice.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "FO_BUYER")
public class Buyer {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="BUYER_ID")
	private UUID buyerId;
	
	@Column(name="FNAME")
	@NotEmpty(message = "First name should not be empty.")
	private String firstname;

	@Column(name="LNAME")
	@NotEmpty(message = "Last name should not be empty.")
	private String lastname;

	@OneToMany(orphanRemoval=true,cascade=CascadeType.ALL,fetch=FetchType.LAZY)
	@Fetch(FetchMode.SUBSELECT)
	@JoinColumn(name="BUYER")
	@OrderColumn(name="ORDERING")
	@NotEmpty(message = "Please provide a valid phone number.")
	@Size(max=3, message = "At most 3 phone numbers are allowed.")
	private List<PhoneNumber> phones;

	@OneToMany(orphanRemoval=true,cascade=CascadeType.ALL,fetch=FetchType.LAZY)
	@Fetch(FetchMode.SUBSELECT)
	@JoinColumn(name="BUYER")
	@OrderColumn(name="ORDERING")
	@Size(max=3, message = "At most 3 addresses are allowed.")
	@NotEmpty(message = "Please provide a valid address.")
	private List<Address> address;
	
	@Column(name="P_PH_INDEX")
	@ColumnDefault(value = "0")
	private int prefPhoneIndex;
	
	@Column(name="P_AD_INDEX")
	@ColumnDefault(value = "0")
	private int prefAddrIndex;
	
	@Column(name="CURRENT_ORDER_ID")
	private String orderId;

	public Buyer() {
	}

	public Buyer(UUID buyerId, String firstname, String lastname) {
		this.buyerId = buyerId;
		this.firstname = firstname;
		this.lastname = lastname;
	}

	public UUID getBuyerId() {
		return buyerId;
	}

	public void setBuyerId(UUID buyerId) {
		this.buyerId = buyerId;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public List<PhoneNumber> getPhones() {
		return phones;
	}

	public void setPhones(List<PhoneNumber> phones) {
		this.phones = phones;
	}
	
	public void addPhone(PhoneNumber phone) {
		if(this.phones==null)
			this.phones = new ArrayList<>();
		this.phones.add(phone);
	}

	public List<Address> getAddresses() {
		return address;
	}

	public void setAddresses(List<Address> address) {
		this.address = address;
	}

	public int getPrefPhoneIndex() {
		return prefPhoneIndex;
	}

	public void setPrefPhoneIndex(int prefPhoneIndex) {
		this.prefPhoneIndex = prefPhoneIndex;
	}

	public int getPrefAddrIndex() {
		return prefAddrIndex;
	}

	public void setPrefAddrIndex(int prefAddrIndex) {
		this.prefAddrIndex = prefAddrIndex;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	@Override
	public String toString() {
		return "Buyer [buyerId=" + buyerId + ", firstname=" + firstname + ", lastname=" + lastname + ", phones="
				+ phones + ", address=" + address + ", prefPhoneIndex=" + prefPhoneIndex + ", prefAddrIndex="
				+ prefAddrIndex + "]";
	}
	
}
