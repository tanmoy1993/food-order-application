using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace DeliveryService.Configuration
{
    public class DeliveryServiceDBConfig : IDeliveryServiceDBConfig
    {
        public string DeliveryContextCollectionName { get; set; }
        public string ConnectionString { get; set; }
        public string DatabaseName { get; set; }
    }

    public interface IDeliveryServiceDBConfig
    {
        string DeliveryContextCollectionName { get; set; }
        string ConnectionString { get; set; }
        string DatabaseName { get; set; }
    }
}
