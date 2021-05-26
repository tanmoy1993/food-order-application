using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Confluent.Kafka;
using DeliveryService.Configuration;
using DeliveryService.Misc;
using DeliveryService.Models;
using DeliveryService.Services;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace DeliveryService
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
            services.Configure<DeliveryServiceDBConfig>(
                Configuration.GetSection("DeliveryServiceDBConfig"));
            var s = Configuration.GetSection("DeliveryServiceDBConfig").Get<DeliveryServiceDBConfig>();
            Console.WriteLine("props: " + s.DeliveryContextCollectionName);
            services.AddSingleton<IDeliveryServiceDBConfig>(sp =>
                sp.GetRequiredService<IOptions<DeliveryServiceDBConfig>>().Value);

            services.AddMvc().AddNewtonsoftJson();

            services.AddSingleton<DeliveryServiceDBConfig>();

            string kafkaHost = Environment.GetEnvironmentVariable("KAFKA_ADVERTISED_HOST_NAME")
                                               + ":" + Environment.GetEnvironmentVariable("KAFKA_ADVERTISED_PORT");
            string groupId = Environment.GetEnvironmentVariable("DELIVERY_SERVICE_KAFKA_GROUP_ID");

            Console.WriteLine($"kafka host: {kafkaHost}, groud: {groupId}");

            ProducerConfig producerConfig = new ProducerConfig { BootstrapServers = kafkaHost };
            ConsumerConfig consumerConfig = new ConsumerConfig
            {
                BootstrapServers = kafkaHost,
                GroupId = groupId,
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

            Console.WriteLine("initializing background services");
            //producer
            services.AddSingleton<Func<DeliveryContext, OrderConfirmed>>
                                                    (HelperUtil.GetConfirmedTopicMsgBody);
            services.AddSingleton<Func<DeliveryContext, OrderCancelled>>
                                                    (HelperUtil.GetCancelTopicMsgBody);
            services.AddSingleton<Func<DeliveryContext, OrderDelivered>>
                                                    (HelperUtil.GetDeliveredTopicMsgBody);

            services.AddHostedService<GenericProducer<OrderConfirmed>>();
            services.AddHostedService<GenericProducer<OrderCancelled>>();
            services.AddHostedService<GenericProducer<OrderDelivered>>();

            //consumer
            Func<PaymentStatus, DeliveryContextService, Task<Boolean>> postConsumeHandlerPayStatus =
                ((status, dbService) =>
                {
                    if (!status.Status)
                        return dbService.UpdateStatusAndRemove(status.OrderId, DeliveryContext.Status.PAYMENT_FAIL);
                    else
                        return Task.FromResult(true);
                });

            Func<OrderReceived, DeliveryContextService, Task<Boolean>> postConsumeHandlerOrdReceived =
                ((status, dbService) => dbService.Create(status));

            Func<BuyerContact, DeliveryContextService, Task<Boolean>> postConsumeHandlerUserContact =
                ((contact, dbService) => dbService.UpdateContactDetails(contact));

            Func<OrderCancelled, DeliveryContextService, Task<Boolean>> postConsumeHandlerOrdCancel =
                ((status, dbService) =>
                {
                    if (!status.Reason.Contains("RESTAURANT_CANCELLED"))
                        return dbService.UpdateStatusAndRemove(status.OrderId, DeliveryContext.Status.USER_CANCELLED);
                    else
                        return Task.FromResult(true);
                });

            services.AddSingleton<Func<PaymentStatus, DeliveryContextService,
                                    Task<Boolean>>>(postConsumeHandlerPayStatus);
            services.AddSingleton<Func<OrderReceived, DeliveryContextService,
                                    Task<Boolean>>>(postConsumeHandlerOrdReceived);
            services.AddSingleton<Func<OrderCancelled, DeliveryContextService,
                                    Task<Boolean>>>(postConsumeHandlerOrdCancel);
            services.AddSingleton<Func<BuyerContact, DeliveryContextService,
                                    Task<Boolean>>>(postConsumeHandlerUserContact);

            services.AddHostedService<GenericConsumer<PaymentStatus>>();
            services.AddHostedService<GenericConsumer<OrderReceived>>();
            services.AddHostedService<GenericConsumer<OrderCancelled>>();
            services.AddHostedService<GenericConsumer<BuyerContact>>();

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
                app.UseExceptionHandler("/delivery/error");
            }

            app.UseRouting();

            app.UseAuthorization();

            app.UseEndpoints(endpoints =>
            {
                endpoints.MapControllers();
            });
        }

        private static Boolean PrintMetadata(string bootstrapServers)
        {
            Boolean topicPartionCreated = false;
            try
            {
                using var adminClient = new AdminClientBuilder(new AdminClientConfig { BootstrapServers = bootstrapServers }).Build();
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
            catch (Exception e)
            {
                Console.WriteLine($"error while reaching broker: {e.Message}");
            }
            return topicPartionCreated;
        }

        static string ToString(int[] array) => $"[{string.Join(", ", array)}]";

    }
}
