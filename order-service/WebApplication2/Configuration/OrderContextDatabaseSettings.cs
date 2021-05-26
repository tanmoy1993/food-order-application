using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace OrderService.Configuration
{
        public class OrderContextDatabaseSettings : IOrderContextDatabaseSettings
        {
            public string OrderContextCollectionName { get; set; }
            public string ConnectionString { get; set; }
            public string DatabaseName { get; set; }
        }

        public interface IOrderContextDatabaseSettings
        {
            string OrderContextCollectionName { get; set; }
            string ConnectionString { get; set; }
            string DatabaseName { get; set; }
        }
    
}
