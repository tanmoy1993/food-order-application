using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using OrderService.Misc;

namespace OrderService.Models
{
    public class OrderReceived
    {
        public OrderReceived(OrderContext item)
        {
            this.OrderId = item.OrderId;
            this.BuyerId = item.BuyerId;
            this.RestaurantId = item.RestaurantId;
            this.TotalCost = item.TotalCost;
            this.OrderedItems = item.OrderedItems;
        }

        public OrderReceived()
        { 
        }

        [JsonProperty("orderId")]
        public string OrderId { get; set; }

        [JsonProperty("buyerId")]
        public string BuyerId { get; set; }

        [JsonProperty("restaurantId")]
        public string RestaurantId { get; set; }

        [JsonProperty("totalCost")]
        public int TotalCost { get; set; }

        [JsonProperty("orderedItems")]
        public List<FoodItem> OrderedItems { get; set; }

        public override string ToString()
        {
            return HelperUtil.PropertyList(this);
        }
    }
}
