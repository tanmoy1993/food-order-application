using Confluent.Kafka;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace OrderService.Models
{
    public class ExceptionResponse
    {
        public ExceptionResponse(string message)
        {
            Message = message;
        }

        public string Message { get; set; }
    }
}
