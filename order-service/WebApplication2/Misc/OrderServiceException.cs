using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace OrderService.Misc
{
    public class OrderServiceException : Exception
    {
        public OrderServiceException(string message) : base(message)
        {

        }
    }
}
