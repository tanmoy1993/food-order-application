using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;
using DeliveryService.Misc;

namespace DeliveryService.Models
{
    public class PaymentStatus
    {
        [JsonProperty("orderId")]
        public string OrderId { get; set; }

        [JsonProperty("status")]
        public Boolean Status { get; set; }

        [JsonProperty("message")]
        public string Message { get; set; }

        public override string ToString()
        {
            return HelperUtil.PropertyList(this);
        }

    }
}
