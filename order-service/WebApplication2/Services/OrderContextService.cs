using Microsoft.Extensions.Logging;
using MongoDB.Bson;
using MongoDB.Driver;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using OrderService.Configuration;
using OrderService.Misc;
using OrderService.Models;

namespace OrderService.Services
{
    public class OrderContextService
    {
        private readonly IMongoCollection<OrderContext> _ord_ctx;
        private readonly ILogger<OrderContextService> _logger;
        public OrderContextService(ILogger<OrderContextService> logger, IOrderContextDatabaseSettings settings)
        {
            _logger = logger;

            var client = new MongoClient(settings.ConnectionString);
            var database = client.GetDatabase(settings.DatabaseName);

            _ord_ctx = database.GetCollection<OrderContext>(settings.OrderContextCollectionName);
        }

        public async Task<OrderContext> GetAsync(string id)
        {
            FindOptions<OrderContext> options = new FindOptions<OrderContext> { Limit = 1 };
            IAsyncCursor<OrderContext> task = await _ord_ctx.FindAsync(x => x.OrderId.Equals(id), options);
            List<OrderContext> list = await task.ToListAsync();
            OrderContext ctx = null;
            try
            {
                ctx = list.First();
            } catch (InvalidOperationException e) {
                _logger.LogError($"Invalid operation for get order: {e.Message}");
                throw new OrderServiceException("Invalid order id.");
            }
            return ctx;
        }

        private async Task GetByBuyerAsync(string id)
        {
            FindOptions<OrderContext> options = new FindOptions<OrderContext> { Limit = 1 };
            IAsyncCursor<OrderContext> task = await _ord_ctx.FindAsync(x => x.BuyerId.Equals(id), options);
            
            try
            {
                List<OrderContext> list = await task.ToListAsync();
                OrderContext ctx = list.First();
            }
            catch (InvalidOperationException e)
            {
                return;
            }
            throw new OrderServiceException("At most one order per customer is allowed");
        }

        public async Task<OrderContext> Create(OrderContext ctx)
        {
            Task t = GetByBuyerAsync(ctx.BuyerId);
            ctx.OrderId = null;
            ctx.ExpireOn = DateTime.UtcNow.AddMinutes(30);
            ctx.LastUpdated = DateTime.UtcNow;
            ctx.Created = DateTime.UtcNow;
            ctx.CurrentStatus = OrderContext.Status.RECEIVED;
            await t;
            await _ord_ctx.InsertOneAsync(ctx);
            GenericProducer<OrderReceived>.ordCtxQueue.Enqueue(ctx);
            return ctx;
        }

        private async Task<bool> UpdateAsync(string id, UpdateDefinition<OrderContext> updates)
        {
            var filter = Builders<OrderContext>.Filter.Eq(ctx => ctx.OrderId, id);
            var update = updates.Set(c => c.LastUpdated, DateTime.UtcNow);
            var result = await _ord_ctx.UpdateOneAsync(filter, update);
            return result.IsAcknowledged;
        }

        private async Task OrderCancelledCheck(string id) 
        {
            OrderContext ctx = await GetAsync(id);
            if (ctx.CurrentStatus >= OrderContext.Status.PAYMENT_FAIL)
            {
                _logger.LogError($"Cancelled/delivered order update check for id:{id} with {ctx.CurrentStatus}");
                throw new OrderServiceException($"Order id:{id} has been cancelled/delivered already.");
            }

        }

        public async Task<bool> UpdateConfirmed(string id, int eta, bool suppressException = false)
        {
            try { 
                _logger.LogInformation($"Confirmed order for id:{id}");
                await OrderCancelledCheck(id);
                var updates = Builders<OrderContext>.Update
                                                    .Set(c => c.CurrentStatus, OrderContext.Status.CONFIRMED)
                                                    .Set(c => c.DeliveryETAMins, eta)
                                                    .Set(c => c.ExpireOn, DateTime.UtcNow.AddMinutes(30));

                return await UpdateAsync(id, updates);
            }
            catch (OrderServiceException e) {
                return ProcessException(suppressException, e);
            }
        }

        public async Task<bool> UpdateStatus(string id, OrderContext.Status status, 
                                                    bool suppressException = false)
        {
            try { 
                _logger.LogInformation($"Status update for order id:{id}");
                await OrderCancelledCheck(id);
                var updates = Builders<OrderContext>.Update.Set(c => c.CurrentStatus, status)
                                    .Set(c => c.ExpireOn, DateTime.UtcNow.AddMinutes(30)); 
                return await UpdateAsync(id, updates);
            }
            catch (OrderServiceException e) {
                return ProcessException(suppressException, e);
            }
        }

        public async Task<bool> UpdateStatusAndRemove(string id, OrderContext.Status status,
                                                        bool suppressException = false)
        {
            try
            {
                _logger.LogInformation($"Cancelled order for id:{id}");
                await OrderCancelledCheck(id);
                var updates = Builders<OrderContext>.Update.Set(c => c.CurrentStatus, status)
                                                        .Set(c => c.ExpireOn, DateTime.UtcNow.AddMinutes(5));
                return await UpdateAsync(id, updates);
            }
            catch (OrderServiceException e) {
                return ProcessException(suppressException, e);
            }

        }

        private bool ProcessException(bool suppress, OrderServiceException e) {
            if (suppress)
            {
                _logger.LogError($"Exception during status update: {e.Message}");
                return false;
            }
            else
                throw e;
        }

        public void Remove(string id) =>
            _ord_ctx.DeleteOne(ctx => ctx.OrderId == id);
    }
}

