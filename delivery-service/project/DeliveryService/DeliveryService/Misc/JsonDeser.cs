using Confluent.Kafka;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace DeliveryService.Misc
{
    public class JsonDeser<T> : IDeserializer<T> where T : class, new()
    {
        public T Deserialize(ReadOnlySpan<byte> data, bool isNull, SerializationContext context)
        {
            byte[] jsonData = data.ToArray();
            string dataS = System.Text.Encoding.UTF8.GetString(jsonData);

            //Console.WriteLine($"received data: {dataS}");
            T dataObj= JsonConvert.DeserializeObject<T>(dataS);
            //Console.WriteLine($"received data obj: {dataObj}");

            return dataObj;

        }
    }
}
