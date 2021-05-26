using Newtonsoft.Json;
using OrderService.Misc;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace OrderService.Models
{
    public class OrderDelivered
    {
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
