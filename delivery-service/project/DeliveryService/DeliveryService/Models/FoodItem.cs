using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace DeliveryService.Models
{
    public class FoodItem
    {
        [JsonProperty("foodId")]
        [Required]
        public string FoodId { get; set; }
        [JsonProperty("name")]
        [Required]
        public string Name { get; set; }
        [JsonProperty("cost")]
        [Required]
        public int Cost { get; set; }
        [JsonProperty("qty")]
        [Required]
        public int Qty { get; set; }

    }
}
