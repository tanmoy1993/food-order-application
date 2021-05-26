using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;
using OrderService.Misc;

namespace OrderService.Models
{
    public class OrderConfirmed
    {
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
