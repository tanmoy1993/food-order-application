using Confluent.Kafka;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using OrderService.Misc;

namespace OrderService.Services
{
    public class GenericConsumer<T> : BackgroundService where T : class, new()
    {
        private readonly ConsumerConfig _consumerConfig;
        private readonly ILogger<GenericConsumer<T>> _logger;
        private readonly OrderContextService _dbService;
        private readonly Func<T, OrderContextService, Task<Boolean>> _postConsumeHandler;

        public GenericConsumer(ConsumerConfig consumerConfig,
                                        ILogger<GenericConsumer<T>> logger,
                                        OrderContextService dbService, 
                                        Func<T, OrderContextService, Task<Boolean>> postConsumeHandler)
        {
            Console.WriteLine("initializing GeneralConsumer: " + typeof(T).FullName);
            _consumerConfig = consumerConfig;
            _logger = logger;
            _dbService = dbService;
            _postConsumeHandler = postConsumeHandler;
            Console.WriteLine("initialized GeneralConsumer");
        }

        protected override async Task ExecuteAsync(CancellationToken cts)
        {
            ConsumeResult<Ignore, T> result = null;
            Console.WriteLine("initializing GeneralConsumer subscriber");
            using var consumer = new ConsumerBuilder<Ignore, T>(_consumerConfig)
                        .SetValueDeserializer(new JsonDeser<T>())
                        .SetErrorHandler((_, e) => Console.WriteLine($"Error: {e.Reason}"))
                        .SetPartitionsAssignedHandler((c, partitions) =>
                        {
                            Console.WriteLine($"Assigned partitions: [{string.Join(", ", partitions)}]");
                        })
                        .SetPartitionsRevokedHandler((c, partitions) =>
                        {
                            Console.WriteLine($"Revoking assignment: [{string.Join(", ", partitions)}]");
                        }).Build();
            string topic = HelperUtil.GetTopicName(typeof(T).Name);
            consumer.Subscribe(topic);
            Console.WriteLine($"subscribed topic: {topic}");
            while (!cts.IsCancellationRequested)
            {
                T status = null;
                while (result == null)
                {
                    try
                    {
                        result = consumer.Consume(1000);
                        if (result == null)
                        {
                            await Task.Yield();
                        }
                        else
                        {
                            _logger.LogInformation($"object: {result.Message.Value}");
                            status = result.Message.Value;
                        }
                    }
                    catch (ConsumeException e)
                    {
                        _logger.LogError("Exception during consumption : {}, {}, {}, {}, {}",
                            e.Message, e.StackTrace, e.InnerException.StackTrace,
                            e.InnerException.Message, e.InnerException.Data);
                    }
                }

                _ = _postConsumeHandler(status, _dbService);

                result = null;
            }

        }
    }
}
