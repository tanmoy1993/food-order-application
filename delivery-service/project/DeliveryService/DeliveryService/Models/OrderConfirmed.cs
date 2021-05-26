using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;
using DeliveryService.Misc;

namespace DeliveryService.Models
{
    public class OrderConfirmed
    {
        public OrderConfirmed() { 
        }

        public OrderConfirmed(DeliveryContext item)
        {
            OrderId = item.OrderId;
            BuyerId = item.BuyerId;
            DeliveryETA = item.DeliveryETAMins;
        }

        [JsonProperty("orderId")]
        public string OrderId { get; set; }

        [JsonProperty("buyerId")]
        public string BuyerId { get; set; }

        [JsonProperty("deliveryETA")]
        public int DeliveryETA { get; set; }

        public override string ToString()
        {
            return HelperUtil.PropertyList(this);
        }
    }
}
