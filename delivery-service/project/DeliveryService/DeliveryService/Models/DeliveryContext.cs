using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;
using DeliveryService.Misc;

namespace DeliveryService.Models
{
    public partial class DeliveryContext
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string OrderId { get; set; }

        [Required]
        public string BuyerId { get; set; }

        [Required]
        public string RestaurantId { get; set; }

        [Required]
        public int TotalCost { get; set; }

        [Required]
        [MinLength(1)]
        public List<FoodItem> OrderedItems { get; set; }

        [BsonDateTimeOptions(Kind = DateTimeKind.Utc)]
        public DateTime ExpireOn { get; set; }

        public int DeliveryETAMins { get; set; }

        [BsonRepresentation(BsonType.String)]
        [JsonConverter(typeof(StringEnumConverter))]
        public Status CurrentStatus { get; set; }

        public BuyerContact contact { get; set; }

        public override string ToString() {
            return HelperUtil.PropertyList(this);
        }

        public DeliveryContext() { 
        }

        public DeliveryContext(OrderReceived item) {
            OrderId = item.OrderId;
            BuyerId = item.BuyerId;
            RestaurantId = item.RestaurantId;
            OrderedItems = item.OrderedItems;
            TotalCost = item.TotalCost;

            CurrentStatus = DeliveryContext.Status.RECEIVED;
        }
    }
}
