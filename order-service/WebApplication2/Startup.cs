using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Confluent.Kafka;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using OrderService.Configuration;
using OrderService.Misc;
using OrderService.Models;
using OrderService.Services;

namespace OrderService
{
    public class Startup
    {
        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        public IConfiguration Configuration { get; }

        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices(IServiceCollection services)
        {
            services.AddOptions();
            services.Configure<OrderContextDatabaseSettings>(
                Configuration.GetSection("OrderContextDatabaseSettings"));
            var s = Configuration.GetSection("OrderContextDatabaseSettings").Get<OrderContextDatabaseSettings>();
            Console.WriteLine("props: " +  s.OrderContextCollectionName);
            services.AddSingleton<IOrderContextDatabaseSettings>(sp =>
                sp.GetRequiredService<IOptions<OrderContextDatabaseSettings>>().Value);

            services.AddMvc().AddNewtonsoftJson();

            services.AddSingleton<OrderContextService>();

            string kafkaHost = Environment.GetEnvironmentVariable("KAFKA_ADVERTISED_HOST_NAME")
                                               + ":" + Environment.GetEnvironmentVariable("KAFKA_ADVERTISED_PORT");
            string groupId = Environment.GetEnvironmentVariable("ORDER_SERVICE_KAFKA_GROUP_ID");

            Console.WriteLine($"kafka host: {kafkaHost}, groud: {groupId}");

            ProducerConfig producerConfig = new ProducerConfig { BootstrapServers = kafkaHost };
            ConsumerConfig consumerConfig = new ConsumerConfig 
                                            { BootstrapServers = kafkaHost, GroupId = groupId,
                EnableAutoCommit = true, 
                //Debug = "all", 
                EnableAutoOffsetStore = true, 
                AutoCommitIntervalMs = 5000
            };

            services.AddSingleton<ProducerConfig>(producerConfig);
            services.AddSingleton<ConsumerConfig>(consumerConfig);

            Console.WriteLine("checking kafka conn");
            while (!PrintMetadata(kafkaHost))
            {
                Thread.Sleep(10000);
                Console.WriteLine("re-checking kafka conn");
            }

            Func<PaymentStatus, OrderContextService, Task<Boolean>> postConsumeHandlerPayStatus = 
                ((status, dbService) =>
                {
                    if (status.Status)
                        return dbService.UpdateStatus(status.OrderId, OrderContext.Status.PAYMENT_SUCCESS, true);
                    else
                        return dbService.UpdateStatusAndRemove(status.OrderId, OrderContext.Status.PAYMENT_FAIL, true);
                });

            Func<OrderConfirmed, OrderContextService, Task<Boolean>> postConsumeHandlerOrdConfirmed =
                ((status, dbService) =>
                       dbService.UpdateConfirmed(status.OrderId, status.DeliveryETA, true)
                );

            Func<OrderDelivered, OrderContextService, Task<Boolean>> postConsumeHandlerOrdDelivered =
                ((status, dbService) =>
                       dbService.UpdateStatusAndRemove(status.OrderId, OrderContext.Status.DELIVERED, true)
                );

            Func<OrderCancelled, OrderContextService, Task<Boolean>> postConsumeHandlerOrdCancel =
                ((status, dbService) =>
                {
                    if (status.Reason.Contains("RESTAURANT_CANCELLED"))
                        return dbService.UpdateStatusAndRemove(status.OrderId, OrderContext.Status.RESTAURANT_CANCELLED, true);
                    else
                        return Task.FromResult(false);                    
                });

            services.AddSingleton<Func<PaymentStatus, OrderContextService, 
                                    Task<Boolean>>>(postConsumeHandlerPayStatus);
            services.AddSingleton<Func<OrderConfirmed, OrderContextService, 
                                    Task<Boolean>>>(postConsumeHandlerOrdConfirmed);
            services.AddSingleton<Func<OrderDelivered, OrderContextService,
                                    Task<Boolean>>>(postConsumeHandlerOrdDelivered);
            services.AddSingleton<Func<OrderCancelled, OrderContextService,
                                    Task<Boolean>>>(postConsumeHandlerOrdCancel);

            services.AddSingleton<Func<OrderContext, OrderReceived>>
                                                     (HelperUtil.GetReceivedTopicMsgBody);
            services.AddSingleton<Func<OrderContext, OrderCancelled>>
                                                    (HelperUtil.GetCancelTopicMsgBody);

            Console.WriteLine("initializing background services");
            //services.AddHostedService<OrderReceivedPublisher>();

            services.AddHostedService<GenericProducer<OrderReceived>>();
            services.AddHostedService<GenericProducer<OrderCancelled>>();

            services.AddHostedService<GenericConsumer<PaymentStatus>>();
            services.AddHostedService<GenericConsumer<OrderConfirmed>>();
            services.AddHostedService<GenericConsumer<OrderCancelled>>();
            services.AddHostedService<GenericConsumer<OrderDelivered>>();

            Console.WriteLine("initializing controller");
            services.AddControllers();
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IWebHostEnvironment env)
        {
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
            }
            else
            {
                app.UseExceptionHandler("/order/error");
            }

            app.UseRouting();

            app.UseAuthorization();

            app.UseEndpoints(endpoints =>
            {
                endpoints.MapControllers();
            });
        }

        static Boolean PrintMetadata(string bootstrapServers)
        {
            Boolean topicPartionCreated = false;
            try
            {
                using (var adminClient = new AdminClientBuilder(new AdminClientConfig { BootstrapServers = bootstrapServers }).Build())
                {
                    // Warning: The API for this functionality is subject to change.
                    var meta = adminClient.GetMetadata(TimeSpan.FromSeconds(20));
                    Console.WriteLine($"{meta.OriginatingBrokerId} {meta.OriginatingBrokerName}");
                    meta.Brokers.ForEach(broker =>
                        Console.WriteLine($"Broker: {broker.BrokerId} {broker.Host}:{broker.Port}"));

                    meta.Topics.ForEach(topic =>
                    {
                        Console.WriteLine($"Topic: {topic.Topic} {topic.Error}");
                        topic.Partitions.ForEach(partition =>
                        {
                            topicPartionCreated = true;
                            Console.WriteLine($"  Partition: {partition.PartitionId}");
                            Console.WriteLine($"    Replicas: {ToString(partition.Replicas)}");
                            Console.WriteLine($"    InSyncReplicas: {ToString(partition.InSyncReplicas)}");
                        });
                    });
                }
            }
            catch (Exception e) {
                Console.WriteLine($"error while reaching broker: {e.Message}" );
            }
            return topicPartionCreated;
        }

        static string ToString(int[] array) => $"[{string.Join(", ", array)}]";
    }
}
