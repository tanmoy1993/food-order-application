using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace DeliveryService.Misc
{
    public class DeliveryServiceException : Exception
    {
        public DeliveryServiceException(string message) : base(message)
        {

        }
    }
}
