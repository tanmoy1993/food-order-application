using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;
using OrderService.Misc;

namespace OrderService.Models
{
    public class OrderCancelled
    {
        public OrderCancelled()
        {
        }

        public OrderCancelled(OrderContext item)
        {
            this.OrderId = item.OrderId;
            this.BuyerId = item.BuyerId;
            this.RestaurantId = item.RestaurantId;

            //if serialization done from OrderContext i.e. this service
            this.Reason = "USER_CANCELLED";
        }

        [JsonProperty("orderId")]
        public string OrderId { get; set; }

        [JsonProperty("buyerId")]
        public string BuyerId { get; set; }

        [JsonProperty("restaurantId")]
        public string RestaurantId { get; set; }

        [JsonProperty("reason")]
        public string Reason { get; set; }
        public override string ToString()
        {
            return HelperUtil.PropertyList(this);
        }
    }
}
