using Newtonsoft.Json;
using DeliveryService.Misc;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace DeliveryService.Models
{
    public class OrderDelivered
    {
        public OrderDelivered(DeliveryContext item)
        {
            OrderId = item.OrderId;
            BuyerId = item.BuyerId;
        }

        public OrderDelivered()
        {
        }

        [JsonProperty("orderId")]
        public string OrderId { get; set; }

        [JsonProperty("buyerId")]
        public string BuyerId { get; set; }

        public override string ToString()
        {
            return HelperUtil.PropertyList(this);
        }
    }
}
