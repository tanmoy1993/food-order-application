using Confluent.Kafka;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using DeliveryService.Misc;
using DeliveryService.Models;

namespace DeliveryService.Services
{
    public class GenericProducer<T> : BackgroundService
    {
        public static ConcurrentQueue<DeliveryContext> dlvCtxQueue;
        private readonly ProducerConfig _producerConfig;
        private readonly ILogger<GenericProducer<T>> _logger;
        private readonly string _topic;
        private readonly Func<DeliveryContext, T> _preProduceHandler;

        public GenericProducer(ProducerConfig producerConfig,
                        ILogger<GenericProducer<T>> logger,
                        Func<DeliveryContext, T> preProduceHandler)
        {
            Console.WriteLine("initializing publisher: " + typeof(T).FullName);
            if (dlvCtxQueue == null)
            {
                Console.WriteLine("initializing publisher Q");
                dlvCtxQueue = new ConcurrentQueue<DeliveryContext>();
            }
            _topic = HelperUtil.GetTopicName(typeof(T).Name);
            _producerConfig = producerConfig;
            _preProduceHandler = preProduceHandler;
            _logger = logger;
            Console.WriteLine("initialized publisher");
        }
        protected override async Task ExecuteAsync(CancellationToken cts)
        {
            DeliveryContext ctx;
            
            using var producer = new ProducerBuilder<Null, string>(_producerConfig).Build();
            while (!cts.IsCancellationRequested)
            {
                if (!dlvCtxQueue.TryDequeue(out ctx))
                {
                    await Task.Yield();
                    continue;
                }
                _logger.LogInformation($"processing {_topic} topic: {ctx} ");
                producer.Produce(_topic, CreateMessage(ctx), Handler); // ignore ack
                _logger.LogInformation($"processed {_topic} topic: {ctx} ");
            }
        }

        private void Handler(DeliveryReport<Null, string> report)
        {
            _logger.LogInformation("produced {} on p:{}, o:{}", _topic, report.Partition, report.Offset);
        }

        private Message<Null, string> CreateMessage(DeliveryContext item)
        {
            _logger.LogInformation("message creating");
            Message<Null, string> msg = new Message<Null, string>();

            msg.Headers = new Headers();
            msg.Headers.Add("__TypeId__", Encoding.Default.GetBytes(_topic.Substring(3)));
            msg.Value = JsonConvert.SerializeObject(_preProduceHandler(item));

            _logger.LogInformation($"message created: {msg.Value}");

            return msg;
        }
    }
}
