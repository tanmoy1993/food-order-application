using MongoDB.Bson.Serialization.Attributes;
using System.ComponentModel.DataAnnotations;

namespace DeliveryService.Models
{
    public class BuyerContact
    {
        [BsonIgnore]
        public string orderId { get; set; }

        public string buyerId { get; set; }

        public Phone phone { get; set; }

        public Address address { get; set; }

        public class Phone
        {
            public string phoneNo { get; set; }
        }

        public class Address
        {
            public string addressLineNo1 { get; set; }

            public string addressLineNo2 { get; set; }

            public int zipCode { get; set; }
        }
    }
}